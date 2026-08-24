package dev.gaphunter.ratelimiterfallbackcompanion.detect

/**
 * Resilience4j annotations that support a `fallbackMethod` attribute --
 * matched by simple annotation name only, so it works whether the real
 * Resilience4j jar is on the classpath or not.
 */
object Resilience4jAnnotations {
    val WITH_FALLBACK = setOf("RateLimiter", "CircuitBreaker", "Retry", "Bulkhead", "TimeLimiter")
}
