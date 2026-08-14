# REBOOT API

POST /api/v1/auth/register
POST /api/v1/auth/login
GET /api/v1/profile
PUT /api/v1/profile/onboarding
GET /api/v1/dashboard
GET /api/v1/plans
GET /api/v1/progress
GET /api/v1/achievements
GET /api/v1/habits
GET /api/v1/history
POST /api/v1/ai/chat
POST /api/v1/ai/plan
GET /api/v1/settings
GET /api/v1/subscription
GET /api/v1/referral
GET /health

Все защищённые endpoints используют:
Authorization: Bearer <JWT>

AI:
Android -> /api/v1/ai/chat -> Groq
Ключ Groq хранится только на backend.
