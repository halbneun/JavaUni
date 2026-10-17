package ua.kpi.comsys.streams;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

/** Приклади трьох методів на нескінченному джерелі. */
public class Main {
    public static void main(String[] args) {
        TransactionStreamAnalyzer analyzer = new TransactionStreamAnalyzer();

        System.out.println("First amount > 250:");
        System.out.println(analyzer.findFirstHighValueTransaction(
                transactions(), new BigDecimal("250")));

        System.out.println("Transactions within budget 600:");
        System.out.println(analyzer.takeTransactionsUntilBudgetExceeded(
                transactions(), new BigDecimal("600")));

        System.out.println("Three batches of two transactions:");
        analyzer.processBatches(transactions(), 2, 3)
                .forEach(System.out::println);
    }

    private static Stream<Transaction> transactions() {
        return Stream.iterate(1, n -> n + 1)
                .map(n -> new Transaction(
                        "TX-" + n,
                        n % 2 == 0 ? "books" : "electronics",
                        new BigDecimal(n * 100L),
                        LocalDateTime.of(2026, 10, 17, 12, 0).plusMinutes(n)));
    }
}
