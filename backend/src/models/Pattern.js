import mongoose from "mongoose";

const AudioSchema = new mongoose.Schema(
  {
    url: String,
    publicId: String,
    source: { type: String, enum: ["tts", "manual"] },
    stale: { type: Boolean, default: false }, // text changed after audio was made
  },
  { _id: false }
);

const ExampleSchema = new mongoose.Schema({
  en: { type: String, required: true },
  bn: { type: String, required: true },
  enAudio: AudioSchema,
  bnAudio: AudioSchema,
});

const PatternSchema = new mongoose.Schema(
  {
    title: { type: String, required: true }, // I'd like to + Verb
    meaning: { type: String, required: true }, // আমি ... করতে চাই
    explanation: String,
    isPremium: { type: Boolean, default: false },
    order: { type: Number, default: 0 },
    titleAudio: AudioSchema,
    meaningAudio: AudioSchema,
    examples: [ExampleSchema],
  },
  { timestamps: true }
);

export default mongoose.models.Pattern ||
  mongoose.model("Pattern", PatternSchema);
