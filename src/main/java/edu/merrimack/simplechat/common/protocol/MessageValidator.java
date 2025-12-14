package edu.merrimack.simplechat.common.protocol;

import edu.merrimack.simplechat.common.ProtocolConstants;

import java.io.InvalidObjectException;

/**
 * Validates message fields against protocol constraints.
 */
public final class MessageValidator {

    /** Utility class; prevent instantiation. */
    private MessageValidator() {
    }

    /**
     * Validates a fully deserialized message for required fields and constraints.
     */
    public static void validate(BaseMessage message) throws InvalidObjectException {
        if (message.getTimestamp() <= 0) {
            throw new InvalidObjectException("timestamp missing");
        }

        switch (message.getType()) {
            case CONNECT:
                validateConnect((ConnectMessage) message);
                break;
            case CONNECT_ACK:
                validateConnectAck((ConnectAckMessage) message);
                break;
            case SET_USERNAME:
                validateUsername(((SetUsernameMessage) message).getUsername());
                break;
            case CHAT_MESSAGE:
                validateChat((ChatMessage) message);
                break;
            case LIST_USERS:
                // No payload to validate; presence of timestamp already checked.
                break;
            case USER_LIST:
                validateUserList((UserListMessage) message);
                break;
            case SERVER_BROADCAST:
                validateContent(((ServerBroadcastMessage) message).getContent(), "content");
                break;
            case ERROR:
                ErrorMessage err = (ErrorMessage) message;
                validateContent(err.getCode(), "code");
                validateContent(err.getMessageText(), "message");
                break;
            case DISCONNECT:
                // reason may be null/blank; no strict requirement
                break;
            default:
                throw new InvalidObjectException("Unhandled type");
        }
    }

    /**
     * Ensures the user list is present; empty list is permitted.
     */
    private static void validateUserList(UserListMessage msg) throws InvalidObjectException {
        if (msg.getUsers() == null) {
            throw new InvalidObjectException("users required");
        }
    }

    /**
     * Ensures connect message fields are present and version matches.
     */
    private static void validateConnect(ConnectMessage msg) throws InvalidObjectException {
        validateContent(msg.getClientId(), "clientId");
        if (msg.getVersion() == null || !ProtocolConstants.VERSION.equals(msg.getVersion())) {
            throw new InvalidObjectException("Unsupported version");
        }
        String username = msg.getUsername();
        if (username != null && !username.isBlank()) {
            validateUsername(username);
        }
    }

    /**
     * Ensures connect ack includes status and message.
     */
    private static void validateConnectAck(ConnectAckMessage msg) throws InvalidObjectException {
        validateContent(msg.getStatus(), "status");
        validateContent(msg.getMessageText(), "message");
    }

    /**
     * Validates chat message routing and content rules.
     */
    private static void validateChat(ChatMessage msg) throws InvalidObjectException {
        validateUsername(msg.getFrom());
        validateContent(msg.getContent(), "content");
        if (msg.isDirect()) {
            validateUsername(msg.getTo());
        }
    }

    /**
     * Enforces username length and character whitelist.
     */
    private static void validateUsername(String username) throws InvalidObjectException {
        if (username == null) {
            throw new InvalidObjectException("username required");
        }
        String trimmed = username.trim();
        if (trimmed.length() < ProtocolConstants.MIN_USERNAME_LENGTH || trimmed.length() > ProtocolConstants.MAX_USERNAME_LENGTH) {
            throw new InvalidObjectException("username length invalid");
        }
        if (!trimmed.matches("^[A-Za-z0-9_.-]+$")) {
            throw new InvalidObjectException("username contains invalid characters");
        }
    }

    /**
     * Validates non-empty textual content and length limits.
     */
    private static void validateContent(String value, String field) throws InvalidObjectException {
        if (value == null) {
            throw new InvalidObjectException(field + " required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidObjectException(field + " cannot be empty");
        }
        if (trimmed.length() > ProtocolConstants.MAX_CONTENT_LENGTH) {
            throw new InvalidObjectException(field + " too long");
        }
    }
}
