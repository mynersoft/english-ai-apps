import { NextResponse } from "next/server";
import Pattern from "@/models/Pattern";
import { connectDB, isAdmin } from "@/lib/audio";

// GET /api/patterns  -> list screen (title, meaning, premium, count, audio)
export async function GET() {
  await connectDB();
  const patterns = await Pattern.find().sort({ order: 1, createdAt: 1 }).lean();
  return NextResponse.json(
    patterns.map((p) => ({
      id: p._id,
      title: p.title,
      meaning: p.meaning,
      isPremium: p.isPremium,
      sentenceCount: p.examples.length,
      titleAudioUrl: p.titleAudio?.url ?? null,
      meaningAudioUrl: p.meaningAudio?.url ?? null,
    }))
  );
}

// POST /api/patterns  -> add new pattern (admin)
export async function POST(req) {
  if (!isAdmin(req)) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  await connectDB();
  const body = await req.json();
  const pattern = await Pattern.create({
    title: body.title,
    meaning: body.meaning,
    explanation: body.explanation,
    isPremium: !!body.isPremium,
    order: body.order ?? 0,
    examples: (body.examples || []).map((e) => ({ en: e.en, bn: e.bn })),
  });
  return NextResponse.json(pattern, { status: 201 });
}