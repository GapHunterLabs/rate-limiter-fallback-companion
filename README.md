# Rate Limiter Fallback Companion

Gutter warning icon on any Java/Kotlin method annotated with a
Resilience4j resilience annotation (`@RateLimiter`,
`@CircuitBreaker`, `@Retry`, `@Bulkhead`, `@TimeLimiter`) whose
`fallbackMethod = "name"` doesn't match any method in the enclosing
class — a real, silent footgun: nothing fails at compile time, but
Resilience4j throws at runtime the first time the fallback is actually
triggered (rate limited, circuit open, retry exhausted), which can be
a rare code path that isn't exercised until production.

## Why it exists

`fallbackMethod` is a plain string, resolved by reflection at runtime
— a typo, a rename that missed this one reference, or a copy-pasted
annotation from a different class all produce code that compiles
fine and passes every test that doesn't actually trigger the fallback.
Nothing in the IDE flags it today.

## Why built this way

- **100% static text/PSI analysis** — matches the annotation and the
  referenced method name by simple text, so it works whether the real
  Resilience4j jar is on the classpath or not. Java and Kotlin.

## v0.1 scope — stated honestly, not exhaustively

Only checks that a method with the given *name* exists somewhere in
the class — it doesn't validate the fallback's parameter/return-type
signature actually matches what Resilience4j requires (same
parameters as the original method, optionally plus a trailing
Throwable, and a matching return type). A wrong-signature fallback
with the right name isn't covered (yet).

## Usage

Open any Java/Kotlin class using a Resilience4j annotation. A
`fallbackMethod` that doesn't match a real method shows a warning
icon on the annotated method's name.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
