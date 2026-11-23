import { AppError } from "@/lib/errors";

/**
 * One function for every AI provider. Switch provider/model ONLY from env vars:
 *   AI_PROVIDER = gemini | groq | anthropic     (default: gemini - has a free tier)
 *   AI_MODEL    = model name for that provider (optional, sensible defaults below)
 *   AI_API_KEY  = key for the chosen provider
 */
const DEFAULT_MODELS = {
  gemini: "gemini-2.0-flash",
  groq: "llama-3.3-70b-versatile",
  anthropic: "claude-haiku-5-5",
};

const TIMEOUT_MS = 25_000;

async function postJson(url, headers, body) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...headers },
      body: JSON.stringify(body),
      signal: controller.signal,
    });
    if (res.status === 429) {
      throw new AppError(429, "AI_BUSY", "The AI teacher is busy right now. Please try again in a minute.");
    }
    if (!res.ok) {
      console.error("AI provider error:", res.status, (await res.text()).slice(0, 300));
      throw new AppError(502, "AI_UNAVAILABLE", "The AI teacher is not available right now.");
    }
    return await res.json();
  } catch (err) {
    if (err instanceof AppError) throw err;
    if (err?.name === "AbortError") throw new AppError(504, "AI_TIMEOUT", "The AI teacher took too long to answer.");
    console.error("AI request failed:", err);
    throw new AppError(502, "AI_UNAVAILABLE", "The AI teacher is not available right now.");
  } finally {
    clearTimeout(timer);
  }
}

/**
 * @param {string} system   instructions
 * @param {{role:"user"|"assistant", content:string}[]} messages  conversation, last one is the user's
 * @param {{json?:boolean, maxTokens?:number, temperature?:number}} opts
 * @returns {Promise<string>} model text
 */
export async function generateText({ system, messages, json = false, maxTokens = 600, temperature = 0.6 }) {
  const provider = (process.env.AI_PROVIDER || "gemini").toLowerCase();
  const apiKey = process.env.AI_API_KEY;
  if (!apiKey) throw new AppError(500, "AI_NOT_CONFIGURED", "AI is not configured on the server.");
  const model = process.env.AI_MODEL || DEFAULT_MODELS[provider];
  if (!model) throw new AppError(500, "AI_NOT_CONFIGURED", `Unknown AI_PROVIDER "${provider}".`);

  if (provider === "gemini") {
    const data = await postJson(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
      { "x-goog-api-key": apiKey },
      {
        systemInstruction: { parts: [{ text: system }] },
        contents: messages.map((m) => ({
          role: m.role === "assistant" ? "model" : "user",
          parts: [{ text: m.content }],
        })),
        generationConfig: {
          temperature,
          maxOutputTokens: maxTokens,
          ...(json ? { responseMimeType: "application/json" } : {}),
        },
      }
    );
    return data?.candidates?.[0]?.content?.parts?.map((p) => p.text).join("") || "";
  }

  if (provider === "groq") {
    const data = await postJson(
      "https://api.groq.com/openai/v1/chat/completions",
      { Authorization: `Bearer ${apiKey}` },
      {
        model,
        temperature,
        max_tokens: maxTokens,
        messages: [{ role: "system", content: system }, ...messages],
        ...(json ? { response_format: { type: "json_object" } } : {}),
      }
    );
    return data?.choices?.[0]?.message?.content || "";
  }

  if (provider === "anthropic") {
    const data = await postJson(
      "https://api.anthropic.com/v1/messages",
      { "x-api-key": apiKey, "anthropic-version": "2023-06-01" },
      { model, max_tokens: maxTokens, temperature, system, messages }
    );
    return data?.content?.map((b) => b.text || "").join("") || "";
  }

  throw new AppError(500, "AI_NOT_CONFIGURED", `Unknown AI_PROVIDER "${provider}".`);
}
