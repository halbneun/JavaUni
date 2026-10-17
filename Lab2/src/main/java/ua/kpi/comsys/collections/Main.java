package ua.kpi.comsys.collections;

import java.util.List;

/** Демонстрація роботи обох методів. */
public class Main {
    public static void main(String[] args) {
        List<String> logs = List.of(
                "192.168.1.2 - alice [01/May/2025:07:20:10 +0000] \"GET /index.html HTTP/1.1\" 200 9481",
                "10.0.0.1 - bob [01/May/2025:07:21:10 +0000] \"POST /login HTTP/1.1\" 200 128",
                "192.168.1.2 - alice [01/May/2025:07:22:10 +0000] \"GET /about HTTP/1.1\" 404 512",
                "invalid log line",
                "10.0.0.1 - bob [01/May/2025:07:23:10 +0000] \"GET /profile HTTP/1.1\" 200 32",
                "8.8.8.8 - - [01/May/2025:07:24:10 +0000] \"GET / HTTP/1.1\" 200 -"
        );

        LogAnalyzer analyzer = new LogAnalyzer();
        System.out.println("Unique IPs: " + analyzer.getUniqueIps(logs));
        System.out.println("Top 2 IPs: " + analyzer.getTopIps(logs, 2));
    }
}
