# 1. Use SecureRandom instead of java.util.Random

## Status

Accepted

## Context 

The RNG Service generates the numbers that decide real-money game outcomes 
(scratch card symbols, prizes). 
In a gambling platform, these outcomes must be **unpredictable** and the 
generator must be **certifiable** by regulators (e.g. GLI).

Java Offers two options:

- `java.util.Random` - a linear congruential generator. Fast, but **predictable**:
observing a few outputs (or knowing the seed) lets an attacker compute future values.
- `java.security.SecureRandom` - a cryptographically strong generator, 
seeded from OS entropy, designed to be infeasible to predict even after
observing many outputs.

## Decision 

Use **`SecureRandom`**, with **no manual seed** - let it self-seed from OS entropy.

## Consequences

**Positive**
- Outputs are unpredictable, meeting the security bar for real-money gaming.
- Certifiable for regulatory review (SecureRandom is a recognized primitive).

**Negative / trade-offs**
- Slightly slower than `java.util.Random` (negligible at our request volume).
- Non-reproducible by design - for deterministic tests we would need 
a separate test-only generator (see potential future ADR)
