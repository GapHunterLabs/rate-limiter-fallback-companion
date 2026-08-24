# Demo data for screenshots

`OrderService.java` — `process` references `handleRateLimit`, which
doesn't exist (flagged). `processSafely` references
`handleLimitExceeded`, which does exist (not flagged).

## How to get the screenshot

1. `./gradlew runIde` from `rate-limiter-fallback-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `OrderService.java` — a warning icon should
   appear on `process` but not on `processSafely`.
3. Screenshot with both methods visible, save into
   `rate-limiter-fallback-companion/docs/screenshots/`. Close the
   sandbox.
