# Task Tracker

Проект для управления личными задачами, построенный на микросервисной архитектуре.
В системе есть регистрация и авторизация пользователей, работа с задачами, маршрутизация запросов через gateway, обмен событиями через Kafka и отправка email-уведомлений.
Клиентская часть реализована отдельным frontend-приложением на React.

## О проекте

- регистрирует и аутентифицирует пользователей через JWT
- хранит и управляет личными задачами
- поддерживает статусы задач: `создана`, `бэклог`, `заблокирована`, `завершена`, `отменена`
- позволяет создавать, просматривать, редактировать и удалять задачи из веб-интерфейса
- отправляет welcome письмо после регистрации
- формирует ежедневные email-напоминания по задачам через планировщик
- синхронизирует сервисы через события в Kafka
- маршрутизирует клиентские запросы через единый API Gateway

## Технические особенности

- микросервисная архитектура с разделением ответственности между сервисами
- безопасная аутентификация и авторизация на базе Spring Security и JWT
- событийное взаимодействие между сервисами через Kafka
- централизованная конфигурация через Spring Cloud Config
- service discovery через Eureka
- использование Redis для distributed lock планировщика и ограничения отправки email
- SPA frontend на React и TypeScript — полностью написан Claude Code
- локальный запуск всей системы через Docker Compose

## Сервисы

| Сервис | Назначение |
| --- | --- |
| `frontend` | Веб-интерфейс для регистрации, входа, списка задач и страницы задачи |
| `gateway-service` | Единая точка входа для клиентских API-запросов |
| `account-service` | Регистрация, логин, выпуск JWT, endpoint текущего пользователя |
| `task-management-service` | CRUD для задач, статусы, планировщик напоминаний |
| `mail-service` | Получает сообщения из Kafka и отправляет email |
| `registry` | Eureka service discovery |
| `config-service` | Централизованная конфигурация Spring-сервисов |

## Стек

### Backend

- Общее: Java 21, Spring Boot 3
- Микросервисы на Spring Cloud: Config, Gateway, Eureka, Redis для распределённой блокировки, Kafka
- Аутентификация: Spring Security
- БД: Spring Data JPA, PostgreSQL, Flyway
- Документация: OpenAPI / Swagger
- Тесты: JUnit 5 + Testcontainers

### Frontend

- React 19
- TypeScript
- Vite

### Infrastructure

- Docker
- Docker Compose
- Apache Kafka
- PostgreSQL
- Redis

## Локальный запуск через Docker

### Что понадобится

- Docker Desktop или Docker Engine с Docker Compose
- Java 21 локально

Почему нужна Java: backend Docker-образы в этом репозитории не собирают приложение внутри контейнера, а ожидают готовые `.jar` файлы в `build/libs` каждого сервиса. Поэтому перед `docker compose up` нужно один раз собрать backend-артефакты локально.

### 1. Клонировать репозиторий

```bash
git clone <your-repository-url>
cd task-tracker
```

### 2. Подготовить переменные окружения

В репозитории уже есть минимальный `.env` для PostgreSQL:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

Если вы хотите, чтобы работала отправка welcome email и ежедневных напоминаний, добавьте в `.env` ещё и эти переменные:

```env
SMPT_SERVER_USERNAME=your_email@gmail.com
SMPT_SERVER_PASSWORD=your_app_password
```

Важно: в проекте переменные называются именно `SMPT_*`, поэтому в `.env` нужно использовать эти же имена.

### 3. Собрать backend-артефакты

Из корня репозитория:

```bash
./gradlew clean build -x test
```

Эта команда соберёт `.jar` файлы для:

- `account-service`
- `task-management-service`
- `mail-service`
- `gateway-service`
- `registry`
- `config-service`

### 4. Запустить проект через Docker Compose

```bash
docker compose up --build
```

При первом запуске Docker:

- соберёт backend-образы из готовых `.jar`
- соберёт frontend-образ
- поднимет PostgreSQL, Redis и Kafka
- запустит Config Server, Eureka, Gateway, backend-сервисы и frontend

### 5. Открыть приложение

После старта контейнеров основные точки входа будут такими:

- Frontend: [http://localhost](http://localhost)
- API Gateway: [http://localhost:8080](http://localhost:8080)
- Eureka Dashboard: [http://localhost:8761](http://localhost:8761)

### 6. Остановить проект

```bash
docker compose down
```

Чтобы удалить ещё и volumes:

```bash
docker compose down -v
```

## Важные детали

- frontend проксирует `/api/*` запросы в `gateway-service`
- конфигурация Spring-сервисов централизована в `service-configs/`
- в текущей конфигурации `config-service` настроен на получение конфигов из GitHub-репозитория, поэтому при запуске понадобится доступ в интернет
- отправка email использует Gmail SMTP-настройки в `mail-service`
