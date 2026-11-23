# English AI: Login and Registration (Phase 2)

Two repos (put each in its own GitHub repo):
- english-ai-android  -> Kotlin + Compose app
- english-ai-backend  -> Next.js (JavaScript) API

## Firebase setup (once)
1. Firebase Console: create project, enable Authentication > Email/Password and Google.
2. Add Android app, package `com.englishai.app`. Add your debug SHA-1 (needed for Google Sign-In).
3. Download google-services.json -> `english-ai-android/app/google-services.json`
   (or, for GitHub Actions only: add repo secret GOOGLE_SERVICES_JSON = base64 of the file).
4. Project settings > Service accounts > generate key -> fill the FIREBASE_* values in Vercel / .env.local.

## Backend
cd english-ai-backend && cp .env.example .env.local && npm install && npm run dev
Deploy to Vercel and add the same env vars.

## Android
1. Set API_BASE_URL in gradle.properties (must end with "/").
2. Push to main. GitHub Actions builds the APK (artifact: english-ai-debug-apk).
3. Optional wrapper (docs ask for it): in Codespaces run `gradle wrapper --gradle-version 8.9`, commit gradlew + gradle/wrapper/*.

## Flow
Register: Firebase createUser -> ID token -> POST /api/v1/auth/email/register {name}
Login:    Firebase signIn    -> ID token -> POST /api/v1/auth/email/login
Google:   Credential Manager -> Firebase credential -> POST /api/v1/auth/google
Reset:    Firebase sendPasswordResetEmail
Session:  app start -> GET /api/v1/auth/me
