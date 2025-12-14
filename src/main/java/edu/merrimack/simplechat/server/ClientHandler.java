package edu.merrimack.simplechat.server;

import edu.merrimack.simplechat.common.Framing;
import edu.merrimack.simplechat.common.NetUtil;
import edu.merrimack.simplechat.common.protocol.BaseMessage;
import edu.merrimack.simplechat.common.protocol.ChatMessage;
import edu.merrimack.simplechat.common.protocol.ConnectAckMessage;
import edu.merrimack.simplechat.common.protocol.ConnectMessage;
import edu.merrimack.simplechat.common.protocol.DisconnectMessage;
import edu.merrimack.simplechat.common.protocol.ErrorMessage;
import edu.merrimack.simplechat.common.protocol.ListUsersMessage;
import edu.merrimack.simplechat.common.protocol.MessageParser;
import edu.merrimack.simplechat.common.protocol.MessageType;
import edu.merrimack.simplechat.common.protocol.ServerBroadcastMessage;
import edu.merrimack.simplechat.common.protocol.SetUsernameMessage;
import edu.merrimack.simplechat.common.protocol.UserListMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles a single client connection lifecycle.
 */
public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ClientRegistry registry;
    private volatile boolean active = true;
    private String username;

    /** Creates a handler for a single client socket tied to the shared registry. */
    public ClientHandler(Socket socket, ClientRegistry registry) {
        this.socket = socket;
        this.registry = registry;
    }

    /**
     * Primary Runnable entry point; delegates to lifecycle handling.
     */
    @Override
    public void run() {
        try {
            handleClient();
        } catch (Exception e) {
            log.warn("Client handler error: {}", e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Processes initial handshake then enters the listen loop while active.
     */
    private void handleClient() throws IOException {
        String firstJson = Framing.readFrame(socket.getInputStream());
        if (firstJson == null) {
            throw new IOException("Empty handshake from client");
        }

        BaseMessage base;
        try {
            base = MessageParser.parse(firstJson);
        } catch (InvalidObjectException e) {
            send(new ErrorMessage("BAD_JSON", e.getMessage()));
            active = false;
            return;
        }

        if (base.getType() != MessageType.CONNECT) {
            send(new ErrorMessage("INVALID_HANDSHAKE", "First message must be CONNECT"));
            active = false;
            return;
        }

        ConnectMessage connect = (ConnectMessage) base;
        String desired = connect.getUsername();
        if (desired == null || desired.isBlank()) {
            desired = "guest-" + UUID.randomUUID().toString().substring(0, 8);
        }

        if (!attemptSetUsername(desired)) {
            send(new ErrorMessage("USERNAME_TAKEN", "Username already in use"));
            active = false;
            return;
        }

        ConnectAckMessage ack = new ConnectAckMessage("OK", "Welcome to SCP v1");
        send(ack);
        registry.broadcast(new ServerBroadcastMessage(username + " joined"), this);

        listenLoop();
    }

    /**
     * Reads framed messages until the client disconnects or an error occurs.
     */
    private void listenLoop() throws IOException {
        while (active) {
            String json = Framing.readFrame(socket.getInputStream());
            if (json == null) {
                break;
            }
            BaseMessage msg;
            try {
                msg = MessageParser.parse(json);
            } catch (InvalidObjectException e) {
                send(new ErrorMessage("INVALID_MESSAGE", e.getMessage()));
                continue;
            }

            switch (msg.getType()) {
                case SET_USERNAME:
                    handleSetUsername((SetUsernameMessage) msg);
                    break;
                case CHAT_MESSAGE:
                    handleChatMessage((ChatMessage) msg);
                    break;
                case DISCONNECT:
                    handleDisconnect((DisconnectMessage) msg);
                    break;
                case LIST_USERS:
                    handleListUsers((ListUsersMessage) msg);
                    break;
                default:
                    send(new ErrorMessage("NOT_ALLOWED", "Message type not allowed in this state"));
            }
        }
    }

    /** Handles username change requests and notifies other clients. */
    private void handleSetUsername(SetUsernameMessage msg) {
        String oldName = this.username;
        if (attemptSetUsername(msg.getUsername())) {
            registry.broadcast(new ServerBroadcastMessage(oldName + " is now known as " + msg.getUsername()), this);
        }
    }

    /**
     * Tries to claim the desired username; returns true on success and sends errors
     * on failure.
     */
    private boolean attemptSetUsername(String desired) {
        if (desired == null || desired.isBlank()) {
            send(new ErrorMessage("INVALID_USERNAME", "Username required"));
            return false;
        }
        if (!desired.equals(username) && !registry.register(desired, this)) {
            send(new ErrorMessage("USERNAME_TAKEN", "Username already in use"));
            return false;
        }
        if (username != null && !username.equals(desired)) {
            registry.unregister(username);
        }
        this.username = desired;
        return true;
    }

    /**
     * Routes chat messages either to a direct recipient or broadcast to all.
     */
    private void handleChatMessage(ChatMessage msg) {
        if (!username.equals(msg.getFrom())) {
            send(new ErrorMessage("INVALID_SENDER", "from field must match session username"));
            return;
        }
        if (msg.isDirect()) {
            ClientHandler target = registry.get(msg.getTo());
            if (target == null) {
                send(new ErrorMessage("UNKNOWN_USER", "User not found: " + msg.getTo()));
                return;
            }
            target.send(msg);
        } else {
            registry.broadcast(msg, this);
        }
    }

    /** Returns the current user list to the requesting client. */
    private void handleListUsers(ListUsersMessage ignored) {
        UserListMessage response = new UserListMessage(registry.listUsernames());
        send(response);
    }

    /** Handles graceful disconnects initiated by the client. */
    private void handleDisconnect(DisconnectMessage msg) {
        log.info("Disconnect requested by {}: {}", username, msg.getReason());
        active = false;
    }

    /**
     * Frames and writes a message to the client socket; deactivates on failure.
     */
    public synchronized void send(BaseMessage message) {
        try {
            byte[] framed = Framing.frame(message.serialize());
            socket.getOutputStream().write(framed);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            log.warn("Failed to send to {}: {}", username, e.getMessage());
            active = false;
        }
    }

    /**
     * Releases resources and informs others that the user left.
     */
    private void cleanup() {
        if (username != null) {
            registry.unregister(username);
            registry.broadcast(new ServerBroadcastMessage(username + " left"), this);
        }
        NetUtil.closeQuietly(socket);
    }
}
