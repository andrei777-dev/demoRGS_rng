# RNG Service

Secure random number generator for the demoRGS RGS platform.
Game engines call this service to produce fair, unpredictable outcomes.

## What it does

- Generates cryptographically secure random **integers** (within a range) and **doubles** (`[0, 1)`).
- **Stateless** - no database, no shared state. Scales horizontally.
- Self-checks output fairness with a **chi-square uniformity test**, exposed as a metric (regulatory requirement for gambling).

## Tech stack

- Java 25, Spring Boot 4.1
- Maven
- REST (gRPC planned - future migration)
- springdoc-openapi (Swagger UI)
- Micrometer + Prometheus (metrics)
- Apache Commons Math (chi-square)
- JUnit 5, Mockito, Spring MockMvc (tests)

## Consumers

| Caller | Uses |
|--------|------|
| Scratch Card Engine | `/integers`, `/doubles` to pick symbols / outcomes |

## API

Base path: `/api/v1/rng` · Port: `7772`

| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| POST | `/integers` | `{ "min": 0, "max": 9, "count": 10 }` | list of integers in `[min, max]` |
| POST | `/doubles`  | `{ "count": 5 }` | list of doubles in `[0, 1)` |

`count` must be between 1 and 10,000.

Invalid input (`count <= 0`, `count > 10000`, `min > max`, malformed JSON) → `400 Bad Request`.
All errors (400 / 404 / 405 / 415 / 500) return the same `ApiError` body:
`timestamp`, `status`, `error`, `message`, `path`.

**Interactive docs (Swagger UI):** http://localhost:7772/swagger-ui/index.html
**OpenAPI spec:** http://localhost:7772/v3/api-docs

## Running locally

```bash
./mvnw spring-boot:run
```

## Running tests

```bash
./mvnw test
```

Fast tests only (skips the 1M-draw statistical tests):

```bash
./mvnw test -DexcludedGroups=statistical
```

See [docs/TESTING.md](docs/TESTING.md) for what each test class covers.

## Monitoring

- Health: http://localhost:7772/actuator/health
- Liveness / readiness (used by K8s probes): `/actuator/health/liveness`, `/actuator/health/readiness`
- Metrics (Prometheus): http://localhost:7772/actuator/prometheus
- Chi-square failures counter: `rng_chisquare_failures`

## Configuration

All settings live in `src/main/resources/application.yaml`.

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `7772` | Port for REST endpoints and Actuator |
| `rng.chi-square.significance` | `0.05` | Significance level of the runtime chi-square test; must be in `(0, 0.5]` |
| `rng.chi-square.check-interval` | `100000` | Draws per range before a chi-square test runs; must be `> 0` |
| `logging.level.com.demorgs.rng` | `DEBUG` | Log level for this service (use `INFO` in production) |

Invalid `rng.chi-square.*` values stop the app at startup with a clear error.
Any property can be overridden at startup, e.g. `--rng.chi-square.check-interval=1000`.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - where the service fits in the platform
- [docs/FAIRNESS.md](docs/FAIRNESS.md) - RNG method, evidence and limitations
- [docs/TESTING.md](docs/TESTING.md) - test strategy
- [docs/adr/](docs/adr/) - architecture decision records

## Notes

- No secrets or credentials — nothing sensitive to configure.
- gRPC is planned for internal calls; REST is the current interface.
