import mongoose from "mongoose";

const userSettingsSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true, unique: true },
    notifications: {
      dailyReminder: { type: Boolean, default: true },
      lessonReminder: { type: Boolean, default: true },
      speakingReminder: { type: Boolean, default: true },
      weeklySummary: { type: Boolean, default: true },
    },
    explanationLanguage: { type: String, default: "bn" },
  },
  { timestamps: true, collection: "userSettings" }
);

export default mongoose.models.UserSettings || mongoose.model("UserSettings", userSettingsSchema);
