package edu.merrimack.simplechat.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Networking helpers for safe reads and closure.
 */
public final class NetUtil {

    /** Utility class; instantiation not supported. */
    private NetUtil() {
    }

    /**
     * Attempts to fill the buffer completely, returning bytes read or -1 if EOF before any byte.
     */
    public static int readFully(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;
        int remaining = buffer.length;
        while (remaining > 0) {
            int read = in.read(buffer, offset, remaining);
            if (read == -1) {
                return offset == 0 ? -1 : offset;
            }
            offset += read;
            remaining -= read;
        }
        return offset;
    }

    /**
     * Closes a Closeable while suppressing checked exceptions.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
}
