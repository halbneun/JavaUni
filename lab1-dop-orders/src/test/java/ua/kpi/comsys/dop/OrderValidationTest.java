package ua.kpi.comsys.dop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidationTest {

    @Test
    void paidRejectsNullPaymentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Paid(null, new BigDecimal("5")));
    }

    @Test
    void paidRejectsBlankPaymentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Paid("  	 ", new BigDecimal("5")));
    }

    @Test
    void paidRejectsNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Paid("PAY-5", null));
    }

    @Test
    void paidRejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Paid("PAY-5", BigDecimal.ZERO));
    }

    @Test
    void paidRejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Paid("PAY-5", new BigDecimal("-0.01")));
    }

    @Test
    void shippedRejectsNullTrackingCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipped(null, LocalDate.of(2026, 10, 16)));
    }

    @Test
    void shippedRejectsBlankTrackingCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipped("  ", LocalDate.of(2026, 10, 16)));
    }

    @Test
    void shippedRejectsNullDispatchDate() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipped("TRACK-10", null));
    }

    @Test
    void cancelledRejectsNullReason() {
        assertThrows(IllegalArgumentException.class, () -> new Cancelled(null));
    }

    @Test
    void cancelledRejectsBlankReason() {
        assertThrows(IllegalArgumentException.class, () -> new Cancelled("  \n "));
    }

    @Test
    void validRecordsExposeTheirImmutableComponents() {
        Paid paid = new Paid("PAY-OK", new BigDecimal("10.00"));
        Shipped shipped = new Shipped("TRK-OK", LocalDate.of(2026, 10, 16));
        Cancelled cancelled = new Cancelled("Other");

        assertEquals("PAY-OK", paid.paymentId());
        assertEquals(new BigDecimal("10.00"), paid.amount());
        assertEquals("TRK-OK", shipped.trackingCode());
        assertEquals(LocalDate.of(2026, 10, 16), shipped.dispatchDate());
        assertEquals("Other", cancelled.reason());
    }
}
