# 💰 AI-Powered Personal Finance Tracker

A production-ready Spring Boot REST API that helps users track income and expenses,
and get **personalised AI financial advice** powered by OpenAI GPT — delivered
automatically every month via email.

---

## 🧠 The Real Problem This Solves

Most people have no idea where their salary goes every month.
Budgeting apps just show you charts — they don't *tell* you what to do differently.

This app tracks your spending and at the end of every month, an AI analyses your
categories (food, rent, transport, etc.) and gives you **specific, personalised saving tips**
like: *"You spent 40% more on food this month. Consider meal prepping 3 days a week."*

---

## ✅ Non-Negotiable Skills This Project Covers

| Skill | Where It's Used |
|---|---|
| **REST API Design** | Clean endpoints with proper HTTP methods and status codes |
| **Spring Security + JWT** | Login/register with token-based auth |
| **Spring Data JPA** | Entity relationships, JPQL queries, repositories |
| **Exception Handling** | Global `@ControllerAdvice` with clean error responses |
| **LLM / AI API Integration** | OpenAI GPT called from Java using WebClient |
| **Spring Scheduler** | Auto-generates summaries on the 1st of every month |
| **JavaMailSender** | Sends HTML email reports to users |
| **Docker** | Fully containerised with Docker Compose |
| **Swagger / OpenAPI** | Auto-generated interactive API docs |
| **Role-Based Access Control** | USER vs ADMIN roles |

---

## 🏗️ Architecture

```
HTTP Request
    ↓
Controller  (handles HTTP, calls service)
    ↓
Service     (business logic, calls repository + OpenAI)
    ↓
Repository  (JPA — reads/writes to PostgreSQL)
    ↓
PostgreSQL Database

Background:
Scheduler → SummaryService → OpenAiService → EmailService
```

---

## 📁 Project Structure

```
finance-tracker/
├── src/main/java/com/financetracker/
│   ├── FinanceTrackerApplication.java     ← Main entry point
│   ├── controller/
│   │   ├── AuthController.java            ← Register / Login
│   │   ├── TransactionController.java     ← CRUD for transactions
│   │   └── SummaryController.java         ← AI summaries + Admin
│   ├── service/
│   │   ├── AuthService.java               ← Register/login logic
│   │   ├── TransactionService.java        ← Transaction business logic
│   │   ├── SummaryService.java            ← Generates AI summaries
│   │   ├── OpenAiService.java             ← Calls OpenAI GPT API
│   │   └── EmailService.java              ← Sends HTML emails
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── TransactionRepository.java
│   │   └── MonthlySummaryRepository.java
│   ├── model/
│   │   ├── User.java                      ← Users table
│   │   ├── Transaction.java               ← Transactions table
│   │   ├── MonthlySummary.java            ← Summaries table
│   │   ├── Role.java                      ← USER / ADMIN
│   │   ├── TransactionType.java           ← INCOME / EXPENSE
│   │   └── Category.java                  ← FOOD, RENT, etc.
│   ├── config/
│   │   ├── SecurityConfig.java            ← Spring Security setup
│   │   ├── JwtUtil.java                   ← JWT generate/validate
│   │   ├── JwtAuthFilter.java             ← Intercepts requests
│   │   ├── SwaggerConfig.java             ← API docs config
│   │   └── WebClientConfig.java           ← HTTP client bean
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java    ← Catches all exceptions
│   │   ├── ResourceNotFoundException.java
│   │   └── BusinessException.java
│   └── scheduler/
│       └── MonthlySummaryScheduler.java   ← Auto runs monthly
├── src/main/resources/
│   └── application.properties             ← All configuration
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## 🚀 How to Run Locally (Step by Step)

### Prerequisites
You need these installed:
- Java 17 → https://adoptium.net
- Maven → https://maven.apache.org/download.cgi
- PostgreSQL → https://www.postgresql.org/download
- Git → https://git-scm.com

---

### Step 1 — Clone the project
```bash
git clone <your-repo-url>
cd finance-tracker
```

---

### Step 2 — Create the PostgreSQL database

Open pgAdmin or psql and run:
```sql
CREATE DATABASE financedb;
```
(The tables are auto-created by Spring Boot on first run.)

---

### Step 3 — Configure application.properties

Open `src/main/resources/application.properties` and update:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/financedb
spring.datasource.username=postgres          ← your PostgreSQL username
spring.datasource.password=postgres          ← your PostgreSQL password

# OpenAI API key
openai.api.key=sk-proj-xxxxxxxxxxxx          ← from platform.openai.com

# Gmail (for email notifications)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password ← NOT your Gmail password, see below
```

**How to get Gmail App Password:**
1. Go to myaccount.google.com
2. Security → 2-Step Verification → App Passwords
3. Create one called "Finance Tracker"
4. Paste that 16-character password here

---

### Step 4 — Run the app
```bash
mvn spring-boot:run
```

You'll see:
```
Started FinanceTrackerApplication in 3.2 seconds
```

The API is now running at **http://localhost:8080**

---

### Step 5 — Open Swagger UI

Go to **http://localhost:8080/swagger-ui.html**

You can test all endpoints directly in the browser here — no Postman needed!

---

## 🐳 Run with Docker (Alternative)

```bash
# Build and start everything (app + PostgreSQL)
docker-compose up --build

# Stop
docker-compose down
```

---

## 🔌 API Endpoints

### Authentication (No token needed)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create new account |
| POST | `/api/auth/login` | Login, get JWT token |

### Transactions (JWT required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions` | Add income or expense |
| GET | `/api/transactions` | Get all transactions |
| GET | `/api/transactions?type=EXPENSE` | Filter by type |
| GET | `/api/transactions?category=FOOD` | Filter by category |
| GET | `/api/transactions/{id}` | Get one transaction |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Summary (JWT required)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/summary` | Get this month's AI summary |
| GET | `/api/summary?month=5&year=2025` | Get specific month's summary |
| GET | `/api/summary/all` | Get all past summaries |

### Admin (ADMIN role required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/trigger-summary` | Manually trigger AI summary |

---

## 🔐 How to Use JWT in Swagger UI

1. Call `POST /api/auth/register` to create an account
2. Call `POST /api/auth/login` to get your token
3. Click **Authorize** button (top right of Swagger page)
4. Paste your token → click Authorize
5. Now all protected endpoints work!

---

## 📦 Transaction Categories

**Expense categories:** FOOD, RENT, TRANSPORT, ENTERTAINMENT, SHOPPING, HEALTH, EDUCATION, UTILITIES, OTHER

**Income categories:** SALARY, FREELANCE, INVESTMENT, GIFT

---

## 💡 How the AI Part Works

1. User logs transactions throughout the month
2. On the 1st of each month, the scheduler auto-runs
3. It calculates: total income, total expenses, savings, category breakdown
4. It sends this data to OpenAI GPT with a prompt like:
   ```
   User spent ₹8,000 on FOOD, ₹12,000 on RENT, ₹2,000 on TRANSPORT.
   Total income: ₹45,000. Savings: ₹23,000 (51%).
   Give personalised financial advice.
   ```
5. GPT returns specific saving tips
6. The summary + AI advice is saved to DB and emailed to the user

**You can also trigger this manually** via `POST /api/admin/trigger-summary` for testing.

---

## 📝 Resume Bullet Point

> Developed an AI-powered personal finance REST API using Spring Boot 3 with JWT authentication, role-based access control, Spring Data JPA with PostgreSQL, OpenAI GPT integration for personalised monthly saving advice, automated email reports via JavaMailSender, background job scheduling with @Scheduled, and Dockerized deployment with Docker Compose.

---

## 🧰 Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3 | Application framework |
| Spring Security | Authentication & authorization |
| JWT (jjwt) | Token-based auth |
| Spring Data JPA | Database ORM |
| PostgreSQL | Relational database |
| OpenAI API (GPT-4o-mini) | AI financial advice |
| WebClient | HTTP client for API calls |
| JavaMailSender | Email notifications |
| Spring Scheduler | Background jobs |
| Springdoc OpenAPI | Swagger API docs |
| Lombok | Reduce boilerplate |
| Docker + Docker Compose | Containerisation |
