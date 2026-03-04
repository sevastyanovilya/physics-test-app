# Физика: Тест — Android-приложение для репетитора

Приложение для тестирования учеников по физике (7–11 классы). Работает полностью офлайн.

## Быстрый старт через GitHub Actions (без Android Studio)

### 1. Загружаем проект в GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/physics-test-app.git
git push -u origin main
```

### 2. APK собирается автоматически

Перейдите: репозиторий → вкладка **Actions** → выберите последнюю сборку → раздел **Artifacts** → скачайте `PhysicsTest-APK.zip` → распакуйте → устанавливайте APK.

### 3. Добавляем подпись APK (опционально, но рекомендуется)

Без подписи приложение собирается как debug — это работает, но показывает предупреждение.
Для release-подписи нужен Java (только keytool, не нужен Android Studio):

```bash
# Генерируем keystore (один раз)
keytool -genkeypair -v -keystore release.jks -alias physics_key \
  -keyalg RSA -keysize 2048 -validity 10000

# Кодируем в base64 (для GitHub Secret)
base64 release.jks | tr -d '\n'   # Linux/Mac
```

Добавить в GitHub: Settings → Secrets and variables → Actions:
- `KEYSTORE_BASE64` — вывод команды base64
- `KEYSTORE_PASSWORD` — пароль keystore
- `KEY_ALIAS` — physics_key
- `KEY_PASSWORD` — пароль ключа

## Установка APK на телефон

1. Настройки → Безопасность → **Установка из неизвестных источников** → включить
2. Открыть скачанный APK → **Установить**
3. При первом запуске — задать пароль репетитора

## Структура проекта

```
app/src/main/java/com/physics/tutor/
├── data/db/           — Room БД (entities, DAOs)
├── data/repository/   — AppRepository (единая точка доступа к данным)
├── ui/                — Activity (splash, auth, grade, test, result, tutor)
├── viewmodel/         — TestViewModel, TutorViewModel
└── util/              — DatabasePopulator, QrCodeGenerator, CsvExporter
```

## Технические характеристики

- Android 7.0+ (minSdk 24), targetSdk 34
- Kotlin + Room + LiveData + ViewModel (MVVM)
- 100% офлайн — никаких сетевых запросов
- Авторизация: UUID (автоматический) или 4-значный PIN
- 60 вопросов по ФГОС для 7–11 классов
