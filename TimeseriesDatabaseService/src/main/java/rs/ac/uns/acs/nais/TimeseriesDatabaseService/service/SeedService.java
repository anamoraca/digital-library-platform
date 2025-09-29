package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final BookAnalyticsService bookService;
    private final SessionAnalyticsService sessionService;
    private final ErrorAnalyticsService errorService;

    private static final String[] USERS = mkUsers(1, 400);
    private static final String[] BOOKS = { "b-10","b-20","b-30","b-40","b-50","b-60","b-70","b-80","b-90","b-100" };
    private static final String[] DEVICES = { "android", "ios", "web" };
    private static final String[] VERSIONS = { "1.4.2", "1.5.0", "1.6.1" };
    private static final String[] SERVICES = { "gateway", "catalog", "reading", "search" };
    private static final String[] ERROR_TYPES = { "TIMEOUT", "DB_DOWN", "AUTH_FAILED", "RATE_LIMIT", "VALIDATION" };

    private static String[] mkUsers(int from, int toInclusive) {
        String[] arr = new String[toInclusive - from + 1];
        for (int i = from, k = 0; i <= toInclusive; i++, k++) arr[k] = "u-" + i;
        return arr;
    }

    private static String pick(String[] arr) { return arr[ThreadLocalRandom.current().nextInt(arr.length)]; }
    private static <T> T pick(T[] arr) { return arr[ThreadLocalRandom.current().nextInt(arr.length)]; }

    private static Instant randomInstantWithinDays(int daysBack) {
        long now = Instant.now().getEpochSecond();
        long min = now - daysBack * 24L * 3600L;
        long sec = ThreadLocalRandom.current().nextLong(min, now + 1);
        return Instant.ofEpochSecond(sec);
    }

    // BOOKS
    public int seedBooks(int pairs, int daysBack, Integer batchSizeOverride) {
        int batchSize = (batchSizeOverride != null && batchSizeOverride > 0) ? batchSizeOverride : 500;
        List<BookEvent> buffer = new ArrayList<>(batchSize);
        int inserted = 0;

        for (int i = 0; i < pairs; i++) {
            String user = pick(USERS);
            String book = pick(BOOKS);
            BookFormat fmt = pick(BookFormat.values());
            Instant ts = randomInstantWithinDays(daysBack);

            double loadMs = ThreadLocalRandom.current().nextDouble(200, 2700);
            buffer.add(new BookEvent(user, book, fmt, BookEventType.OPENED, loadMs, null, ts, "seed-open-" + UUID.randomUUID()));
            int deltaPages = ThreadLocalRandom.current().nextInt(1, 13);
            buffer.add(new BookEvent(user, book, fmt, BookEventType.PROGRESS, null, deltaPages, ts, "seed-prog-" + UUID.randomUUID()));

            if (buffer.size() >= batchSize) { bookService.insertEventsBatch(buffer); inserted += buffer.size(); buffer.clear(); }
        }
        if (!buffer.isEmpty()) { bookService.insertEventsBatch(buffer); inserted += buffer.size(); }
        return inserted;
    }

    // SESSIONS
    public int seedSessions(int pairs, int daysBack, Integer batchSizeOverride) {
        int batchSize = (batchSizeOverride != null && batchSizeOverride > 0) ? batchSizeOverride : 500;
        List<AppSessionEvent> buffer = new ArrayList<>(batchSize);
        int inserted = 0;

        for (int i = 0; i < pairs; i++) {
            String user = pick(USERS);
            String device = pick(DEVICES);
            String ver = pick(VERSIONS);
            Instant ts = randomInstantWithinDays(daysBack);

            buffer.add(new AppSessionEvent(user, device, ver, SessionEventType.START, null, ts, "seed-start-" + UUID.randomUUID()));
            int duration = ThreadLocalRandom.current().nextInt(30, 1801);
            buffer.add(new AppSessionEvent(user, device, ver, SessionEventType.END, (double) duration, ts, "seed-end-" + UUID.randomUUID()));

            if (buffer.size() >= batchSize) { sessionService.insertBatch(buffer); inserted += buffer.size(); buffer.clear(); }
        }
        if (!buffer.isEmpty()) { sessionService.insertBatch(buffer); inserted += buffer.size(); }
        return inserted;
    }

    // ERRORS
    public int seedErrors(int count, int daysBack, Integer batchSizeOverride) {
        int batchSize = (batchSizeOverride != null && batchSizeOverride > 0) ? batchSizeOverride : 500;
        List<ErrorLogEvent> buffer = new ArrayList<>(batchSize);
        int inserted = 0;

        for (int i = 0; i < count; i++) {
            String svc = pick(SERVICES);
            String typ = pick(ERROR_TYPES);
            int c = ThreadLocalRandom.current().nextInt(1, 4);
            Instant ts = randomInstantWithinDays(daysBack);

            buffer.add(new ErrorLogEvent(svc, typ, c, ts, "seed-err-" + UUID.randomUUID()));
            if (buffer.size() >= batchSize) { errorService.insertBatch(buffer); inserted += buffer.size(); buffer.clear(); }
        }
        if (!buffer.isEmpty()) { errorService.insertBatch(buffer); inserted += buffer.size(); }
        return inserted;
    }

    // ALL
    public SeedResult seedAll(int bookPairs, int sessionPairs, int errorCount, int daysBack) {
        int books = seedBooks(bookPairs, daysBack, null);
        int sessions = seedSessions(sessionPairs, daysBack, null);
        int errors = seedErrors(errorCount, daysBack, null);
        return new SeedResult(books, sessions, errors);
    }
}
