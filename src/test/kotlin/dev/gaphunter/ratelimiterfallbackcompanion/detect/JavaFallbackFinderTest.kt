package dev.gaphunter.ratelimiterfallbackcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaFallbackFinderTest : BasePlatformTestCase() {

    fun `test a fallbackMethod that does not exist is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                @RateLimiter(name = "orders", fallbackMethod = "handleLimit")
                String process() {
                    return "ok";
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaFallbackFinder.findAll(file).size)
    }

    fun `test a fallbackMethod that exists in the class is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                @RateLimiter(name = "orders", fallbackMethod = "handleLimit")
                String process() {
                    return "ok";
                }

                String handleLimit(Throwable t) {
                    return "limited";
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaFallbackFinder.findAll(file).isEmpty())
    }

    fun `test a method with no fallbackMethod attribute is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                @RateLimiter(name = "orders")
                String process() {
                    return "ok";
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaFallbackFinder.findAll(file).isEmpty())
    }

    fun `test a non-resilience4j annotation is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                @Deprecated
                String process() {
                    return "ok";
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaFallbackFinder.findAll(file).isEmpty())
    }
}
