package dev.gaphunter.ratelimiterfallbackcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinFallbackFinderTest : BasePlatformTestCase() {

    fun `test a fallbackMethod that does not exist is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                @RateLimiter(name = "orders", fallbackMethod = "handleLimit")
                fun process(): String {
                    return "ok"
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinFallbackFinder.findAll(file).size)
    }

    fun `test a fallbackMethod that exists in the class is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                @RateLimiter(name = "orders", fallbackMethod = "handleLimit")
                fun process(): String {
                    return "ok"
                }

                fun handleLimit(t: Throwable): String {
                    return "limited"
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinFallbackFinder.findAll(file).isEmpty())
    }

    fun `test a method with no fallbackMethod attribute is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                @RateLimiter(name = "orders")
                fun process(): String {
                    return "ok"
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinFallbackFinder.findAll(file).isEmpty())
    }
}
