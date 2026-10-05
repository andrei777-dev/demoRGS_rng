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

- Every test increments `rng_chisquare_checks` and stores its p-value in
  `rng_chisquare_last_pvalue` (close to 0 = suspicious).
- A rejected test (p-value below the significance level) increments
  `rng_chisquare_failures` and logs a WARN.
- All three are exposed via `/actuator/prometheus`. For a fair RNG,
  failures / checks stays close to the significance level (about 5%).

**In tests:** `SecureRandomUniformityTest` runs the same chi-square test on
1,000,000 real draws for several integer ranges and for doubles (split into 10 buckets).
See [Test Documentation](TESTING.md) for the method and thresholds.

## Certification / Compliance Status

- **Not certified.** Just a demo.

## Known Limitations

- The runtime monitor checks **integers only**. Doubles are checked by the statistical
  tests, but not on live traffic (see the TODO in `RngService`).
- Ranges wider than **10,000** values (`ChiSquareMonitor.MAX_RANGE`) are not checked at
  runtime: the histogram would be too large and each value would get too few hits
  for a valid chi-square test.
- At significance `0.05`, a perfectly fair RNG still fails about 5% of runtime checks
  by chance, so a single failure is not proof of bias; a rising failure rate is.
- `SecureRandom`'s default algorithm depends on the host OS/JVM; a certified
  deployment would pin and document the exact algorithm.
- No tamper-evidence, audit logging, or seed-ceremony process (out of scope for a demo).
