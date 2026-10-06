package com.example.whatsup;

public class App {
    public static void main(String[] args) {
        WhatsApp app = WhatsApp.getInstance();
        app.registerUser("alice");
        app.registerUser("bob");

        app.sendMessage("alice", "bob", "Salam Bob !");
        app.sendMessage("bob", "alice", "Salam Alice, ça va ?");

        app.getConversation("alice", "bob").forEach(m ->
                System.out.printf("[%s] %s -> %s : %s%n", m.sentAt(), m.sender(), m.receiver(), m.content()));

        System.out.println("Same instance? " + (app == WhatsApp.getInstance()));
    }
}
