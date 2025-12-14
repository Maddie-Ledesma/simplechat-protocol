package edu.merrimack.simplechat.common;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests covering framing and deframing of JSON payloads.
 */
public class FramingTest {

    @Test
    /** Verifies that framing then reading returns the original JSON string. */
    void frameAndRead() throws IOException {
        String json = "{\"type\":\"PING\"}";
        byte[] framed = Framing.frame(json);
        ByteArrayInputStream in = new ByteArrayInputStream(framed);
        String read = Framing.readFrame(in);
        assertEquals(json, read);
    }

    @Test
    /** Confirms the 4-byte prefix matches payload length. */
    void frameIncludesLength() {
        String json = "{}";
        byte[] framed = Framing.frame(json);
        int length = ((framed[0] & 0xFF) << 24) | ((framed[1] & 0xFF) << 16) | ((framed[2] & 0xFF) << 8) | (framed[3] & 0xFF);
        assertEquals(json.getBytes(ProtocolConstants.UTF8).length, length);
    }
}
