import { NextResponse } from "next/server";
import Pattern from "@/models/Pattern";
import { connectDB, isAdmin, deleteAudio, listSlots } from "@/lib/audio";

const audioInfo = (a) => ({
  url: a?.url ?? null,
  source: a?.source ?? null,
  stale: a?.stale ?? false,
});

// GET /api/patterns/:id -> full pattern for the sentence screen
export async function GET(req, { params }) {
  const { id } = await params;
  await connectDB();
  const p = await Pattern.findById(id).lean();
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });

  return NextResponse.json({
    id: p._id,
    title: p.title,
    meaning: p.meaning,
    explanation: p.explanation,
    isPremium: p.isPremium,
    titleAudio: audioInfo(p.titleAudio),
    meaningAudio: audioInfo(p.meaningAudio),
    examples: p.examples.map((e) => ({
      id: e._id,
      en: e.en,
      bn: e.bn,
      enAudio: audioInfo(e.enAudio),
      bnAudio: audioInfo(e.bnAudio),
    })),
  });
}

// PUT /api/patterns/:id -> edit text (admin). Changed text => audio marked stale.
// Send existing examples with their "id" so their audio is kept.
export async function PUT(req, { params }) {
  if (!isAdmin(req)) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const { id } = await params;
  await connectDB();
  const body = await req.json();
  const p = await Pattern.findById(id);
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });

  if (body.title !== undefined && body.title !== p.title) {
    p.title = body.title;
    if (p.titleAudio?.url) p.titleAudio.stale = true;
  }
  if (body.meaning !== undefined && body.meaning !== p.meaning) {
    p.meaning = body.meaning;
    if (p.meaningAudio?.url) p.meaningAudio.stale = true;
  }
  if (body.explanation !== undefined) p.explanation = body.explanation;
  if (body.isPremium !== undefined) p.isPremium = !!body.isPremium;
  if (body.order !== undefined) p.order = body.order;

  if (Array.isArray(body.examples)) {
    const oldById = new Map(p.examples.map((e) => [String(e._id), e]));
    const keptIds = new Set();
    const next = body.examples.map((inc) => {
      const old = inc.id ? oldById.get(String(inc.id)) : null;
      if (!old) return { en: inc.en, bn: inc.bn }; // new example
      keptIds.add(String(old._id));
      const ex = old.toObject();
      if (inc.en !== old.en) {
        ex.en = inc.en;
        if (ex.enAudio?.url) ex.enAudio.stale = true;
      }
      if (inc.bn !== old.bn) {
        ex.bn = inc.bn;
        if (ex.bnAudio?.url) ex.bnAudio.stale = true;
      }
      return ex;
    });

    // removed examples -> clean up their audio files
    for (const old of p.examples) {
      if (!keptIds.has(String(old._id))) {
        await deleteAudio(old.enAudio?.publicId);
        await deleteAudio(old.bnAudio?.publicId);
      }
    }
    p.examples = next;
  }

  await p.save();
  return NextResponse.json({ ok: true });
}

// DELETE /api/patterns/:id -> remove pattern + all its audio (admin)
export async function DELETE(req, { params }) {
  if (!isAdmin(req)) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const { id } = await params;
  await connectDB();
  const p = await Pattern.findById(id);
  if (!p) return NextResponse.json({ error: "Not found" }, { status: 404 });

  for (const s of listSlots(p)) await deleteAudio(s.holder[s.field]?.publicId);
  await p.deleteOne();
  return NextResponse.json({ ok: true });
}

