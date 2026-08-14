# REBOOT MASTER v1.0

Полный стартовый монорепозиторий REBOOT:
- Android/Kotlin + Jetpack Compose
- FastAPI backend
- JWT регистрация/вход
- PostgreSQL-ready модели
- персональный onboarding: привычки/проблемы/цели
- AI Gateway для Groq
- генерация задач/планов
- история AI
- локальный профиль/состояние
- голосовой интерфейс через Android SpeechRecognizer + TextToSpeech
- 5 вкладок
- RPG: XP, уровни, streak, achievements
- режимы AI: Друг/Тренер/Провокатор/Мудрец/Турбо
- планы: тренировки, саморазвитие, карьера, психология, здоровье, свободная цель
- прогресс, привычки, дневник, режим тишины
- referral/subscription API-заготовки
- Docker Compose для backend + PostgreSQL + Redis

ВАЖНО:
1. GROQ_API_KEY хранится ТОЛЬКО на backend в .env.
2. Для production нужен HTTPS, нормальная миграция БД, email verification,
   rate limiting, secrets manager, privacy policy и возрастные ограничения.
3. Провокатор/Турбо не оскорбляют пользователя и не дают опасных заданий.
4. Медицинские/кризисные ситуации не диагностируются ИИ.

Сборка Android:
- открыть папку в Android Studio
- дождаться Gradle sync
- Build > Build APK(s)

Запуск backend:
docker compose -f backend/docker-compose.yml up --build
