# Fairness / Compliance

> **Demo project.** This service is a portfolio/learning demo.

## Method

Random numbers are generated with Java's `java.security.SecureRandom`,
a cryptographically strong PRNG seeded from operating-system entropy.
No manual seeding is used, so outputs are unpredictable and cannot be
reproduced from observed values.

Integers are produced with `secureRandom.nextLong(min, (long) max + 1)`,
which returns an unbiased value in `[min, max]`. The bound is computed as a `long`,
so even the full `int` range works without overflow. Doubles are returned in `[0, 1)`.

See [ADR 001](adr/001-securerandom-vs-random.md) for why `SecureRandom`
was chosen over `java.util.Random`.

## Evidence

**At runtime:** output uniformity is continuously self-checked with a
**chi-square goodness-of-fit test** (`ChiSquareMonitor`). Generated integers are
accumulated in a separate histogram per range (`min:max`). Once a range reaches
`rng.chi-square.check-interval` draws (default `100,000`), its observed distribution is
tested against a uniform expectation at significance level `rng.chi-square.significance`
(default `0.05`), and the histogram is reset.

- A rejected test (non-uniform output) increments the metric `rng_chisquare_failures`,
  exposed via `/actuator/prometheus`, and logs a WARN.

**In tests:** `SecureRandomUniformityTest` runs the same chi-square test on
1,000,000 real draws for several integer ranges and for doubles (split into 10 buckets).
See [Test Documentation](TESTING.md) for the method and thresholds.

## Certification / Compliance Status

- **Not certified.** Just a demo.

## Known Limitations

- The runtime monitor checks **integers only**. Doubles are checked by the statistical
  tests, but not on live traffic (see the TODO in `RngService`).
- At significance `0.05`, a perfectly fair RNG still fails about 5% of runtime checks
  by chance, so a single failure is not proof of bias; a rising failure rate is.
- `SecureRandom`'s default algorithm depends on the host OS/JVM; a certified
  deployment would pin and document the exact algorithm.
- No tamper-evidence, audit logging, or seed-ceremony process (out of scope for a demo).
