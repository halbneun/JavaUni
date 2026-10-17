# Лабораторна робота №2 — Аналізатор логів серверів

**Дисципліна:** Технології розробки на платформі Java (КПІ ім. Ігоря Сікорського)  
**Тема:** Збірка проєкту за допомогою Gradle та робота з Java Collections Framework  
**Варіант:** `V = N mod 3 = 12 mod 3 = 0`.

## Реалізовані вимоги

- `LogAnalyzer#getUniqueIps(List<String>)` — повертає унікальні IP з коректних CLF-записів.
- `LogAnalyzer#getTopIps(List<String>, int)` — рейтинг IP за частотою, а при нічиїй — лексикографічно.
- Неправильні рядки й `null`-елементи пропускаються. Якщо список дорівнює `null`, виникає `IllegalArgumentException("logLines cannot be null")`.
- Якщо `topN <= 0`, виникає `IllegalArgumentException("topN must be greater than 0")`.
- Тільки стандартні Java Collections (`List`, `Set`, `Map`, `HashMap`, `TreeSet`).
- Gradle (Kotlin DSL), JUnit 5 та керування залежностями через `gradle/libs.versions.toml`.
- Усі Java-класи в пакеті `ua.kpi.comsys.collections`.

## Вимоги

- JDK 21 або новіший (рекомендовано JDK 21 чи твій JDK 27).
- Gradle **9.8.0**, який підтримує запуск під JDK 27.
## Як запустити у Windows PowerShell

Відкрий PowerShell **в папці Lab2**, де лежить `build.gradle.kts`.

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat run
```

**Особливість архіву:** `gradle-wrapper.jar` не включено; при першому запуску скрипт `gradlew.bat` автоматично завантажує ОФІЦІЙНИЙ JAR Gradle 9.8.0 із `services.gradle.org`. Після цього запускає стандартний Gradle Wrapper. Щоб створити стандартні файли wrapper локально, після першого запуску можна виконати `.\gradlew.bat wrapper --gradle-version 9.8.0`, але локально завантажений JAR за бажанням потрібно додати до Git.

У Linux/macOS:

```bash
./gradlew test
./gradlew build
./gradlew run
```

Після успішного виконання тестів очікується `BUILD SUCCESSFUL`.

Якщо Gradle вже встановлено, можна просто виконати `gradle test`, `gradle build`, `gradle run`.

## Формат вхідних даних

Приклад Common Log Format:

```text
127.0.0.1 ident alice [01/May/2025:07:20:10 +0000] "GET /index.html HTTP/1.1" 200 9481
```

Парсер очікує IP-адресу, два поля ідентифікації, дату й часовий пояс,
HTTP-запит у лапках, 3-цифровий статус і розмір відповіді (число або `-`).
Некоректні дати й IP-адреси ігноруються. Підтримуються IPv4 і IPv6.

## Структура

```text
lab2-log-analyzer/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew, gradlew.bat
├── src/main/java/ua/kpi/comsys/collections/
│   ├── LogAnalyzer.java
│   └── Main.java
├── src/test/java/ua/kpi/comsys/collections/LogAnalyzerTest.java
└── README.md

