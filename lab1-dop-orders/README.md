# Лабораторна робота №1 — Data-Oriented Programming (Java)

**Дисципліна:** Технології розробки на платформі Java  
**Тема:** Сучасні можливості Java та підхід Data-Oriented Programming (DOP)  
**Варіант:** `V = 12 mod 3 = 0`  
**Предметна область:** Система управління замовленнями

## Призначення

Програма моделює чотири стани замовлення (`Pending`, `Paid`, `Shipped`, `Cancelled`) як **незмінні дані**, відокремлює їх від бізнес-логіки в `OrderProcessor`, обробляє всі можливі стани за допомогою **switch expression + pattern matching** без `default` та створює результат через **Text Blocks**.

## Вимоги до середовища

* **JDK 21 або новіший** (pattern matching для `switch` без preview-режиму).
* **Apache Maven 3.8+**.

## Структура проєкту

```text
lab1-dop-orders/
├── pom.xml
├── .gitignore
├── README.md
├── REPORT.md
└── src/
    ├── main/java/ua/kpi/comsys/dop/
    │   ├── OrderStatus.java
    │   ├── Pending.java
    │   ├── Paid.java
    │   ├── Shipped.java
    │   ├── Cancelled.java
    │   ├── OrderProcessor.java
    │   └── Main.java
    └── test/java/ua/kpi/comsys/dop/
        ├── OrderProcessorTest.java
        └── OrderValidationTest.java
```

## Як запустити

Відкрийте термінал у папці, де лежить `pom.xml`:

```bash
mvn clean test
```

```bash
mvn compile exec:java -Dexec.mainClass=ua.kpi.comsys.dop.Main
```

## Основні компоненти

|Компонент|Призначення|
|-|-|
|`OrderStatus`|`sealed interface` — лише чотири дозволені реалізації|
|`Pending`|запис без додаткових полів|
|`Paid`|`paymentId`, `BigDecimal amount`; перевірка ID і суми|
|`Shipped`|`trackingCode`, `LocalDate dispatchDate`; перевірка коду та дати|
|`Cancelled`|`reason`; перевірка причини|
|`OrderProcessor`|метод `String process(OrderStatus status)`|
|`Main`|приклади обробки кожного типу|

## Ключові вимоги методички

* Усі Java-файли містяться в пакеті `ua.kpi.comsys.dop`.
* Ієрархія побудована на `sealed interface` і публічних `record`.
* Дані незмінні; логіка форматування винесена в окремий обробник.
* Некоректні значення викликають `IllegalArgumentException`.
* `null` у `process` викликає **точно** `IllegalArgumentException("Status cannot be null")`.
* `switch` охоплює всі стани й не має `default`.
* Вихід формується через Text Blocks з назвами полів згідно з умовою.
* Є тести JUnit 5 і цей README.

