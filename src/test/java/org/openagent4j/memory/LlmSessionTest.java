package org.openagent4j.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LlmSessionTest {

    @Test
    void stateIsSharedBySessionId() {
        LlmSession first = LlmSession.newPersistentSession("shared-session");
        LlmSession second = LlmSession.newPersistentSession("shared-session");
        first.clearState();

        first.putState("topic", "weather");

        assertEquals("weather", second.state("topic"));
    }

    @Test
    void removeAndClearStateWorkAsExpected() {
        LlmSession session = LlmSession.newPersistentSession("clear-session");
        session.clearState();

        session.putState("a", 1);
        session.putState("b", 2);

        assertEquals(1, session.removeState("a"));
        assertNull(session.state("a"));

        session.clearState();
        assertEquals(0, session.snapshotState().size());
    }

    @Test
    void concurrentWritesAreSafe() throws Exception {
        LlmSession session = LlmSession.newPersistentSession("concurrent-session");
        session.clearState();

        int writes = 100;
        CountDownLatch latch = new CountDownLatch(writes);
        try (var pool = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < writes; i++) {
                final int index = i;
                pool.submit(() -> {
                    session.putState("k-" + index, index);
                    latch.countDown();
                });
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }

        assertEquals(true, latch.await(5, TimeUnit.SECONDS));
        assertEquals(writes, session.snapshotState().size());
    }
}