package ua.kpi.comsys.collections;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Аналізує IP-адреси з рядків серверного логу у Common Log Format.
 * Некоректні рядки та null-елементи ігноруються.
 */
public class LogAnalyzer {

    // IP ident authuser [dd/MMM/yyyy:HH:mm:ss +zzzz] "request" status bytes
    // Повний збіг виключає випадкові рядки та Combined Log Format з додатковими полями.
    private static final Pattern COMMON_LOG = Pattern.compile(
            "^(\\S+) (\\S+) (\\S+) \\[([0-9]{2}/[A-Za-z]{3}/[0-9]{4}:" +
                    "[0-9]{2}:[0-9]{2}:[0-9]{2} [+-][0-9]{4})\\] " +
                    "\\\"([^\\\"]*)\\\" ([0-9]{3}) ([0-9]+|-)\\s*$"
    );

    private static final Pattern IPV4 = Pattern.compile("[0-9]{1,3}(\\.[0-9]{1,3}){3}");
    private static final Pattern REQUEST = Pattern.compile(
            "[A-Z]+ \\S+ HTTP/[0-9]+(?:\\.[0-9]+)?|-"
    );
    private static final DateTimeFormatter LOG_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd/MMM/uuuu:HH:mm:ss Z")
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    /** Повертає унікальні IP-адреси з коректних рядків логу. */
    public Set<String> getUniqueIps(List<String> logLines) {
        requireLines(logLines);
        Set<String> ips = new TreeSet<>();
        for (String line : logLines) {
            parseIp(line).ifPresent(ips::add);
        }
        return ips;
    }

    /**
     * Повертає щонайбільше topN IP-адрес за спаданням кількості записів.
     * При рівній кількості адреси впорядковуються лексикографічно.
     */
    public List<String> getTopIps(List<String> logLines, int topN) {
        requireLines(logLines);
        if (topN <= 0) {
            throw new IllegalArgumentException("topN must be greater than 0");
        }
        Map<String, Integer> counts = new HashMap<>();
        for (String line : logLines) {
            parseIp(line).ifPresent(ip -> counts.merge(ip, 1, Integer::sum));
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Comparator
                .<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                .reversed()
                .thenComparing(Map.Entry::getKey));
        return entries.stream().limit(topN).map(Map.Entry::getKey).toList();
    }

    private static void requireLines(List<String> logLines) {
        if (logLines == null) {
            throw new IllegalArgumentException("logLines cannot be null");
        }
    }

    private static java.util.Optional<String> parseIp(String line) {
        if (line == null) {
            return java.util.Optional.empty();
        }
        Matcher matcher = COMMON_LOG.matcher(line);
        if (!matcher.matches()) {
            return java.util.Optional.empty();
        }
        String ip = matcher.group(1);
        if (!isValidIp(ip) || !REQUEST.matcher(matcher.group(5)).matches()) {
            return java.util.Optional.empty();
        }
        try {
            OffsetDateTime.parse(matcher.group(4), LOG_DATE);
        } catch (DateTimeParseException exception) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(ip);
    }

    /** Перевірка IPv4 без DNS-запитів, IPv6 тільки для адрес із двокрапкою. */
    private static boolean isValidIp(String ip) {
        if (IPV4.matcher(ip).matches()) {
            for (String octet : ip.split("\\.")) {
                if (Integer.parseInt(octet) > 255) {
                    return false;
                }
            }
            return true;
        }
        if (ip.contains(":")) {
            try {
                return InetAddress.getByName(ip) instanceof Inet6Address;
            } catch (UnknownHostException exception) {
                return false;
            }
        }
        return false;
    }
}
