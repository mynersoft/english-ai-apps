import { generateText } from "@/lib/aiProvider";

const LEVEL_RULES = {
  beginner: "Use very simple words and short sentences (max 12 words). Speak slowly and clearly.",
  intermediate: "Use everyday English with natural sentences. Introduce one useful new word or phrase sometimes.",
  advanced: "Use natural, rich English with idioms and longer sentences. Challenge the student a little.",
};

function buildSystemPrompt({ level, topic }) {
  return `You are "English AI", a friendly English speaking partner for Bangla-speaking learners.
Topic: ${topic}. Student level: ${level}.
${LEVEL_RULES[level]}

Each turn, do two jobs:
1. Reply naturally to the student in 1-3 short sentences and end with ONE easy question to keep the conversation going.
2. Check the student's LAST message for grammar, tense, article, preposition or word-order mistakes.
   The message comes from speech-to-text, so IGNORE capitalization and punctuation. Do not mark a sentence wrong for style only.

Return ONLY valid JSON in exactly this shape:
{
  "reply": "your spoken reply in English",
  "correction": {
    "hasMistake": true or false,
    "original": "the student's sentence",
    "corrected": "the corrected sentence (empty string if no mistake)",
    "explanationBn": "short, simple explanation in Bangla (empty string if no mistake)"
  }
}
If the student's message is correct, set hasMistake to false and keep the other correction fields empty.
Never add text outside the JSON.`;
}

function parseModelJson(raw) {
  const cleaned = String(raw || "").replace(/```json|```/gi, "").trim();
  const start = cleaned.indexOf("{");
  const end = cleaned.lastIndexOf("}");
  if (start === -1 || end === -1) return null;
  try {
    return JSON.parse(cleaned.slice(start, end + 1));
  } catch {
    return null;
  }
}

/** One conversation turn: AI reply + grammar correction of the student's message. */
export async function speakingTurn({ message, level, topic, history }) {
  const raw = await generateText({
    system: buildSystemPrompt({ level, topic }),
    messages: [...history, { role: "user", content: message }],
    json: true,
    maxTokens: 500,
    temperature: 0.6,
  });

  const parsed = parseModelJson(raw);
  if (!parsed || typeof parsed.reply !== "string") {
    // Model did not return JSON: still give the student a usable reply.
    return {
      reply: String(raw || "").trim() || "Sorry, I did not understand. Can you say that again?",
      correction: { hasMistake: false, original: message, corrected: "", explanationBn: "" },
    };
  }

  const c = parsed.correction || {};
  const corrected = typeof c.corrected === "string" ? c.corrected.trim() : "";
  const hasMistake = c.hasMistake === true && corrected !== "" && corrected !== message;
  return {
    reply: parsed.reply.trim(),
    correction: {
      hasMistake,
      original: message,
      corrected: hasMistake ? corrected : "",
      explanationBn: hasMistake && typeof c.explanationBn === "string" ? c.explanationBn.trim() : "",
    },
  };
}
