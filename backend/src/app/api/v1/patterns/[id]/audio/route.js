import { NextResponse } from "next/server";
import Pattern from "@/models/Pattern";
import {
  connectDB,
  isAdmin,
  synthesize,
  uploadAudio,
  deleteAudio,
  pickSlots,
  publicIdFor,
} from "@/lib/audio";

const unauthorized = () => NextResponse.json({ error: "Unauthorized" }, { status: 401 });

// POST /api/patterns/:id/audio  -> generate audio with Google TTS
// body: { target: "all"|"title"|"meaning"|"example", index?, lang?: "en"|"bn", force?: boolean }
//  - "all": only missing or stale audio is generated
//  - manual (your own recorded) audio is never overwritten unless force = true
export async function POST(req, { params }) {
  if (!isAdmin(req)) return unauthorized();
  const { id } = await params;
  await connectDB();
  const body = await req.json();
  const force = !!body.force;
  const p = await Pattern.findById(id);
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });

  const slots = pickSlots(p, body);
  if (!slots.length) return NextResponse.json({ error: "Invalid target" }, { status: 400 });

  const generated = [];
  const skipped = [];

  for (const s of slots) {
    const a = s.holder[s.field];
    const blocked = a?.source === "manual" && !force;
    const should = body.target === "all" ? !a?.url || a.stale || force : true;
    if (blocked || !should) {
      skipped.push(s.key);
      continue;
    }
    const buffer = await synthesize(s.text, s.lang);
    const result = await uploadAudio(buffer, publicIdFor(id, s.key));
    s.holder[s.field] = {
      url: result.secure_url,
      publicId: result.public_id,
      source: "tts",
      stale: false,
    };
    generated.push(s.key);
  }

  await p.save();
  return NextResponse.json({ generated, skipped });
}

// PUT /api/patterns/:id/audio  -> upload your own recorded mp3 (multipart/form-data)
// fields: file, target, index?, lang?
export async function PUT(req, { params }) {
  if (!isAdmin(req)) return unauthorized();
  const { id } = await params;
  await connectDB();
  const form = await req.formData();
  const file = form.get("file");
  const target = form.get("target");
  const index = form.get("index") !== null ? Number(form.get("index")) : undefined;
  const lang = form.get("lang") || undefined;
  if (!file) return NextResponse.json({ error: "file required" }, { status: 400 });

  const p = await Pattern.findById(id);
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });
  const [s] = pickSlots(p, { target, index, lang });
  if (!s) return NextResponse.json({ error: "Invalid target" }, { status: 400 });

  const buffer = Buffer.from(await file.arrayBuffer());
  const result = await uploadAudio(buffer, publicIdFor(id, s.key));
  s.holder[s.field] = {
    url: result.secure_url,
    publicId: result.public_id,
    source: "manual",
    stale: false,
  };
  await p.save();
  return NextResponse.json({ key: s.key, url: result.secure_url });
}

// DELETE /api/patterns/:id/audio  -> remove audio. body: { target, index?, lang? }
export async function DELETE(req, { params }) {
  if (!isAdmin(req)) return unauthorized();
  const { id } = await params;
  await connectDB();
  const body = await req.json();
  const p = await Pattern.findById(id);
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });
  const [s] = pickSlots(p, body);
  if (!s) return NextResponse.json({ error: "Invalid target" }, { status: 400 });

  await deleteAudio(s.holder[s.field]?.publicId);
  s.holder[s.field] = undefined;
  await p.save();
  return NextResponse.json({ ok: true });
}
