import { AppError } from "@/lib/errors";

const LEVELS = ["beginner", "intermediate", "advanced"];
const TOPICS = [
  "Daily Life", "Self Introduction", "Family", "Education", "Shopping", "Travel",
  "Food", "Job Interviews", "Workplace", "Social Communication", "Business Communication",
];

export function validateSpeakingTurn(body) {
  const message = typeof body?.message === "string" ? body.message.trim().replace(/\s+/g, " ") : "";
  if (!message) throw new AppError(400, "VALIDATION_ERROR", "Message is required.");
  if (message.length > 500) throw new AppError(400, "VALIDATION_ERROR", "Message is too long (max 500 characters).");

  const level = LEVELS.includes(body?.level) ? body.level : "beginner";
  const topic = TOPICS.includes(body?.topic) ? body.topic : "Daily Life";

  // Only the last 10 turns are used, to keep cost low.
  const history = (Array.isArray(body?.history) ? body.history : [])
    .filter((h) => (h?.role === "user" || h?.role === "assistant") && typeof h?.content === "string" && h.content.trim())
    .slice(-10)
    .map((h) => ({ role: h.role, content: h.content.trim().slice(0, 500) }));

  return { message, level, topic, history };
}
