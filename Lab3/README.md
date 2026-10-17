# Лабораторна робота №3 — Transaction Stream Analyzer

**Дисципліна:** Технології розробки на платформі Java  
**Тема:** Нескінченні потоки Java Stream API, Optional, Stream Gatherers  
**Номер у списку:** 12  
**Варіант:** `V = 12 mod 3 = 0` — Електронна комерція та стрім транзакцій

## Вимоги

- **JDK 27** (Java 24+ потрібна для фінального Stream Gatherer API; у цьому проєкті налаштовано JDK 27).
- **Gradle 9.8.1** (підтримує JDK 27).
- Інтернет під час першого запуску для Gradle й JUnit 5.
- Бізнес-логіка — тільки Java; Kotlin DSL використовується лише для збірки.

## Структура

```text
Lab3/
├── src/main/java/ua/kpi/comsys/streams/
│   ├── Transaction.java
│   ├── TransactionStreamAnalyzer.java
│   └── Main.java
├── src/test/java/ua/kpi/comsys/streams/
│   └── TransactionStreamAnalyzerTest.java
├── gradle/libs.versions.toml
├── gradle/wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew.bat / gradlew / gradle-bootstrap.ps1
├── .gitignore
├── README.md
└── REPORT.md
```

## Методи

- `Optional<Transaction> findFirstHighValueTransaction(Stream<Transaction>, BigDecimal threshold)`  
  Шукає першу транзакцію з `amount > threshold` завдяки `filter().findFirst()`.
- `List<Transaction> takeTransactionsUntilBudgetExceeded(Stream<Transaction>, BigDecimal maxBudget)`  
  Накопичує **початковий префікс**, допоки наступна транзакція не зробить суму більшою за бюджет (`takeWhile`); транзакцію-порушник **не включає**. Рівність бюджету дозволена.
- `List<List<Transaction>> processBatches(Stream<Transaction>, int batchSize, int maxBatches)`  
  **Власний stateful Gatherer**, формує вікна розміру `batchSize`; на нескінченному потоці завершується рівно після `maxBatches` повних вікон. На скінченному потоці включає фінальний неповний батч, якщо ліміт не досягнуто.

Усі `null` аргументи спричиняють `IllegalArgumentException`; `batchSize` і `maxBatches` повинні бути > 0. `null` транзакція чи `amount` у потоці також викликають `IllegalArgumentException`. Вимога до валідації самого `record Transaction` методичкою не встановлена, тому `record` залишається точної форми зі специфікації.

**Важливий теоретичний момент:** для **нескінченного потоку без жодної транзакції, що перевищує threshold**, `findFirst()` не може завершитися — у нього немає достатньої інформації, щоб повернути `Optional.empty()`. Для скінченного потоку без збігів він правильно поверне `Optional.empty()`.

## Запуск у Windows PowerShell

```powershell
cd "C:\Users\halb\Desktop\учеба 3к\1сем\Java\lab3"
java -version
echo $env:JAVA_HOME
.\gradlew.bat clean test
.\gradlew.bat build
.\gradlew.bat run
```

## Тести

`TransactionStreamAnalyzerTest` містить 21 тест JUnit 5, зокрема три перевірки нескінченних стрімів із `@Timeout(2)` (`Stream.generate`).
