import { apiHandler } from "@/lib/apiHandler";
import { connectDB } from "@/lib/db";
import { ok } from "@/lib/response";
import { verifyRequestToken } from "@/middleware/auth";
import { assertProvider, toPublicUser, upsertUserFromToken } from "@/modules/auth/auth.service";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// POST /api/v1/auth/email/login  (Bearer token from Firebase signInWithEmailAndPassword)
export const POST = apiHandler(async (request) => {
  const decoded = await verifyRequestToken(request);
  assertProvider(decoded, "password");
  await connectDB();
  const { user } = await upsertUserFromToken(decoded);
  return ok({ user: toPublicUser(user) });
}, { limit: 20 });
