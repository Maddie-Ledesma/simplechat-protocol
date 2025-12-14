package edu.merrimack.simplechat.server;

import edu.merrimack.simplechat.common.protocol.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry mapping usernames to client handlers.
 */
public class ClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(ClientRegistry.class);

    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    /**
     * Attempts to register a username; returns false if already present.
     */
    public boolean register(String username, ClientHandler handler) {
        ClientHandler existing = clients.putIfAbsent(username, handler);
        if (existing != null) {
            return false;
        }
        log.info("Registered user {}", username);
        return true;
    }

    /**
     * Removes the username from the registry if present.
     */
    public void unregister(String username) {
        if (username != null) {
            clients.remove(username);
            log.info("Unregistered user {}", username);
        }
    }

    /** Retrieves the handler for a username or null if not found. */
    public ClientHandler get(String username) {
        return clients.get(username);
    }

    /** Returns the current number of registered clients. */
    public int size() {
        return clients.size();
    }

    /**
     * Broadcasts a message to all clients except an optional sender to exclude.
     */
    public void broadcast(BaseMessage message, ClientHandler exclude) {
        for (ClientHandler handler : clients.values()) {
            if (handler != exclude) {
                handler.send(message);
            }
        }
    }

    /** Snapshot of all registered handlers. */
    public Collection<ClientHandler> all() {
        return clients.values();
    }

    /** Returns a snapshot list of all registered usernames. */
    public java.util.List<String> listUsernames() {
        return java.util.List.copyOf(clients.keySet());
    }
}
