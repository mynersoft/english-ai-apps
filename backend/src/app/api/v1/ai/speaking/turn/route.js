import { ok } from "@/lib/response";
import { withAuth } from "@/middleware/auth";
import { readJson } from "@/validators/auth.validator";
import { validateSpeakingTurn } from "@/validators/speaking.validator";
import { speakingTurn } from "@/modules/ai/speaking.service";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";
export const maxDuration = 30;

// POST /api/v1/ai/speaking/turn
// Body: { message, level?, topic?, history?: [{ role: "user"|"assistant", content }] }
// Returns: { reply, correction: { hasMistake, original, corrected, explanationBn } }
export const POST = withAuth(
  async (request) => {
    const input = validateSpeakingTurn(await readJson(request));
    const result = await speakingTurn(input);
    return ok(result);
  },
  { limit: 20 }
);
