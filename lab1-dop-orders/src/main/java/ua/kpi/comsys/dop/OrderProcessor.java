package ua.kpi.comsys.dop;

/** Бізнес-логіка: перетворення будь-якого допустимого стану на текстовий опис. */
public class OrderProcessor {

    public String process(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Sealed interface гарантує вичерпність усіх case без default.
        return switch (status) {
            case Pending pending -> """
                    Order Status: PENDING
                    Details: Order is currently pending processing.
                    """;
            case Paid paid -> """
                    Order Status: PAID
                    Details: Payment ID: %s, Amount: $%s
                    """.formatted(paid.paymentId(), paid.amount());
            case Shipped shipped -> """
                    Order Status: SHIPPED
                    Details: Tracking Code: %s, Dispatch Date: %s
                    """.formatted(shipped.trackingCode(), shipped.dispatchDate());
            case Cancelled cancelled -> """
                    Order Status: CANCELLED
                    Details: Reason: %s
                    """.formatted(cancelled.reason());
        };
    }
}
