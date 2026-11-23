import { apiHandler } from "@/lib/apiHandler";
import { connectDB } from "@/lib/db";
import { ok } from "@/lib/response";
import { verifyRequestToken } from "@/middleware/auth";
import { deleteAccount } from "@/modules/auth/auth.service";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// DELETE /api/v1/auth/account  (permanent: removes app data and the Firebase account)
export const DELETE = apiHandler(async (request) => {
  const decoded = await verifyRequestToken(request);
  await connectDB();
  await deleteAccount(decoded);
  return ok({ deleted: true });
}, { limit: 5 });
