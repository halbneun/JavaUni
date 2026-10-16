package ua.kpi.comsys.dop;

import java.math.BigDecimal;

/** Замовлення оплачене; зберігається ідентифікатор платежу і сума. */
public record Paid(String paymentId, BigDecimal amount) implements OrderStatus {

    public Paid {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID must not be blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
