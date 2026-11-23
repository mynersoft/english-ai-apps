import mongoose from "mongoose";

// Passwords are never stored here: Firebase handles credentials.
const userSchema = new mongoose.Schema(
  {
    firebaseUid: { type: String, required: true, unique: true, index: true },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    name: { type: String, required: true, trim: true, maxlength: 80 },
    photoUrl: { type: String, default: null },
    authProviders: { type: [String], default: [] }, // "password", "google.com"
    role: { type: String, enum: ["student", "admin"], default: "student" },
    status: { type: String, enum: ["active", "deactivated"], default: "active", index: true },
    learningLevel: { type: String, enum: ["beginner", "intermediate", "advanced", null], default: null },
    dailyGoalMinutes: { type: Number, default: 20, min: 5, max: 240 },
    lastLoginAt: { type: Date, default: null },
  },
  { timestamps: true }
);

export default mongoose.models.User || mongoose.model("User", userSchema);
