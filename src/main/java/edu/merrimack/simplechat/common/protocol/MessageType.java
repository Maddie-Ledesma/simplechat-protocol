package edu.merrimack.simplechat.common.protocol;

/**
 * Enumerates the supported protocol message types.
 */
public enum MessageType {
    CONNECT,
    CONNECT_ACK,
    SET_USERNAME,
    CHAT_MESSAGE,
    LIST_USERS,
    USER_LIST,
    SERVER_BROADCAST,
    ERROR,
    DISCONNECT
}
