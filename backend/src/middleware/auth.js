import { AppError } from "@/lib/errors";
import { adminAuth } from "@/lib/firebaseAdmin";
import { connectDB } from "@/lib/db";
import { apiHandler } from "@/lib/apiHandler";
import User from "@/models/User";

/** Verifies the Firebase ID token from "Authorization: Bearer <token>". */
export async function verifyRequestToken(request) {
  const header = request.headers.get("authorization") || "";
  const match = header.match(/^Bearer (.+)$/i);
  if (!match) throw new AppError(401, "UNAUTHORIZED", "Missing authentication token.");
  try {
    return await adminAuth().verifyIdToken(match[1], true); // true = reject revoked tokens
  } catch {
    throw new AppError(401, "UNAUTHORIZED", "Invalid or expired token. Please log in again.");
  }
}

/**
 * Protects a route: valid token + existing, active user (+ optional role check).
 * handler receives (request, { decoded, user, params }).
 */
export function withAuth(handler, { roles, limit } = {}) {
  return apiHandler(async (request, context) => {
    const decoded = await verifyRequestToken(request);
    await connectDB();
    const user = await User.findOne({ firebaseUid: decoded.uid });
    if (!user) throw new AppError(404, "USER_NOT_FOUND", "Account not found. Please log in again.");
    if (user.status !== "active") throw new AppError(403, "ACCOUNT_DISABLED", "This account is deactivated.");
    if (roles && !roles.includes(user.role)) throw new AppError(403, "FORBIDDEN", "You do not have access.");
    return handler(request, { ...context, decoded, user });
  }, { limit });
}
