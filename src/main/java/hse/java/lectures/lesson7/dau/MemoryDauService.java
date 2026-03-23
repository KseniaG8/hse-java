package hse.java.lectures.lesson7.dau;

import java.util.concurrent.*;
import java.time.*;
import java.util.*;

public class MemoryDauService implements DauService {
    private final Clock clock;

    private final ConcurrentMap<LocalDate, ConcurrentMap<Integer, Set<Integer>>> storage = new ConcurrentHashMap<>();

    public MemoryDauService(Clock clock) {
        this.clock = clock;
    }

    private LocalDate currentDate() {
        return LocalDate.now(clock);
    }

    private void cleanOldData() {
        LocalDate threshold = currentDate().minusDays(2);
        storage.keySet().removeIf(date -> date.isBefore(threshold));
    } 

    @Override
    public void postEvent(Event event) {
        cleanOldData();

        LocalDate today = currentDate();

        storage.computeIfAbsent(today, d -> new ConcurrentHashMap<Integer, Set<Integer>>())
        .computeIfAbsent(event.authorId(), a -> ConcurrentHashMap.newKeySet())
        .add(event.userId());
    }

    @Override
    public Map<Integer, Long> getDauStatistics(List<Integer> authorIds) {
        LocalDate yesterday = currentDate().minusDays(1);
        Map<Integer, Long> result = new HashMap<>();

        Map<Integer, Set<Integer>> dayData = storage.get(yesterday);

        for (Integer authorId : authorIds) {
            if (dayData == null) {
                result.put(authorId, 0L);
            } else {
                Set<Integer> users = dayData.getOrDefault(authorId, Collections.emptySet());
                result.put(authorId, (long) users.size());
            }
        }

        return result;
    }

    @Override
    public Long getAuthorDauStatistics(int authorId) {
        LocalDate yesterday = currentDate().minusDays(1);

        ConcurrentMap<Integer, Set<Integer>> dayData = storage.get(yesterday);

        if(dayData == null) {
            return 0L;
        }

        Set<Integer> users = dayData.getOrDefault(authorId, Collections.emptySet());
        return (long) users.size();
    }
}
