package edu.merrimack.simplechat.protocol;

import edu.merrimack.simplechat.common.protocol.BaseMessage;
import edu.merrimack.simplechat.common.protocol.ChatMessage;
import edu.merrimack.simplechat.common.protocol.ConnectMessage;
import edu.merrimack.simplechat.common.protocol.MessageParser;
import org.junit.jupiter.api.Test;

import java.io.InvalidObjectException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that messages can round-trip through serialization and parsing.
 */
public class MessageParserTest {

    @Test
    /** Ensures chat messages preserve fields after serialization and parsing. */
    void chatMessageRoundTrip() throws InvalidObjectException {
        ChatMessage original = new ChatMessage("alice", "bob", true, "hello");
        String json = original.serialize();
        BaseMessage parsed = MessageParser.parse(json);
        ChatMessage chat = (ChatMessage) parsed;
        assertEquals(original.getFrom(), chat.getFrom());
        assertEquals(original.getTo(), chat.getTo());
        assertEquals(original.getContent(), chat.getContent());
        assertEquals(original.isDirect(), chat.isDirect());
    }

    @Test
    /** Ensures connect messages survive a round-trip through JSON. */
    void connectRoundTrip() throws InvalidObjectException {
        ConnectMessage original = new ConnectMessage("client-1", "alice");
        String json = original.serialize();
        BaseMessage parsed = MessageParser.parse(json);
        ConnectMessage connect = (ConnectMessage) parsed;
        assertEquals(original.getClientId(), connect.getClientId());
        assertEquals(original.getUsername(), connect.getUsername());
        assertNotNull(connect.getVersion());
    }
}
