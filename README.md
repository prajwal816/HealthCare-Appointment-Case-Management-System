# CIMHANS — Mental Health Appointment & Case Management System

> **Production-grade full-stack healthcare platform** for psychological hospitals and mental health institutions.

[![CI/CD](https://github.com/your-org/cimhans/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/your-org/cimhans/actions)
![Java](https://img.shields.io/badge/Java-17-007396?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

---

## 📋 Overview

CIMHANS is an enterprise-grade mental health management system built with:
- **Spring Boot 3.2** backend with JWT authentication, RBAC, and AES-256 encrypted session notes
- **React 18 + Tailwind CSS** frontend with role-based dashboards
- **PostgreSQL 16** with Flyway migrations
- **Redis 7** caching with per-cache TTL strategies
- **Docker Compose** for one-command deployment
- **GitHub Actions** CI/CD pipeline

---

## 🏗️ Architecture

```
Internet → Nginx (80/443) → Spring Boot API (:8080) → PostgreSQL + Redis
                          → React SPA (static files)
```

## 👥 Roles

| Role | Access |
|------|--------|
| `ADMIN` | Full system access, analytics, user management |
| `PSYCHIATRIST` | Clinical notes, own patients/schedule |
| `PSYCHOLOGIST` | Clinical notes, own patients/schedule |
| `RECEPTIONIST` | Patient registration, appointment booking |
| `PATIENT` | Own appointments and records |

---

## 🚀 Quick Start (Local)

### Prerequisites
- Docker Desktop ≥ 24.x
- JDK 17 (for local backend dev)
- Node.js 20 (for local frontend dev)

### 1. Clone & Configure
```bash
git clone https://github.com/your-org/cimhans.git
cd cimhans
cp .env.example .env
# Edit .env and set your secrets
```

### 2. Run with Docker Compose
```bash
docker compose up -d
```

Services will start at:
| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| API | http://localhost/api/v1 |
| Swagger UI | http://localhost/swagger-ui.html |
| Actuator | http://localhost:8080/actuator/health |

### 3. Local Development (without Docker)

**Backend:**
```bash
# Start only infrastructure
docker compose up postgres redis -d

cd mental-health-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend:**
```bash
cd mental-health-frontend
npm install
npm run dev
# Visit http://localhost:5173
```

---

## 🔐 Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@cimhans.com | Admin@123 |

> ⚠️ Change all credentials immediately in production.

---

## 📡 Key API Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/login` | Public | Login |
| POST | `/api/v1/auth/refresh` | Public | Refresh tokens |
| POST | `/api/v1/auth/logout` | Auth | Logout |
| GET | `/api/v1/patients` | Staff | List patients |
| POST | `/api/v1/patients` | Admin/Reception | Register patient |
| GET | `/api/v1/patients/search?q=` | Staff | Search patients |
| POST | `/api/v1/appointments` | Staff/Patient | Book appointment |
| PATCH | `/api/v1/appointments/{id}/confirm` | Staff | Confirm |
| PATCH | `/api/v1/appointments/{id}/complete` | Therapist | Complete |
| PATCH | `/api/v1/appointments/{id}/cancel` | All | Cancel |
| POST | `/api/v1/session-notes/appointment/{id}` | Therapist | Create note |
| GET | `/api/v1/session-notes/appointment/{id}` | Therapist/Admin | Get note |
| GET | `/api/v1/admin/dashboard/stats` | Admin | Analytics |

---

## 🗄️ Database Schema

```
users ──────────────┬── patients ─── appointments ── session_notes
                    └── therapists ──┤               
                                     └── appointment_slots
audit_logs (append-only)
refresh_tokens
notifications
documents
```

---

## 🧪 Testing

```bash
# Backend unit + integration tests
cd mental-health-backend
./mvnw test

# With coverage report
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

## 🐳 Docker Commands

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f backend

# Restart a service
docker compose restart backend

# Stop everything
docker compose down

# Stop and remove volumes (⚠️ destroys data)
docker compose down -v
```

---

## 🔒 Security Features

- JWT access tokens (15 min) + refresh token rotation (7 days)
- BCrypt-12 password hashing
- AES-256-CBC session note encryption with random IV per write
- Method-level RBAC via `@PreAuthorize`
- Rate limiting on auth endpoints (Bucket4j)
- SQL injection prevention (JPA parameterized queries)
- CORS whitelist configuration
- Soft delete on all entities (no hard deletes)
- Immutable audit log table
- Optimistic locking on appointments and session notes

---

## 📁 Project Structure

```
cimhans/
├── mental-health-backend/          # Spring Boot API
│   ├── src/main/java/com/cimhans/
│   │   ├── config/                 # Security, Redis, Swagger
│   │   ├── controller/v1/          # REST controllers
│   │   ├── domain/entity/          # JPA entities
│   │   ├── domain/enums/           # Enums
│   │   ├── dto/                    # Request/Response DTOs
│   │   ├── exception/              # Global error handling
│   │   ├── repository/             # Spring Data JPA
│   │   ├── scheduler/              # Reminder jobs
│   │   ├── security/               # JWT, filters, RBAC
│   │   └── service/                # Business logic
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/           # Flyway SQL scripts
├── mental-health-frontend/         # React + Vite + Tailwind
│   └── src/
│       ├── api/                    # Axios + services
│       ├── components/layout/      # Sidebar, Layout
│       ├── pages/                  # Role-based pages
│       ├── router/                 # Protected routes
│       └── store/                  # Zustand auth store
├── nginx/                          # Reverse proxy config
├── .github/workflows/              # GitHub Actions CI/CD
├── docker-compose.yml
└── .env.example
```

---

## 🌐 Production Deployment (VPS)

```bash
# On your VPS
mkdir -p /opt/cimhans && cd /opt/cimhans
cp docker-compose.yml .env .
nano .env  # Set all secrets

# Pull and start
docker compose pull
docker compose up -d

# Setup Let's Encrypt SSL (optional)
apt install certbot
certbot certonly --standalone -d yourdomain.com
# Update nginx.conf with SSL paths
```

---

## 📄 License

Private — For educational/internship demonstration purposes.

---

*Built with ❤️ for mental health professionals.*
