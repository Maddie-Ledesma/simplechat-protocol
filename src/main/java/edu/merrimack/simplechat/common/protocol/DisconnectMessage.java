package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Sent by client to indicate it is closing the connection.
 */
public class DisconnectMessage extends BaseMessage {

    private String reason;

    /** No-arg constructor for JSON deserialization. */
    public DisconnectMessage() {
        super(MessageType.DISCONNECT);
    }

    /** Creates a disconnect notice with an optional reason string. */
    public DisconnectMessage(String reason) {
        super(MessageType.DISCONNECT);
        this.reason = reason;
    }

    /** Human-readable explanation of why the client disconnects. */
    public String getReason() {
        return reason;
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("reason", reason);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("DISCONNECT expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.reason = obj.getString("reason");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid DISCONNECT: " + e.getMessage());
        }
    }
}
