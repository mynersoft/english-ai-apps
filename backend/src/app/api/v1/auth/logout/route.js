import { ok } from "@/lib/response";
import { withAuth } from "@/middleware/auth";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// POST /api/v1/auth/logout
// The app signs out of Firebase locally. We do NOT revoke refresh tokens here because that
// would sign the user out of all their other devices (multi-device login is a requirement).
export const POST = withAuth(async () => ok({ loggedOut: true }));
