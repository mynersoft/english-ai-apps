import { apiHandler } from "@/lib/apiHandler";
import { connectDB } from "@/lib/db";
import { ok } from "@/lib/response";
import { verifyRequestToken } from "@/middleware/auth";
import { assertProvider, toPublicUser, upsertUserFromToken } from "@/modules/auth/auth.service";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// POST /api/v1/auth/google  (Authorization: Bearer <Firebase ID token from Google sign-in>)
export const POST = apiHandler(async (request) => {
  const decoded = await verifyRequestToken(request);
  assertProvider(decoded, "google.com");
  await connectDB();
  const { user, isNewUser } = await upsertUserFromToken(decoded);
  return ok({ user: toPublicUser(user), isNewUser }, isNewUser ? 201 : 200);
});
