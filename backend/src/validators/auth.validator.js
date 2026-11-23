import { AppError } from "@/lib/errors";

export async function readJson(request) {
  try {
    return await request.json();
  } catch {
    return {};
  }
}

export function validateRegister(body) {
  const name = typeof body?.name === "string" ? body.name.trim().replace(/\s+/g, " ") : "";
  if (name.length < 2 || name.length > 60) {
    throw new AppError(400, "VALIDATION_ERROR", "Name must be between 2 and 60 characters.");
  }
  return { name };
}
