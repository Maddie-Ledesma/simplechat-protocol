package edu.merrimack.simplechat.client;

import edu.merrimack.simplechat.common.Framing;
import edu.merrimack.simplechat.common.protocol.BaseMessage;
import edu.merrimack.simplechat.common.protocol.ChatMessage;
import edu.merrimack.simplechat.common.protocol.ConnectAckMessage;
import edu.merrimack.simplechat.common.protocol.ConnectMessage;
import edu.merrimack.simplechat.common.protocol.DisconnectMessage;
import edu.merrimack.simplechat.common.protocol.ErrorMessage;
import edu.merrimack.simplechat.common.protocol.MessageParser;
import edu.merrimack.simplechat.common.protocol.MessageType;
import edu.merrimack.simplechat.common.protocol.ServerBroadcastMessage;
import edu.merrimack.simplechat.common.protocol.SetUsernameMessage;
import edu.merrimack.simplechat.common.protocol.ListUsersMessage;
import edu.merrimack.simplechat.common.protocol.UserListMessage;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Socket;
import java.util.UUID;

/**
 * Lightweight client wrapper that handles connection setup and messaging helpers.
 */
public class ChatClient {

    private final String host;
    private final int port;
    private final String username;
    private Socket socket;
    private ClientReceiver receiver;

    /**
     * Creates a chat client bound to the given host/port and presenting the provided username.
     */
    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    /**
     * Establishes a socket connection, performs the handshake, and starts the receive thread.
     */
    public void connect() throws IOException, InvalidObjectException {
        socket = new Socket(host, port);

        ConnectMessage connect = new ConnectMessage(UUID.randomUUID().toString(), username);
        send(connect);

        String ackJson = Framing.readFrame(socket.getInputStream());
        if (ackJson == null) {
            throw new InvalidObjectException("No response from server");
        }
        BaseMessage msg = MessageParser.parse(ackJson);
        if (msg.getType() != MessageType.CONNECT_ACK) {
            throw new InvalidObjectException("Unexpected handshake response");
        }
        ConnectAckMessage ack = (ConnectAckMessage) msg;
        if (!"OK".equalsIgnoreCase(ack.getStatus())) {
            throw new InvalidObjectException("Connection rejected: " + ack.getMessageText());
        }

        receiver = new ClientReceiver(socket, this::handleIncoming);
        new Thread(receiver, "client-receiver").start();
    }

    /**
     * Serializes and transmits a protocol message to the server.
     */
    public void send(BaseMessage message) throws IOException {
        byte[] frame = Framing.frame(message.serialize());
        socket.getOutputStream().write(frame);
        socket.getOutputStream().flush();
    }

    /**
     * Sends a broadcast chat message to all connected users.
     */
    public void sendChatToAll(String content) {
        ChatMessage msg = new ChatMessage(username, null, false, content);
        sendWithFriendlyError(msg, "send your message");
    }

    /**
     * Sends a direct message to the specified recipient.
     */
    public void sendDirect(String to, String content) {
        ChatMessage msg = new ChatMessage(username, to, true, content);
        sendWithFriendlyError(msg, "send your direct message to " + to);
    }

    /**
     * Requests that the server update this session's username.
     */
    public void requestUsernameChange(String newName) {
        SetUsernameMessage msg = new SetUsernameMessage(newName);
        sendWithFriendlyError(msg, "change your username");
    }

    /** Requests the list of currently connected users. */
    public void requestUserList() {
        ListUsersMessage msg = new ListUsersMessage();
        sendWithFriendlyError(msg, "fetch the user list");
    }

    /**
     * Gracefully disconnects by notifying the server and closing the socket.
     */
    public void disconnect() {
        try {
            send(new DisconnectMessage("client_exit"));
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Simple console handling for incoming messages from the receiver thread.
     */
    private void handleIncoming(BaseMessage message) {
        if (message instanceof ChatMessage) {
            ChatMessage chat = (ChatMessage) message;
            if (chat.isDirect()) {
                System.out.println("[DM from " + chat.getFrom() + "] " + chat.getContent());
            } else {
                System.out.println("[" + chat.getFrom() + "] " + chat.getContent());
            }
        } else if (message instanceof ServerBroadcastMessage) {
            System.out.println("[SERVER] " + ((ServerBroadcastMessage) message).getContent());
        } else if (message instanceof ErrorMessage) {
            System.out.println("[ERROR] " + ((ErrorMessage) message).getCode() + ": " + ((ErrorMessage) message).getMessageText());
        } else if (message instanceof DisconnectMessage) {
            System.out.println("[SERVER] Disconnect: " + ((DisconnectMessage) message).getReason());
            disconnect();
        } else if (message instanceof UserListMessage) {
            UserListMessage list = (UserListMessage) message;
            System.out.println("[USERS] " + String.join(", ", list.getUsers()));
        }
    }

    /**
     * Helper to send a message and report user-friendly errors when the network is unavailable.
     */
    private void sendWithFriendlyError(BaseMessage message, String action) {
        try {
            send(message);
        } catch (IOException e) {
            System.out.printf("Could not %s. Please check your connection and try again. (%s)%n", action, e.getMessage());
        }
    }
}
