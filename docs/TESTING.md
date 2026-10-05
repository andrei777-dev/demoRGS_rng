# Testing

> **Demo project.** These tests show the testing approach; they are not a certification test suite.

## How to run

| Command | Runs |
|---------|------|
| `mvn test` | everything (41 tests) |
| `mvn test -DexcludedGroups=statistical` | fast tests only (skips the 1M-draw tests) |
| `mvn test -Dgroups=statistical` | statistical tests only |

## Test types

| Type | Class | Tests | What it checks |
|------|-------|-------|----------------|
| Unit | `SecureRandomGeneratorTest` | 6 | values stay in `[min, max]`, every value is reachable, negative ranges, `min == max`, doubles in `[0, 1)`, no int overflow on the full int range |
| Unit (Mockito) | `RngServiceTest` | 8 | input validation (`count`, `min`/`max`, max count), returns the generated values, feeds every integer to the monitor, doubles not monitored |
| Unit | `ChiSquareMonitorTest` | 9 | uniform data passes, biased data fails, test runs exactly at the interval, histogram reset, ranges kept separate, single-value range skipped, full int range skipped without overflow, checks counter, last p-value |
| Unit | `ChiSquarePropertiesTest` | 3 | invalid settings are rejected |
| Spring context | `ChiSquarePropertiesBindingTest` | 1 | `rng.chi-square.*` is loaded from `application.yaml` |
| Unit | `GlobalExceptionHandlerTest` | 3 | 400 / 500 / Spring errors mapped to `ApiError`; internal messages never leak on 500 |
| Web slice (`@WebMvcTest`) | `RngControllerTest` | 6 | URL mapping, JSON in/out, 400 / 405 / 415 status codes |
| Statistical | `SecureRandomUniformityTest` | 4 | 1,000,000 real draws per test, chi-square goodness-of-fit |
| Smoke | `RngApplicationTests` | 1 | Spring context starts |

## Statistical method

- Real `SecureRandomGenerator`, **1,000,000 draws** per test.
- Ranges tested: `1..6`, `0..9`, `1..100`, and doubles split into 10 equal buckets.
- Chi-square goodness-of-fit against a uniform distribution (Apache Commons Math).
- Threshold: **p-value > 0.001**. A fair RNG fails about 0.1% of runs by chance;
  0.05 would fail about 5% of runs and make CI flaky.
- At runtime, `ChiSquareMonitor` repeats this check on live traffic (alpha `0.05`,
  every `100,000` draws per range, ranges up to `10,000` values) and exposes
  `rng_chisquare_checks`, `rng_chisquare_failures` and `rng_chisquare_last_pvalue`.

## Not covered yet

- Concurrency test for `ChiSquareMonitor` under many threads.
- Contract tests with the Scratch Card Engine.
- Performance / load tests.
- Certified statistical batteries (e.g. NIST SP 800-22, Dieharder).