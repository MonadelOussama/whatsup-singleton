package com.example.whatsup;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WhatsAppTest {

    private WhatsApp app;

    @BeforeEach
    void setUp() {
        app = WhatsApp.getInstance();
        app.reset();
    }

    // ---------- Singleton behaviour ----------

    @Test
    void getInstance_alwaysReturnsSameInstance() {
        assertSame(WhatsApp.getInstance(), WhatsApp.getInstance());
    }

    @Test
    void constructor_isPrivate() throws Exception {
        Constructor<WhatsApp> c = WhatsApp.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()));
    }

    @Test
    void getInstance_isThreadSafe() throws Exception {
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        Set<WhatsApp> instances = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                start.await();
                instances.add(WhatsApp.getInstance());
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, instances.size());
    }

    @Test
    void state_isSharedAcrossReferences() {
        WhatsApp.getInstance().registerUser("alice");
        assertTrue(WhatsApp.getInstance().isRegistered("alice"));
    }

    // ---------- Business logic ----------

    @Test
    void registerUser_addsUser() {
        app.registerUser("alice");
        assertTrue(app.isRegistered("alice"));
        assertEquals(1, app.getUserCount());
    }

    @Test
    void registerUser_duplicateThrows() {
        app.registerUser("alice");
        assertThrows(IllegalArgumentException.class, () -> app.registerUser("alice"));
    }

    @Test
    void registerUser_blankThrows() {
        assertThrows(IllegalArgumentException.class, () -> app.registerUser("  "));
        assertThrows(IllegalArgumentException.class, () -> app.registerUser(null));
    }

    @Test
    void sendMessage_storesMessage() {
        app.registerUser("alice");
        app.registerUser("bob");

        Message m = app.sendMessage("alice", "bob", "Salam");

        assertEquals("alice", m.sender());
        assertEquals("bob", m.receiver());
        assertEquals("Salam", m.content());
        assertNotNull(m.sentAt());
        assertEquals(1, app.getInbox("bob").size());
    }

    @Test
    void sendMessage_unknownUsersThrow() {
        app.registerUser("alice");
        assertThrows(IllegalArgumentException.class, () -> app.sendMessage("alice", "ghost", "hi"));
        assertThrows(IllegalArgumentException.class, () -> app.sendMessage("ghost", "alice", "hi"));
    }

    @Test
    void sendMessage_blankContentThrows() {
        app.registerUser("alice");
        app.registerUser("bob");
        assertThrows(IllegalArgumentException.class, () -> app.sendMessage("alice", "bob", ""));
    }

    @Test
    void getConversation_returnsBothDirectionsInOrder() {
        app.registerUser("alice");
        app.registerUser("bob");
        app.registerUser("carol");

        app.sendMessage("alice", "bob", "1");
        app.sendMessage("bob", "alice", "2");
        app.sendMessage("alice", "carol", "not in this chat");
        app.sendMessage("alice", "bob", "3");

        var conv = app.getConversation("alice", "bob");
        assertEquals(3, conv.size());
        assertEquals("1", conv.get(0).content());
        assertEquals("2", conv.get(1).content());
        assertEquals("3", conv.get(2).content());
    }

    @Test
    void reset_clearsStateButKeepsSingleInstance() {
        app.registerUser("alice");
        app.reset();
        assertEquals(0, app.getUserCount());
        assertSame(app, WhatsApp.getInstance());
    }
}
