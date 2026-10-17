package ua.kpi.comsys.streams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStreamAnalyzerTest {
    private final TransactionStreamAnalyzer analyzer = new TransactionStreamAnalyzer();

    private static Transaction tx(int number, String amount) {
        return new Transaction("TX-" + number, "books", new BigDecimal(amount),
                LocalDateTime.of(2026, 10, 17, 12, 0).plusMinutes(number));
    }

    @Test
    void findsFirstGreaterThanThreshold() {
        Transaction result = analyzer.findFirstHighValueTransaction(
                Stream.of(tx(1, "50"), tx(2, "150"), tx(3, "200")),
                new BigDecimal("100")).orElseThrow();
        assertEquals("TX-2", result.transactionId());
    }

    @Test
    void equalityDoesNotCountAsHighValue() {
        assertEquals(Optional.of(tx(2, "101")),
                analyzer.findFirstHighValueTransaction(
                        Stream.of(tx(1, "100"), tx(2, "101")),
                        new BigDecimal("100")));
    }

    @Test
    void noMatchInFiniteStreamReturnsEmpty() {
        assertTrue(analyzer.findFirstHighValueTransaction(
                Stream.of(tx(1, "10"), tx(2, "20")),
                new BigDecimal("30")).isEmpty());
    }

    @Test
    void emptyStreamReturnsEmptyOptional() {
        assertTrue(analyzer.findFirstHighValueTransaction(
                Stream.empty(), BigDecimal.ZERO).isEmpty());
    }

    @Test
    @Timeout(2)
    void highValueTerminatesInfiniteStreamLazily() {
        AtomicInteger calls = new AtomicInteger();
        Optional<Transaction> result = analyzer.findFirstHighValueTransaction(
                Stream.generate(() -> {
                    int i = calls.incrementAndGet();
                    return tx(i, String.valueOf(i * 10));
                }), new BigDecimal("30"));
        assertEquals("TX-4", result.orElseThrow().transactionId());
        assertEquals(4, calls.get());
    }

    @Test
    void budgetStopsBeforeExceedingTransaction() {
        assertEquals(List.of(tx(1, "100"), tx(2, "150")),
                analyzer.takeTransactionsUntilBudgetExceeded(
                        Stream.of(tx(1, "100"), tx(2, "150"), tx(3, "100")),
                        new BigDecimal("300")));
    }

    @Test
    void budgetIncludesExactLimit() {
        assertEquals(List.of(tx(1, "150"), tx(2, "150")),
                analyzer.takeTransactionsUntilBudgetExceeded(
                        Stream.of(tx(1, "150"), tx(2, "150"), tx(3, "10")),
                        new BigDecimal("300")));
    }

    @Test
    void budgetReturnsEmptyWhenFirstWouldExceed() {
        assertTrue(analyzer.takeTransactionsUntilBudgetExceeded(
                Stream.of(tx(1, "5"), tx(2, "1")), new BigDecimal("4")).isEmpty());
    }

    @Test
    void emptyBudgetStreamReturnsEmptyList() {
        assertEquals(List.of(), analyzer.takeTransactionsUntilBudgetExceeded(
                Stream.empty(), BigDecimal.ZERO));
    }

    @Test
    @Timeout(2)
    void budgetTerminatesInfiniteStream() {
        AtomicInteger requested = new AtomicInteger();
        List<Transaction> result = analyzer.takeTransactionsUntilBudgetExceeded(
                Stream.generate(() -> tx(requested.incrementAndGet(), "10")),
                new BigDecimal("25"));
        assertEquals(List.of(tx(1, "10"), tx(2, "10")), result);
        assertEquals(3, requested.get()); // Третій елемент викликає зупинку.
    }

    @Test
    @Timeout(2)
    void batchesTerminateInfiniteStreamAtExactLimit() {
        AtomicInteger requested = new AtomicInteger();
        List<List<Transaction>> result = analyzer.processBatches(
                Stream.generate(() -> tx(requested.incrementAndGet(), "10")), 3, 2);
        assertEquals(2, result.size());
        assertEquals(List.of(tx(1, "10"), tx(2, "10"), tx(3, "10")), result.get(0));
        assertEquals(List.of(tx(4, "10"), tx(5, "10"), tx(6, "10")), result.get(1));
        assertEquals(6, requested.get());
    }

    @Test
    void batchReturnsPartialLastWindowForFiniteStream() {
        assertEquals(List.of(List.of(tx(1, "10"), tx(2, "20")),
                        List.of(tx(3, "30"))),
                analyzer.processBatches(Stream.of(tx(1, "10"), tx(2, "20"), tx(3, "30")), 2, 4));
    }

    @Test
    void batchReturnsOnlyRequestedNumber() {
        assertEquals(List.of(List.of(tx(1, "1"), tx(2, "2"))),
                analyzer.processBatches(Stream.of(tx(1, "1"), tx(2, "2"),
                        tx(3, "3"), tx(4, "4")), 2, 1));
    }

    @Test
    void batchesOfOneWork() {
        assertEquals(List.of(List.of(tx(1, "10")), List.of(tx(2, "20"))),
                analyzer.processBatches(Stream.of(tx(1, "10"), tx(2, "20")), 1, 5));
    }

    @Test
    void emptyBatchStreamReturnsEmpty() {
        assertEquals(List.of(), analyzer.processBatches(Stream.empty(), 2, 3));
    }

    @Test
    void batchResultsAreImmutable() {
        List<List<Transaction>> result = analyzer.processBatches(Stream.of(tx(1, "1")), 2, 3);
        assertThrows(UnsupportedOperationException.class, () -> result.add(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> result.get(0).clear());
    }

    @Test
    void nullStreamThrowsForAllMethods() {
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.findFirstHighValueTransaction(null, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.takeTransactionsUntilBudgetExceeded(null, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.processBatches(null, 2, 2));
    }

    @Test
    void nullRequiredArgumentsThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.findFirstHighValueTransaction(Stream.empty(), null));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.takeTransactionsUntilBudgetExceeded(Stream.empty(), null));
    }

    @Test
    void invalidBatchParametersThrow() {
        assertEquals("batchSize must be greater than 0",
                assertThrows(IllegalArgumentException.class,
                        () -> analyzer.processBatches(Stream.empty(), 0, 1)).getMessage());
        assertEquals("maxBatches must be greater than 0",
                assertThrows(IllegalArgumentException.class,
                        () -> analyzer.processBatches(Stream.empty(), 1, 0)).getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.processBatches(Stream.empty(), -1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.processBatches(Stream.empty(), 1, -1));
    }

    @Test
    void nullElementsOrAmountsFailClearly() {
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.findFirstHighValueTransaction(
                        Stream.of(new Transaction("x", "books", null, LocalDateTime.now())),
                        BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.takeTransactionsUntilBudgetExceeded(
                        Stream.of((Transaction) null), BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> analyzer.processBatches(Stream.of((Transaction) null), 1, 1));
    }

    @Test
    void separateCallsDoNotShareState() {
        List<Transaction> expected = List.of(tx(1, "5"));
        assertEquals(expected, analyzer.takeTransactionsUntilBudgetExceeded(
                Stream.of(tx(1, "5"), tx(2, "6")), new BigDecimal("10")));
        assertEquals(expected, analyzer.takeTransactionsUntilBudgetExceeded(
                Stream.of(tx(1, "5"), tx(2, "6")), new BigDecimal("10")));
        assertEquals(List.of(List.of(tx(1, "5"))),
                analyzer.processBatches(Stream.of(tx(1, "5")), 1, 1));
    }
}
