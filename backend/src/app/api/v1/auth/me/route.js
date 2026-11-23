import { ok } from "@/lib/response";
import { withAuth } from "@/middleware/auth";
import { toPublicUser } from "@/modules/auth/auth.service";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// GET /api/v1/auth/me
export const GET = withAuth(async (request, { user }) => ok({ user: toPublicUser(user) }), { limit: 60 });
