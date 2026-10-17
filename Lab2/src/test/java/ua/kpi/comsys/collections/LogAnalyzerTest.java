package ua.kpi.comsys.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalyzerTest {
    private final LogAnalyzer analyzer = new LogAnalyzer();

    private static String line(String ip, String path) {
        return ip + " - alice [01/May/2025:07:20:10 +0000] \"GET " + path
                + " HTTP/1.1\" 200 9481";
    }

    @Test void uniqueIpsAreCollected() {
        assertEquals(Set.of("127.0.0.1", "10.0.0.2"), analyzer.getUniqueIps(List.of(
                line("127.0.0.1", "/a"), line("10.0.0.2", "/b"), line("127.0.0.1", "/c"))));
    }

    @Test void emptyInputReturnsEmptyCollections() {
        assertTrue(analyzer.getUniqueIps(List.of()).isEmpty());
        assertTrue(analyzer.getTopIps(List.of(), 2).isEmpty());
    }

    @Test void malformedAndNullLinesAreIgnored() {
        List<String> input = new ArrayList<>(Arrays.asList(
                "this is not common log format", null,
                "10.0.0.2 - alice [01/May/2025:07:20:10 +0000] \"GET / HTTP/1.1\" 200 10 extra",
                line("192.168.0.1", "/")));
        assertEquals(Set.of("192.168.0.1"), analyzer.getUniqueIps(input));
        assertEquals(List.of("192.168.0.1"), analyzer.getTopIps(input, 10));
    }

    @Test void invalidIpAndDateAreIgnored() {
        List<String> input = List.of(
                line("999.1.2.3", "/"), line("server.example.com", "/"),
                "10.0.0.1 - alice [35/May/2025:07:20:10 +0000] \"GET / HTTP/1.1\" 200 4",
                line("10.0.0.9", "/"));
        assertEquals(Set.of("10.0.0.9"), analyzer.getUniqueIps(input));
    }

    @Test void invalidRequestIsIgnored() {
        assertEquals(Set.of(), analyzer.getUniqueIps(List.of(
                "10.0.0.1 - alice [01/May/2025:07:20:10 +0000] \"nonstandard request\" 200 4")));
    }

    @Test void ipv6AndUnknownBytesAreSupported() {
        assertEquals(Set.of("2001:db8::1"), analyzer.getUniqueIps(List.of(
                "2001:db8::1 - - [01/May/2025:07:20:10 +0000] \"GET / HTTP/2\" 200 -")));
    }

    @Test void topIpsSortedByFrequency() {
        List<String> logs = List.of(
                line("1.1.1.1", "/1"), line("2.2.2.2", "/2"),
                line("2.2.2.2", "/3"), line("3.3.3.3", "/4"),
                line("2.2.2.2", "/5"), line("1.1.1.1", "/6"));
        assertEquals(List.of("2.2.2.2", "1.1.1.1"), analyzer.getTopIps(logs, 2));
    }

    @Test void tiesUseLexicographicOrder() {
        List<String> logs = List.of(
                line("192.168.0.9", "/a"), line("10.0.0.2", "/b"),
                line("192.168.0.9", "/c"), line("10.0.0.2", "/d"));
        assertEquals(List.of("10.0.0.2", "192.168.0.9"), analyzer.getTopIps(logs, 2));
    }

    @Test void returnsAllIfTopNExceedsNumberOfUniqueIps() {
        assertEquals(List.of("10.0.0.1"), analyzer.getTopIps(List.of(line("10.0.0.1", "/")), 100));
    }

    @Test void exactTopNSize() {
        assertEquals(List.of("10.0.0.1"), analyzer.getTopIps(List.of(
                line("10.0.0.2", "/"), line("10.0.0.1", "/")), 1));
    }

    @Test void noMutationOfOriginalList() {
        List<String> data = new ArrayList<>(List.of(line("10.0.0.1", "/")));
        List<String> copy = List.copyOf(data);
        analyzer.getUniqueIps(data);
        analyzer.getTopIps(data, 1);
        assertEquals(copy, data);
    }

    @Test void nullUniqueInputThrowsExactMessage() {
        assertEquals("logLines cannot be null", assertThrows(IllegalArgumentException.class,
                () -> analyzer.getUniqueIps(null)).getMessage());
    }

    @Test void nullTopInputThrowsExactMessage() {
        assertEquals("logLines cannot be null", assertThrows(IllegalArgumentException.class,
                () -> analyzer.getTopIps(null, 3)).getMessage());
    }

    @Test void topNZeroThrowsExactMessage() {
        assertEquals("topN must be greater than 0", assertThrows(IllegalArgumentException.class,
                () -> analyzer.getTopIps(List.of(), 0)).getMessage());
    }

    @Test void negativeTopNThrowsExactMessage() {
        assertEquals("topN must be greater than 0", assertThrows(IllegalArgumentException.class,
                () -> analyzer.getTopIps(List.of(), -5)).getMessage());
    }
}
