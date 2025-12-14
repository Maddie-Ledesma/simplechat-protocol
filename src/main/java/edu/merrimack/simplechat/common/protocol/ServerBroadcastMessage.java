package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Server-emitted broadcast message delivered to all connected clients.
 */
public class ServerBroadcastMessage extends BaseMessage {

    private String content;

    /** No-arg constructor for JSON deserialization. */
    public ServerBroadcastMessage() {
        super(MessageType.SERVER_BROADCAST);
    }

    /** Creates a broadcast carrying the given content string. */
    public ServerBroadcastMessage(String content) {
        super(MessageType.SERVER_BROADCAST);
        this.content = content;
    }

    /**
     * Returns broadcast text.
     */
    public String getContent() {
        return content;
    }

    /**
     * Serializes the broadcast to JSON.
     */
    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("content", content);
        return obj;
    }

    /**
     * Loads fields from JSON.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("SERVER_BROADCAST expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.content = obj.getString("content");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid SERVER_BROADCAST: " + e.getMessage());
        }
    }
}
