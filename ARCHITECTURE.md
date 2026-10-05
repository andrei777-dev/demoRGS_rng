# RNG Service - Architecture

## Overview

The RNG Service is a **stateless** service in the demoRGS RGS platform.
It produces cryptographically secure random numbers for game engines 
and verifies its own output fairness with a chi-square test. 
It has **no database** and calls **no other services** - it only receives requests and responds.

## Where it fits
```mermaid
graph LR
    SC[Scratch Engine] -- REST --> RNG[RNG Service]
    RNG -- reads --> ENT((OS Entropy))
    RNG -- exposes --> PM((Prometheus Metrics))

    classDef svc fill:#1565c0,color:#fff
    classDef external fill:#455a64,color:#fff
    classDef observability fill:#00695c,color:#fff

    class SC,RNG svc
    class ENT external
    class PM observability
```

## Dependencies
| Direction | Party | Purpose |
|-----------|-------|---------|
| Inbound | Scratch Card Engine | requests random integers / doubles |
| Internal | OS entropy (`SecureRandom`) | source of randomness |
| Outbound | — | none (no DB, no downstream services) |

## Typical data flow
1. Scratch Card Engine sends `POST /api/v1/rng/integers` (`min`, `max`, `count`).
2. RNG validates the input (`count` 1..10,000, `min <= max`) and generates the values via `SecureRandom`.
3. Each value is fed to the chi-square monitor (per-range histogram).
4. RNG returns the list of numbers.
5. On accumulated volume, the monitor runs the test; failures increment
   `rng_chisquare_failures` (scraped by Prometheus).

## Design properties
- **Stateless** → scales horizontally (each pod has its own `SecureRandom` + monitor).
- **No shared state** → no Redis; Prometheus aggregates per-pod metrics.
- **REST now**, gRPC planned for internal calls (to be added to all services at once).