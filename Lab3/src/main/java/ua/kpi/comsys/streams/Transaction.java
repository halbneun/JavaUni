package ua.kpi.comsys.streams;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Незмінна модель транзакції відповідно до методички ЛР №3. */
public record Transaction(
        String transactionId,
        String category,
        BigDecimal amount,
        LocalDateTime timestamp
) { }
