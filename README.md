# Financial Operations System

Backend **REST** em Spring Boot para **gestão de ordens financeiras** em contexto empresarial (contas a pagar/receber, estados de aprovação, isolamento por organização). Projeto pensado para **portfolio** e aprendizagem — nível **júnior**, com foco em boas práticas comuns em APIs corporativas (multi-tenant, JWT, mensageria, migrações).

---

## Objetivo do sistema

Permitir que **várias empresas** usem a mesma API sem ver dados umas das outras: cada utilizador autenticado está ligado a uma **`Company`**, e o **`companyId`** vai no **JWT**. Sobre isso assentam:

- **Ordens financeiras** (`FinancialOrder`): criação e listagem filtrada por empresa e estado para utilizadores autenticados (**ADMIN** e **FINANCE**); **aprovação** e **rejeição** de ordens pendentes **apenas** com papel **ADMIN** (separação de funções: **FINANCE** opera ordens, **ADMIN** decide o fluxo de aprovação).
- **Utilizadores**: registo público que cria **empresa + primeiro admin**, **login**, e **CRUD administrativo** de users no mesmo tenant (*soft delete*, sem expor passwords nas respostas).
- **Empresas**: criação adicional via API (além do bootstrap do registo).
- **Câmbio**: endpoint de **consulta** a taxa de referência (integração externa com **cache** e **resiliência**).

---

## O que está implementado (visão por blocos)

### Domínio e persistência

- Entidades **`Company`** (incl. `autoApprovalLimit` para política de autoaprovação na criação de ordens), **`User`** (email, hash BCrypt, `Role`, vínculo à empresa, **`deletedAt`** para *soft delete*), **`FinancialOrder`** (tipo, valor, descrição, estado do fluxo).
- **Spring Data JPA** com repositórios que filtram por **`company_id`** (e por utilizador ativo onde aplicável).
- **Flyway**: `V1__initial_schema.sql` (esquema base), `V2__users_soft_delete.sql` (coluna `deleted_at` e índice útil para listagens).

### Segurança e autenticação

- **`POST /auth/login`**: valida credenciais, emite **JWT** assinado (JJWT) com **`sub`** (id do user), **`companyId`**, **`role`**.
- **`POST /auth/register`**: público; cria **nova** `Company` + primeiro **`User`** como **ADMIN** e devolve token (bootstrap de organização).
- **OAuth2 Resource Server**: pedidos autenticados com `Authorization: Bearer …`; rotas públicas: **`/auth/login`**, **`/auth/register`**, **`/actuator/health`**.
- **BCrypt** para passwords; respostas **não** devolvem hash nem password em claro.

### Multi-tenant

- Serviços críticos resolvem o tenant com **`CurrentUserService`** a partir do **JWT** (não confiam em `companyId` enviado pelo cliente em DTOs de criação de ordem).
- Consultas a ordens e a utilizadores respeitam o **mesmo** `companyId` do token; token de outra empresa **não** acede a recursos alheios.

### Workflow de ordens financeiras

- **`POST /financial-orders`**: cria ordem em **PENDING** (e aplica regra de autoaprovação quando o montante está dentro do limite da empresa).
- **`GET /financial-orders`**: listagem **paginada**, opcionalmente por **`status`**, sempre no âmbito da empresa do token.
- **`GET /financial-orders/{id}`**: detalhe com isolamento de tenant.
- **`POST .../{id}/approve`** e **`POST .../{id}/reject`**: transições **`PENDING` → `APPROVED` / `REJECTED`** com **`@PreAuthorize("hasRole('ADMIN')")`** — utilizadores **FINANCE** recebem **403** nestes endpoints.

### Utilizadores (administração)

- **`/users`**: apenas **`ROLE_ADMIN`** — listagem paginada, detalhe, criação (**POST**), atualização parcial (**PATCH**), remoção lógica (**DELETE** → preenche `deleted_at`).
- Regras: email único entre ativos; não desativar o **último ADMIN** da empresa nem **a própria** conta pelo mesmo fluxo; login não autentica utilizadores *soft deleted*.

### Mensageria (RabbitMQ)

- Após **commit** bem-sucedido da criação de ordem, publicação de evento de domínio (listener com **`@TransactionalEventListener(phase = AFTER_COMMIT)`** para não publicar se a transação falhar).
- Configuração de exchange/fila/binding no pacote de messaging (nomes centralizados).

### Tratamento de erros

- **`GlobalExceptionHandler`** + **`ApiError`**: respostas JSON consistentes para validação (**400** com `fieldErrors`), **401** (credenciais), **403** (acesso negado), **400** para vários erros de negócio mapeados via `IllegalArgumentException` (há espaço para evoluir para **404**/**409** mais explícitos no futuro).

### Integração FX (Frankfurter)

- **`GET /fx/rate`**: parâmetros `from` / `to`.
- **RestClient**, **cache Caffeine** (TTL configurável), **Resilience4j** (circuit breaker + retry em leituras) para não depender de falhas repetidas da API externa.

### Observabilidade

- **Filtro de correlation id** (header propagado ao **MDC**; padrão de log em consola com `%X{correlationId}`).
- **Actuator**: exposição mínima de **`health`** (adequado a probes).

### Testes automatizados

- Testes unitários/integração onde existem no módulo (ex.: serviços de FX); comando padrão Maven abaixo.

---

## Estrutura do código (pacotes)

```
com.api.financial_operations_system
├── config          # Security, JWT decoder, RabbitMQ, FX client, correlation id
├── controller      # REST
├── domain          # Entidades e enums
├── dto             # Pedidos/respostas e erros
├── exception       # GlobalExceptionHandler
├── integration     # Clientes HTTP externos (FX)
├── messaging       # Publicação/consumo AMQP
├── repository      # Spring Data
├── security        # Emissão de JWT
├── service         # Regras de negócio
└── events          # Eventos de domínio (quando aplicável)
```

---

## Stack técnica

| Tecnologia | Uso |
|------------|-----|
| Java **21** | Linguagem |
| Spring Boot **3.5.x** | Web, JPA, Security, Validation, Actuator, AMQP, Cache, AOP |
| PostgreSQL | Base de dados |
| Flyway | Migrações versionadas |
| RabbitMQ | Filas (eventos pós-commit) |
| jjwt | Construção do JWT no login/registo |
| Caffeine | Cache de cotações |
| Resilience4j | Circuit breaker + retry no cliente FX |
| Lombok | Redução de boilerplate nas entidades/DTOs |

---

## Pré-requisitos

- **JDK 21** e **Maven**
- **PostgreSQL** (ex.: base `finops`, utilizador com permissões nas tabelas da aplicação)
- **RabbitMQ** acessível (host/porta/credenciais alinhados ao `application.properties` ou variáveis de ambiente)

---

## Como executar

Na raiz do projeto:

```bash
mvn spring-boot:run
```

Ou executar a classe **`FinancialOperationsSystemApplication`** na IDE.

### Variáveis de ambiente úteis

| Variável | Descrição |
|----------|-----------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Ligação PostgreSQL |
| `JWT_SECRET` | Segredo HMAC (em produção: valor forte, ≥32 caracteres; **nunca** commitar segredo real) |
| `JPA_DDL_AUTO` | Em produção usar tipicamente **`validate`** quando Flyway governa o esquema |

### Token no JSON (Postman / cliente HTTP)

- **Login** devolve o campo **`acessToken`** (nome atual do `LoginResponse`).
- **Registo** devolve **`accessToken`**.
- Rotas protegidas: cabeçalho **`Authorization: Bearer <token>`**.

---

## API — resumo de rotas

| Área | Rotas principais |
|------|------------------|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Users | `GET/POST/PATCH/DELETE /users` (ADMIN; tenant do JWT) |
| Ordens | `POST/GET /financial-orders`, `POST .../{id}/approve`, `POST .../{id}/reject` |
| Empresas | `POST /companies` |
| FX | `GET /fx/rate?from=&to=` |
| Saúde | `GET /actuator/health` |

**Nota para testes manuais:** cada **`POST /auth/register`** cria uma **nova** empresa. Para adicionar colaboradores a uma empresa **já existente**, faz **login** como **ADMIN** dessa empresa e usa **`POST /users`** (o novo utilizador fica no mesmo `companyId` do token).

**Sugestão de ordem no Postman:** `GET /actuator/health` → `POST /auth/login` como **ADMIN** (ou `register` se quiseres um tenant novo) → pedidos autenticados → `GET /users` → `POST /users` (ex. FINANCE) → com token **FINANCE**: criar/listar ordens (aprovar/rejeitar deve falhar **403**) → voltar a **ADMIN** para `approve`/`reject` → `GET /fx/rate` → cenários negativos (sem token, email duplicado, *soft delete* + login falhado).

---

## Testes

```bash
mvn test
```

---

## Limitações e próximos passos (realistas)

- Alguns “não encontrado” ou conflitos de negócio ainda saem como **400** genérico em vez de **404**/**409** dedicados — melhoria natural com exceções específicas e handler.
- Mudança de password por endpoint dedicado / “esqueci a password” não faz parte do escopo atual.
- O utilizador **in-memory** (`APP_SECURITY_*` no `SecurityConfiguration`) é legado de configuração; a API real de negócio assenta no **JWT** dos utilizadores da base.

---

## Licença

Projeto de portfolio / aprendizagem; define licença explícita se publicares noutro contexto.
