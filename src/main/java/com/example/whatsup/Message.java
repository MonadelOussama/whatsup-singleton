package com.example.whatsup;

import java.time.Instant;

/** Immutable chat message. */
public record Message(String sender, String receiver, String content, Instant sentAt) {
}
