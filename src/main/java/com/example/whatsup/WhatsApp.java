package com.example.whatsup;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal WhatsApp-like messaging service implemented as a Singleton
 * (initialization-on-demand holder idiom: lazy, thread-safe, lock-free).
 */
public final class WhatsApp {

    private final Set<String> users = ConcurrentHashMap.newKeySet();
    private final List<Message> messages = new CopyOnWriteArrayList<>();

    /** Private constructor: nobody outside can call new WhatsApp(). */
    private WhatsApp() {
    }

    /** The JVM loads this class only on first access to getInstance(), guaranteeing one safe instance. */
    private static final class Holder {
        private static final WhatsApp INSTANCE = new WhatsApp();
    }

    public static WhatsApp getInstance() {
        return Holder.INSTANCE;
    }

    public void registerUser(String username) {
        requireText(username, "username");
        if (!users.add(username)) {
            throw new IllegalArgumentException("User already registered: " + username);
        }
    }

    public boolean isRegistered(String username) {
        return username != null && users.contains(username);
    }

    public Message sendMessage(String from, String to, String content) {
        requireText(content, "content");
        if (!isRegistered(from)) {
            throw new IllegalArgumentException("Unknown sender: " + from);
        }
        if (!isRegistered(to)) {
            throw new IllegalArgumentException("Unknown receiver: " + to);
        }
        Message message = new Message(from, to, content, Instant.now());
        messages.add(message);
        return message;
    }

    /** All messages exchanged between two users, in sending order. */
    public List<Message> getConversation(String userA, String userB) {
        return messages.stream()
                .filter(m -> (m.sender().equals(userA) && m.receiver().equals(userB))
                          || (m.sender().equals(userB) && m.receiver().equals(userA)))
                .toList();
    }

    /** All messages received by a user. */
    public List<Message> getInbox(String username) {
        return messages.stream().filter(m -> m.receiver().equals(username)).toList();
    }

    public int getUserCount() {
        return users.size();
    }

    /** Clears state (the instance itself stays unique). Mainly useful for tests. */
    public void reset() {
        users.clear();
        messages.clear();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
