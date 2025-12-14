package edu.merrimack.simplechat.server;

import edu.merrimack.simplechat.common.Framing;
import edu.merrimack.simplechat.common.NetUtil;
import edu.merrimack.simplechat.common.config.ServerConfig;
import edu.merrimack.simplechat.common.protocol.ErrorMessage;
import edu.merrimack.simplechat.common.protocol.MessageValidator;
import edu.merrimack.simplechat.common.protocol.ServerBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Multi-threaded chat server with length-prefixed JSON messaging.
 */
public class ChatServer {

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private final ServerConfig config;
    private final ClientRegistry registry = new ClientRegistry();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private ExecutorService executor;

    /** Constructs a server instance using the provided configuration. */
    public ChatServer(ServerConfig config) {
        this.config = config;
    }

    /**
     * Binds the server socket, accepts clients, and hands each to a handler until stopped.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(config.getPort());
        executor = Executors.newCachedThreadPool();
        running.set(true);
        log.info("Server listening on port {}", config.getPort());

        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                if (registry.size() >= config.getMaxClients()) {
                    rejectClient(socket, "SERVER_BUSY", "Server is at capacity");
                    continue;
                }
                ClientHandler handler = new ClientHandler(socket, registry);
                executor.submit(handler);
            } catch (SocketException se) {
                if (!running.get()) {
                    break;
                }
                throw se;
            }
        }
    }

    /**
     * Sends an error response to a would-be client and closes the socket.
     */
    private void rejectClient(Socket socket, String code, String message) {
        try (OutputStream out = socket.getOutputStream()) {
            ErrorMessage error = new ErrorMessage(code, message);
            MessageValidator.validate(error);
            out.write(Framing.frame(error.serialize()));
            out.flush();
        } catch (Exception ignored) {
        } finally {
            NetUtil.closeQuietly(socket);
        }
    }

    /**
     * Stops accepting new clients, terminates worker threads, and notifies connected users.
     */
    public void stop() {
        running.set(false);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        broadcastSystem("Server shutting down");
    }

    /**
     * Broadcasts a server-generated message to all connected clients.
     */
    private void broadcastSystem(String content) {
        ServerBroadcastMessage msg = new ServerBroadcastMessage(content);
        for (ClientHandler handler : registry.all()) {
            handler.send(msg);
        }
    }
}
