# Bakery Production Planning API (Lab 4: Security)

REST API для управления производством пекарни с аутентификацией и авторизацией через Spring Security.

## Стек

- Java 21, Spring Boot, Maven
- Spring Web MVC, Spring Data JPA, Hibernate
- PostgreSQL
- Spring Security (HTTP Basic Auth, CSRF, BCrypt)
- dotenv-java

## Предметная область

1. User — пользователь системы (заказчик).
2. Ingredient — ингредиент (мука, сахар).
3. Recipe — рецептура (состав изделия).
4. ProductionBatch — производственная партия.
5. BakingRequest — заявка на выпечку.

## Аутентификация и авторизация

### Регистрация

POST /api/auth/register — создание пользователя.
- Пароль: минимум 8 символов, хотя бы одна буква и одна цифра.
- Email должен быть уникальным.
- Роль всегда USER — повышение прав через регистрацию невозможно.

### Аутентификация

HTTP Basic Auth: Authorization: Basic base64(email:password).
- Пароли хранятся в БД в виде BCrypt-хэша.
- Учётные записи: admin@bakery.local / admin123 (ADMIN), user@bakery.local / user123 (USER).
- Админ создаётся автоматически при старте через DataInitializer.

### CSRF

- CSRF-защита включена для всех POST/PATCH/DELETE.
- Токен получается через GET /api/auth/csrf (возвращается в JSON).
- Передаётся в заголовке X-XSRF-TOKEN.
- Для регистрации и логина CSRF отключён.

### Роли и права

USER — чтение ингредиентов/рецептур, создание/просмотр своих заявок, удаление только своих заявок.
ADMIN — все права USER + создание/изменение/удаление ингредиентов, рецептур, партий; выполнение бизнес-операций; управление пользователями.

### @PreAuthorize

Контекстная проверка: USER может удалить только свою заявку.
Реализовано через BakingRequestSecurity.isOwner(...) и аннотацию @PreAuthorize.

## REST API

CRUD для всех сущностей (см. requests.http):

GET /api/users — ADMIN
POST /api/ingredients — ADMIN
GET /api/ingredients — Любой авторизованный
POST /api/recipes — ADMIN
POST /api/production-batches — ADMIN
POST /api/baking-requests — Любой авторизованный
DELETE /api/baking-requests/{id} — ADMIN или владелец
POST /api/business/** — ADMIN

## Бизнес-операции

1. POST /api/business/start-batch/{id} — запуск партии со списанием ингредиентов.
2. POST /api/business/complete-batch/{id} — завершение партии.
3. POST /api/business/request-to-batch/{id} — превращение заявки в партию.
4. POST /api/business/plan-batch — массовое планирование.
5. POST /api/business/close-batch/{id} — закрытие партии с обновлением заявок.

## Обработка ошибок

401 Unauthorized — нет аутентификации (JSON).
403 Forbidden — недостаточно прав (JSON).
400 Bad Request — бизнес-ограничения.
Ошибки не раскрывают чувствительных данных (не говорят, какой email существует).

## Запуск

Требования:
- Java 21, Maven, PostgreSQL 14+
- Файл .env в корне (см. .env.example)

Настройка БД:
CREATE DATABASE bakery;
CREATE USER bakery_user WITH PASSWORD 'bakery_pass';
GRANT CONNECT ON DATABASE bakery TO bakery_user;
GRANT USAGE ON SCHEMA public TO bakery_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO bakery_user;
GRANT CREATE ON SCHEMA public TO bakery_user;

Файл .env:
DB_URL=jdbc:postgresql://localhost:5432/bakery
DB_USERNAME=bakery_user
DB_PASSWORD=bakery_pass

Запуск:
./mvnw spring-boot:run

Сервер стартует на http://localhost:8080.

## Тестирование

Файл requests.http содержит все запросы с примерами Basic Auth и CSRF-токенами.

Сценарий проверки:
1. GET /api/auth/csrf — получить CSRF-токен.
2. Запустить POST /api/ingredients (admin) — 201 Created.
3. Запустить POST /api/ingredients (user) — 403 Forbidden.
4. Создать заявки от user и admin.
5. USER удаляет свою заявку → 204.
6. USER удаляет чужую заявку → 403.
7. ADMIN удаляет любую заявку → 204.

## Структура

src/main/java/com/example/student/
├── StudentApplication.java
├── auth/ — AuthController, RegisterRequest
├── business/ — бизнес-операции
├── controller/ — CRUD-контроллеры
├── model/ — JPA-сущности
├── repository/ — JpaRepository
├── security/ — SecurityConfig, CustomUserDetailsService, DataInitializer, RestAuthenticationEntryPoint, RestAccessDeniedHandler, BakingRequestSecurity
└── service/ — бизнес-логика CRUD