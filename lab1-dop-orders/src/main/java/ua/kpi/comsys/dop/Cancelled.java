package ua.kpi.comsys.dop;

/** Замовлення скасоване із зазначенням причини. */
public record Cancelled(String reason) implements OrderStatus {

    public Cancelled {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason must not be blank");
        }
    }
}
