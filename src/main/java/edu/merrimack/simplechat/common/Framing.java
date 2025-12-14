package edu.merrimack.simplechat.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Implements 4-byte big-endian length-prefixed framing.
 */
public final class Framing {

    /** Utility class; prevent instantiation. */
    private Framing() {
    }

    /**
     * Frames a UTF-8 JSON string into a byte array with a 4-byte length prefix.
     */
    public static byte[] frame(String json) {
        byte[] payload = json.getBytes(ProtocolConstants.UTF8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + payload.length);
        buffer.putInt(payload.length);
        buffer.put(payload);
        return buffer.array();
    }

    /**
     * Reads a single framed message from the input stream. Returns null if EOF is reached cleanly.
     */
    public static String readFrame(InputStream inputStream) throws IOException {
        byte[] lenBytes = new byte[4];
        int readLen = NetUtil.readFully(inputStream, lenBytes);
        if (readLen == -1) {
            return null;
        }
        if (readLen < 4) {
            throw new IOException("Incomplete frame length");
        }

        ByteBuffer lenBuffer = ByteBuffer.wrap(lenBytes);
        int length = lenBuffer.getInt();
        if (length < 0) {
            throw new IOException("Negative frame length");
        }

        byte[] payload = new byte[length];
        int readPayload = NetUtil.readFully(inputStream, payload);
        if (readPayload < length) {
            throw new IOException("Incomplete frame payload");
        }

        return new String(payload, ProtocolConstants.UTF8);
    }
}
