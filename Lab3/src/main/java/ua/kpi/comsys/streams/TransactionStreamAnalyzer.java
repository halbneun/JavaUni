package ua.kpi.comsys.streams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

/**
 * Аналітика скінченних та нескінченних потоків транзакцій.
 * Stream є одноразовим: кожен виклик отримує новий stream.
 */
public class TransactionStreamAnalyzer {

    /**
     * Шукає першу транзакцію із сумою СТРОГО більшою за threshold.
     * Для нескінченного потоку завершується, якщо відповідна транзакція існує.
     */
    public Optional<Transaction> findFirstHighValueTransaction(
            Stream<Transaction> stream, BigDecimal threshold) {
        requireArgument(stream, "stream");
        requireArgument(threshold, "threshold");

        return stream.sequential()
                .filter(transaction -> validAmount(transaction).compareTo(threshold) > 0)
                .findFirst();
    }

    /**
     * Бере початковий префікс транзакцій, не перевищуючи maxBudget.
     * Транзакція, яка ПЕРША перевищила би бюджет, до списку не входить.
     * При рівності з лімітом транзакція входить.
     */
    public List<Transaction> takeTransactionsUntilBudgetExceeded(
            Stream<Transaction> stream, BigDecimal maxBudget) {
        requireArgument(stream, "stream");
        requireArgument(maxBudget, "maxBudget");

        final class BudgetState {
            private BigDecimal total = BigDecimal.ZERO;

            private boolean canInclude(Transaction transaction) {
                BigDecimal next = total.add(validAmount(transaction));
                if (next.compareTo(maxBudget) > 0) {
                    return false;
                }
                total = next;
                return true;
            }
        }

        BudgetState budget = new BudgetState();
        return stream.sequential().takeWhile(budget::canInclude).toList();
    }

    /**
     * Групує послідовний stream у батчі; обробка нескінченного
     * джерела зупиняється точно після maxBatches повних батчів.
     * Для скінченного потоку фінальний неповний батч теж повертається.
     */
    public List<List<Transaction>> processBatches(
            Stream<Transaction> stream, int batchSize, int maxBatches) {
        requireArgument(stream, "stream");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        if (maxBatches <= 0) {
            throw new IllegalArgumentException("maxBatches must be greater than 0");
        }

        Gatherer<Transaction, BatchState, List<Transaction>> batching =
                Gatherer.ofSequential(
                        () -> new BatchState(batchSize),
                        (state, transaction, downstream) -> {
                            requireArgument(transaction, "transaction");
                            state.current.add(transaction);

                            if (state.current.size() == batchSize) {
                                // Окрема незмінна копія: зміни state.current не
                                // вплинуть на вже сформовані батчі.
                                boolean accepted = downstream.push(List.copyOf(state.current));
                                state.current.clear();
                                state.produced++;
                                // false = коротке замикання нескінченного потоку.
                                return accepted && state.produced < maxBatches;
                            }
                            return true;
                        },
                        (state, downstream) -> {
                            // Якщо джерело скінчилося посеред батча, не губимо дані.
                            // Коли maxBatches вже сформовано, нічого не додаємо.
                            if (state.produced < maxBatches && !state.current.isEmpty()) {
                                downstream.push(List.copyOf(state.current));
                            }
                        }
                );

        return stream.sequential().gather(batching).toList();
    }

    /** Окремий mutable state для кожного виклику Gatherer. */
    private static final class BatchState {
        private final List<Transaction> current;
        private int produced;

        private BatchState(int batchSize) {
            this.current = new ArrayList<>(batchSize);
        }
    }

    private static BigDecimal validAmount(Transaction transaction) {
        requireArgument(transaction, "transaction");
        requireArgument(transaction.amount(), "transaction.amount");
        return transaction.amount();
    }

    private static void requireArgument(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }
}
