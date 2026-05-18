# demoObservabilite

Monorepo avec 3 modules :

- `frontend` : Angular 20
- `backend` : API Spring Boot 3
- `batch` : Spring Batch 5

## Architecture

- `frontend` appelle `backend` via `/api` (proxy Angular en dev)
- `backend` et `batch` utilisent PostgreSQL
- `backend` expose un endpoint public et un endpoint securise par JWT
- observabilite: logs applicatifs enrichis avec `trace_id` et `span_id` via OpenTelemetry Java agent

## Prerequis

- Java 21+
- Maven 3.8+
- Node.js 24.15.0
- npm 11.12.1
- Docker + Docker Compose (optionnel, recommande)

## Lancement local (sans Docker)

### 1) Demarrer PostgreSQL

Utilise une base nommee `demo_observabilite` avec:

- user: `demo`
- password: `demo`

### 2) Demarrer le backend

```powershell
cd backend
mvn spring-boot:run
```

### 3) Demarrer le batch

```powershell
cd batch
mvn spring-boot:run
```

### 4) Demarrer le frontend

Version recommandee front:

- `node -v` -> `v24.15.0`
- `npm -v` -> `11.12.1`

```powershell
cd frontend
npm install
npm start
```

Frontend: `http://localhost:4200`
Backend API: `http://localhost:8080/api/hello`

Credentials de demo JWT:

- username: `demo`
- password: `demo`

Obtenir un token:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/token" -ContentType "application/json" -Body '{"username":"demo","password":"demo"}'
```

Endpoint securise:

- `GET /api/secure/hello` avec header `Authorization: Bearer <token>`

## Lancement complet avec Docker Compose

```powershell
docker compose up --build
```

Services exposes:

- frontend: `http://localhost:4200`
- backend: `http://localhost:8080`
- postgres: `localhost:5432`

Le module `batch` execute son job puis termine.

## OpenTelemetry (logs uniquement)

Dans `docker-compose.yml`, `backend` et `batch` utilisent :

- `JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar`
- `OTEL_INSTRUMENTATION_LOGBACK_MDC_ENABLED=true`
- `OTEL_TRACES_EXPORTER=none`
- `OTEL_METRICS_EXPORTER=none`
- `OTEL_LOGS_EXPORTER=none`

Resultat attendu dans les logs :

- chaque ligne contient `trace_id` et `span_id` quand un contexte de trace existe
- pas d'export de traces/metriques, seulement enrichissement des logs

## Build et tests Java

```powershell
mvn clean verify
```


