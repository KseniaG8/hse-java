package hse.java.lectures.lesson7.dau;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("dau")
public class MemoryDauServiceTest {
    
    @Test
    void shouldReturnZeroWhenNoData() {
        Clock clock = Clock.systemUTC();
        MemoryDauService service = new MemoryDauService(clock);

        assertEquals(0L, service.getAuthorDauStatistics(1));
    }

    @Test
    void shouldCountUniqueUsersForAuthor() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        MemoryDauService service = new MemoryDauService(clock);

        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(1, 100)); 
        service.postEvent(new Event(2, 100));

        assertEquals(2L, service.getAuthorDauStatistics(100));
    }

    @Test
    void shouldReturnZeroForUnknownAuthor() {
        Clock clock = Clock.systemUTC();
        MemoryDauService service = new MemoryDauService(clock);

        assertEquals(0L, service.getAuthorDauStatistics(999));
    }

    @Test
    void shouldCountEventsFromYesterday() throws Exception {
        Clock day1 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        MemoryDauService service = new MemoryDauService(day1);
        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(2, 100));

        Clock day2 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        java.lang.reflect.Field clockField = MemoryDauService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(service, day2);

        assertEquals(2L, service.getAuthorDauStatistics(100));
    }

    @Test
    void shouldNotMixUsersAcrossDays() throws Exception {
        Clock day1 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        MemoryDauService service = new MemoryDauService(day1);
        service.postEvent(new Event(1, 100));

        Clock day2 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        java.lang.reflect.Field clockField = MemoryDauService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(service, day2);
        service.postEvent(new Event(2, 100));

        assertEquals(1L, service.getAuthorDauStatistics(100));
    }

    @Test
    void shouldReturnZeroIfNoEventsYesterday() throws Exception {
        Clock day1 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        MemoryDauService service = new MemoryDauService(day1);
        service.postEvent(new Event(1, 100));

        Clock day2 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        java.lang.reflect.Field clockField = MemoryDauService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(service, day2);

        assertEquals(0L, service.getAuthorDauStatistics(200));
    }

    @Test
    void shouldHandleMultipleAuthors() throws Exception {
        Clock day1 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        MemoryDauService service = new MemoryDauService(day1);
        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(2, 100));
        service.postEvent(new Event(3, 200));

        Clock day2 = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        java.lang.reflect.Field clockField = MemoryDauService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(service, day2);

        Map<Integer, Long> stats = service.getDauStatistics(List.of(100, 200));
        
        assertEquals(2L, stats.get(100));
        assertEquals(1L, stats.get(200));
    }
}