package ua.kpi.comsys.dop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void pendingProducesExactText() {
        String expected = """
                Order Status: PENDING
                Details: Order is currently pending processing.
                """;

        assertEquals(expected, processor.process(new Pending()));
    }

    @Test
    void paidProducesExactText() {
        String expected = """
                Order Status: PAID
                Details: Payment ID: PAY-01, Amount: $199.99
                """;

        assertEquals(expected, processor.process(new Paid("PAY-01", new BigDecimal("199.99"))));
    }

    @Test
    void shippedProducesExactText() {
        String expected = """
                Order Status: SHIPPED
                Details: Tracking Code: TRACK-42, Dispatch Date: 2026-10-16
                """;

        assertEquals(expected, processor.process(new Shipped("TRACK-42", LocalDate.of(2026, 10, 16))));
    }

    @Test
    void cancelledProducesExactText() {
        String expected = """
                Order Status: CANCELLED
                Details: Reason: Out of stock
                """;

        assertEquals(expected, processor.process(new Cancelled("Out of stock")));
    }

    @Test
    void nullStatusIsRejectedWithExactMessage() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> processor.process(null));
        assertEquals("Status cannot be null", exception.getMessage());
    }
}
