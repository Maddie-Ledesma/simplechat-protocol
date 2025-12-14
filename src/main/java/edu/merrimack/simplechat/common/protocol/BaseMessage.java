package edu.merrimack.simplechat.common.protocol;

import edu.merrimack.simplechat.common.JsonSerializable;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Common base for all protocol messages carrying type and timestamp metadata.
 */
public abstract class BaseMessage implements JsonSerializable {

    protected MessageType type;
    protected long timestamp;

    /**
     * Constructs a message with the provided type and captures the current timestamp.
     */
    protected BaseMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Framework-only constructor for reflective deserialization.
     */
    protected BaseMessage() {
    }

    /**
     * Returns the message type.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns the message timestamp in epoch milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Serializes common fields to JSON.
     */
    protected JSONObject baseToJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", type.name());
        obj.put("timestamp", timestamp);
        return obj;
    }

    /**
     * Reads common fields from JSON payload.
     */
    protected void baseFromJson(JSONObject obj) throws InvalidObjectException {
        try {
            this.type = MessageType.valueOf(obj.getString("type"));
            this.timestamp = obj.getLong("timestamp");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid base message: " + e.getMessage());
        }
    }

    @Override
    public abstract JSONType toJSONType();

    @Override
    public abstract void deserialize(JSONType jsonType) throws InvalidObjectException;
}
