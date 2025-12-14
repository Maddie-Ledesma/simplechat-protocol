package edu.merrimack.simplechat.client;

import edu.merrimack.simplechat.common.Framing;
import edu.merrimack.simplechat.common.protocol.BaseMessage;
import edu.merrimack.simplechat.common.protocol.MessageParser;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Continuously reads framed messages from the server.
 */
public class ClientReceiver implements Runnable {

    private final Socket socket;
    private final Consumer<BaseMessage> consumer;

    /**
     * Builds a receiver that pulls frames from the given socket and forwards parsed messages.
     */
    public ClientReceiver(Socket socket, Consumer<BaseMessage> consumer) {
        this.socket = socket;
        this.consumer = consumer;
    }

    /**
     * Continuously reads framed JSON messages, parsing and forwarding them until the socket closes.
     */
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                String json = Framing.readFrame(socket.getInputStream());
                if (json == null) {
                    break;
                }
                try {
                    BaseMessage msg = MessageParser.parse(json);
                    consumer.accept(msg);
                } catch (InvalidObjectException e) {
                    System.err.println("Received invalid message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Receiver error: " + e.getMessage());
        }
    }
}
