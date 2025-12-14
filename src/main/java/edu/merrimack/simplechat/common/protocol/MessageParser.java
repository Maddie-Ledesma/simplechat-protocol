package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.InvalidJSONException;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Utility to parse raw JSON strings into strongly-typed protocol messages.
 */
public final class MessageParser {

    /** Utility class; not meant to be instantiated. */
    private MessageParser() {
    }

    /**
     * Parses raw JSON into a specific message type and validates it.
     */
    public static BaseMessage parse(String json) throws InvalidObjectException {
        JSONObject obj;
        try {
            obj = JsonIO.readObject(json);
        } catch (InvalidJSONException e) {
            throw new InvalidObjectException("Bad JSON: " + e.getMessage());
        }

        String typeStr;
        try {
            typeStr = obj.getString("type");
        } catch (Exception e) {
            throw new InvalidObjectException("Missing type field");
        }

        MessageType type;
        try {
            type = MessageType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException("Unknown message type: " + typeStr);
        }

        BaseMessage message;
        switch (type) {
            case CONNECT:
                message = new ConnectMessage();
                break;
            case CONNECT_ACK:
                message = new ConnectAckMessage();
                break;
            case SET_USERNAME:
                message = new SetUsernameMessage();
                break;
            case LIST_USERS:
                message = new ListUsersMessage();
                break;
            case USER_LIST:
                message = new UserListMessage();
                break;
            case CHAT_MESSAGE:
                message = new ChatMessage();
                break;
            case SERVER_BROADCAST:
                message = new ServerBroadcastMessage();
                break;
            case ERROR:
                message = new ErrorMessage();
                break;
            case DISCONNECT:
                message = new DisconnectMessage();
                break;
            default:
                throw new InvalidObjectException("Unsupported type: " + type);
        }

        message.deserialize(obj);
        MessageValidator.validate(message);
        return message;
    }
}
