package com.acmecorp.orders;

public class OrderService {

    // fallbackMethod references a method that doesn't exist -- flagged.
    @RateLimiter(name = "orders", fallbackMethod = "handleRateLimit")
    String process(String orderId) {
        return "processed " + orderId;
    }

    // fallbackMethod exists in this class -- not flagged.
    @RateLimiter(name = "orders", fallbackMethod = "handleLimitExceeded")
    String processSafely(String orderId) {
        return "processed " + orderId;
    }

    String handleLimitExceeded(String orderId, Throwable t) {
        return "rate limited";
    }
}
