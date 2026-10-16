package ua.kpi.comsys.dop;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Невелика демонстрація всіх чотирьох станів. */
public class Main {

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        OrderStatus[] statuses = {
                new Pending(),
                new Paid("PAY-1024", new BigDecimal("250.75")),
                new Shipped("TRK-7788", LocalDate.of(2026, 10, 16)),
                new Cancelled("Customer changed their mind")
        };

        for (OrderStatus status : statuses) {
            System.out.println(processor.process(status));
        }
    }
}
