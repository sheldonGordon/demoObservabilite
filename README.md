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

Au premier demarrage de PostgreSQL (base vide), les scripts d'init font les actions suivantes:

- creation de la table `film` via `postgres/init/01-create-film-table.sql`
- insertion automatique de 100 films de demo via `postgres/init/02-seed-film.sql`

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

Le frontend affiche d'abord une page de connexion JWT (credentials demo: `demo` / `demo`), puis charge la liste des films.

Frontend: `http://localhost:4200`
Backend API: `http://localhost:8080/api/hello`

Endpoints films:

- `GET /api/films` -> liste de tous les films (id, title, releaseYear, genre) (JWT requis)
- `GET /api/films/{id}` -> detail complet d'un film (JWT requis)
- `PUT /api/films/{id}` -> modification d'un film (JWT requis, payload partiel possible)
- `POST /api/logs/frontend` -> ingestion des logs frontend (sans JWT, payload valide requis)

Correlation frontend -> backend sur les APIs films:

- le frontend envoie `X-Trace-Id` (32 hex) et `X-Session-Id`
- le backend enrichit les logs des requetes `/api/films` et `/api/films/{id}` via MDC
- format log backend: `[trace_id,span_id,frontend_session_id]`
- si `X-Trace-Id` est absent/invalide, le backend genere un `trace_id` fallback et le renvoie dans `X-Resolved-Trace-Id`

Credentials de demo JWT:

- username: `demo`
- password: `demo`

Obtenir un token:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/token" -ContentType "application/json" -Body '{"username":"demo","password":"demo"}'
```

Endpoint securise:

- `GET /api/secure/hello` avec header `Authorization: Bearer <token>`
- `GET /api/films` avec header `Authorization: Bearer <token>`
- `GET /api/films/{id}` avec header `Authorization: Bearer <token>`
- `PUT /api/films/{id}` avec header `Authorization: Bearer <token>`

Payload attendu pour les logs frontend:

```json
{
  "traceId": "2ec7f640d9f5444a9e14dba3116e67af",
  "sessionId": "5a80b46b-5c14-4844-ac6e-6cd02fd4d49e",
  "level": "INFO",
  "event": "FILMS_LIST_SUCCESS",
  "message": "Liste des films chargee",
  "timestamp": "2026-05-20T08:25:03.550Z",
  "route": "/",
  "userAgent": "Mozilla/5.0 ...",
  "context": {
	"count": 100
  }
}
```

## Lancement complet avec Docker Compose

```powershell
docker compose up --build
```

Services exposes:

- frontend: `http://localhost:4200`
- backend: `http://localhost:8080`
- postgres: `localhost:5432`

Le module `batch` execute son job puis termine.

Traitement batch actuel:

- authentification sur l'API backend via JWT
- recuperation de tous les films via `GET /api/films`
- mise a jour aleatoire de `imdb_score` film par film via `PUT /api/films/{id}`
- propagation des headers `X-Trace-Id` et `X-Session-Id` sur les appels API du batch

## OpenTelemetry (logs uniquement)

Dans `docker-compose.yml`, `backend` et `batch` utilisent :

- `JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar`
- `OTEL_INSTRUMENTATION_LOGBACK_MDC_ENABLED=true`
- `OTEL_TRACES_EXPORTER=none`
- `OTEL_METRICS_EXPORTER=none`
- `OTEL_LOGS_EXPORTER=none`

Resultat attendu dans les logs :

- chaque ligne contient `trace_id` et `span_id` quand un contexte de trace existe
- les logs frontend incluent aussi `frontend_session_id` pour la correlation
- pas d'export de traces/metriques, seulement enrichissement des logs

## Build et tests Java

```powershell
mvn clean verify
```


