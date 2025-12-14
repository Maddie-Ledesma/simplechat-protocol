package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Represents user-authored chat content, either broadcast or direct.
 */
public class ChatMessage extends BaseMessage {

    private String from;
    private String to;
    private boolean direct;
    private String content;

    /**
     * No-arg constructor for JSON deserialization.
     */
    public ChatMessage() {
        super(MessageType.CHAT_MESSAGE);
    }

    /**
     * Creates a chat message authored by {@code from} destined to either all users or {@code to} when direct.
     */
    public ChatMessage(String from, String to, boolean direct, String content) {
        super(MessageType.CHAT_MESSAGE);
        this.from = from;
        this.to = to;
        this.direct = direct;
        this.content = content;
    }

    /** Sender username. */
    public String getFrom() {
        return from;
    }

    /** Target username when {@code direct} is true; null when broadcast. */
    public String getTo() {
        return to;
    }

    /** Whether the message is a direct message. */
    public boolean isDirect() {
        return direct;
    }

    /** Chat body text. */
    public String getContent() {
        return content;
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("from", from);
        obj.put("to", to);
        obj.put("direct", direct);
        obj.put("content", content);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("CHAT_MESSAGE expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.from = obj.getString("from");
            this.to = obj.getString("to");
            this.direct = obj.getBoolean("direct");
            this.content = obj.getString("content");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid CHAT_MESSAGE: " + e.getMessage());
        }
    }
}
