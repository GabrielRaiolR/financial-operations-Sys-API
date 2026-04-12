# Financial Operations System

**REST** backend built with **Spring Boot** for **financial order** management in a business context (accounts payable/receivable–style flows, approval states, organization isolation). Intended as a **portfolio** and learning project—**junior** level—with common enterprise API patterns (multi-tenant via JWT, messaging, migrations, external FX with cache and resilience).

---

## What the system does

Several **companies** share one API without seeing each other’s data: each authenticated user belongs to a **`Company`**, and **`companyId`** is carried in the **JWT**. On top of that:

- **Financial orders** (`FinancialOrder`): create, paginated list (optional **`status`** filter), and detail for authenticated users; **approve** and **reject** **pending** orders are **ADMIN-only** (**FINANCE** gets **403** on those endpoints—separation of duties).
- **Users**: public **register** bootstraps **company + first ADMIN**; **login**; **ADMIN** CRUD for users in the same tenant (*soft delete*; passwords never returned).
- **Companies**: additional creation via API (beyond register bootstrap); **GET** by id scoped to the JWT tenant.
- **FX**: **reference rate** lookup (external API with **Caffeine** cache and **Resilience4j**).

---

## Implemented areas (by layer)

### Domain and persistence

- Entities **`Company`** (includes **`autoApprovalLimit`** in the schema for a future auto-approval policy; **order creation currently always starts as `PENDING`**—the limit is not applied in service logic yet), **`User`** (email, BCrypt hash, `Role`, company link, **`deletedAt`** for soft delete), **`FinancialOrder`** (type, amount, description, workflow status).
- **Spring Data JPA** repositories scoped by **`company_id`** (and active users where relevant).
- **Flyway**: `V1__initial_schema.sql` (base schema), `V2__users_soft_delete.sql` (`deleted_at` and supporting index).

### Security and authentication

- **`POST /auth/login`**: validates credentials, issues **JWT** (JJWT) with **`sub`** (user id), **`companyId`**, **`role`**.
- **`POST /auth/register`**: public; creates **`Company`** + first **`User`** as **ADMIN**, returns token (organization bootstrap).
- **OAuth2 Resource Server**: `Authorization: Bearer …` for protected routes; public: **`/auth/login`**, **`/auth/register`**, **`/actuator/health`**.
- **BCrypt** for passwords; responses never include hash or plaintext password.

### Multi-tenant

- Services resolve the tenant with **`CurrentUserService`** from the **JWT** (e.g. order creation does not trust client-supplied `companyId`).
- Order and user queries use the **same** `companyId` as the token.

### Financial order workflow

- **`POST /financial-orders`**: creates an order in **`PENDING`**.
- **`GET /financial-orders`**: paginated list, optional **`status`**, always within the token’s company.
- **`GET /financial-orders/{id}`**: detail with tenant isolation.
- **`POST /financial-orders/{id}/approve`** and **`POST /financial-orders/{id}/reject`**: **`PENDING` → `APPROVED` / `REJECTED`** with **`@PreAuthorize("hasRole('ADMIN')")`**; reject accepts an optional JSON body (`reason`) on **`RejectFinancialOrderRequest`**.

### User administration

- **`/users`**: **`ROLE_ADMIN` only**—paginated list, detail, **POST** create, **PATCH** update, **DELETE** soft-deletes (`deleted_at`).
- Rules include: unique email among active users; cannot remove the **last ADMIN** or **your own** account through that flow; soft-deleted users cannot log in.

### Messaging (RabbitMQ)

- After a **successful transaction commit** on order creation, a domain event is published (**`@TransactionalEventListener(phase = AFTER_COMMIT)`** so nothing is sent if the transaction rolls back).
- Exchange/queue/binding names are centralized (e.g. **`RabbitMqNames`**).
- **`FinancialOrderAmqpListener`** logs consumed messages (example async consumer).

### Error handling

- **`GlobalExceptionHandler`** + **`ApiError`**: consistent JSON for validation (**400** with `fieldErrors`), **401**, **403**, and business errors often mapped via **`IllegalArgumentException`** / **`IllegalStateException`** (room to evolve toward **404**/**409** later).

### FX integration (Frankfurter)

- **`GET /fx/rate`**: query params **`from`** / **`to`**.
- **RestClient**, **Caffeine** cache (TTL from config), **Resilience4j** (circuit breaker + retry) for the external **`app.fx.frankfurter-base-url`** (default `https://api.frankfurter.app`).

### Observability

- **Correlation id** filter (header → **MDC**; console pattern uses `%X{correlationId}`).
- **Actuator**: **`health`** exposed (suitable for probes).

### Automated tests

- Unit/integration tests where present (e.g. FX service); use Maven below.

---

## Code layout (packages)

```
com.api.financial_operations_system
├── config          # Security, JWT decoder, RabbitMQ, FX client, correlation id
├── controller      # REST
├── domain          # Entities and enums
├── dto             # Requests/responses and errors
├── events          # Domain events
├── exception       # GlobalExceptionHandler
├── integration     # External HTTP (FX)
├── messaging       # AMQP publish/consume
├── repository      # Spring Data
├── security        # JWT issuance
└── service         # Business logic
```

---

## Tech stack

| Technology | Role |
|------------|------|
| Java **21** | Language |
| Spring Boot **3.5.13** | Web, JPA, Security, Validation, Actuator, AMQP, Cache, AOP |
| PostgreSQL | Database |
| Flyway | Versioned migrations |
| RabbitMQ | Queues (post-commit events) |
| JJWT **0.12.6** | JWT building on login/register |
| Caffeine | FX quote cache |
| Resilience4j | Circuit breaker + retry on FX client |
| Lombok | Less boilerplate on entities/DTOs |

---

## Prerequisites

- **JDK 21** and **Maven** (or **`./mvnw`** / **`mvnw.cmd`**).
- **PostgreSQL** reachable with a database and user matching your config (defaults below).
- **RabbitMQ** (defaults in `application.properties`, or override via env). A **`docker-compose.yml`** in the repo starts **RabbitMQ 3** with the management UI (ports **5672** / **15672**) and user **`finops`** / password **`finops_dev_only`**.

---

## How to run

From the project root:

```bash
mvn spring-boot:run
```

Or run **`FinancialOperationsSystemApplication`** from your IDE.

### Useful configuration

| Variable / property | Purpose |
|---------------------|---------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL JDBC URL and credentials (defaults: `jdbc:postgresql://localhost:5432/finops`, `finops_user`, `finops_pass`) |
| `JWT_SECRET` | HMAC secret (use a strong value in production, ≥32 characters; never commit real secrets) |
| `JPA_DDL_AUTO` | Prefer **`validate`** in production when Flyway owns the schema |
| `APP_SECURITY_USER`, `APP_SECURITY_PASSWORD` | In-memory user bean (see `SecurityConfiguration`; not used by JWT business flows) |

Optional: start RabbitMQ for local dev:

```bash
docker compose up -d
```

### Tokens in JSON (Postman / HTTP client)

- **Login** returns **`acessToken`** (field name as in `LoginResponse`—typo in code).
- **Register** returns **`accessToken`**.
- Protected routes: header **`Authorization: Bearer <token>`**.

---

## API — route summary

| Area | Main routes |
|------|-------------|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Users | `GET` / `POST` / `PATCH` / `DELETE` `/users` (ADMIN; tenant from JWT) |
| Orders | `POST` / `GET` `/financial-orders`, `GET /financial-orders/{id}`, `POST .../{id}/approve`, `POST .../{id}/reject` |
| Companies | `POST /companies`, `GET /companies/{id}` (id must match JWT company) |
| FX | `GET /fx/rate?from=&to=` |
| Health | `GET /actuator/health` |

**Manual testing note:** each **`POST /auth/register`** creates a **new** company. To add users to an **existing** company, **log in** as that company’s **ADMIN** and use **`POST /users`** (the new user shares the token’s `companyId`).

**Suggested Postman order:** `GET /actuator/health` → `POST /auth/login` as **ADMIN** (or `register` for a new tenant) → authenticated calls → `GET /users` → `POST /users` (e.g. **FINANCE**) → with a **FINANCE** token: create/list orders (approve/reject should return **403**) → switch back to **ADMIN** for approve/reject → `GET /fx/rate` → negative cases (no token, duplicate email, soft delete + failed login).

---

## Tests

```bash
mvn test
```

---

## Limitations and next steps

- Some “not found” or conflict cases still surface as generic **400** responses instead of dedicated **404**/**409**—natural evolution with specific exceptions and mapping.
- No dedicated password change or “forgot password” flow yet.
- The **in-memory** user (`APP_SECURITY_*` in `SecurityConfiguration`) is configuration legacy; business APIs rely on **JWT** users from the database.
- **`autoApprovalLimit`** is stored but not yet enforced when creating orders.

---

## License

Portfolio / learning project; add an explicit license if you publish it elsewhere.
