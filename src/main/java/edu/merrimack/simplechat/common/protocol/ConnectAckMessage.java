package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Server response to a CONNECT handshake indicating status and message.
 */
public class ConnectAckMessage extends BaseMessage {

    private String status;
    private String message;

    /** No-arg constructor for JSON deserialization. */
    public ConnectAckMessage() {
        super(MessageType.CONNECT_ACK);
    }

    /** Builds an acknowledgement with status text and human-readable message. */
    public ConnectAckMessage(String status, String message) {
        super(MessageType.CONNECT_ACK);
        this.status = status;
        this.message = message;
    }

    /** Machine-readable status such as {@code OK} or {@code ERROR}. */
    public String getStatus() {
        return status;
    }

    /** Descriptive message paired with the status. */
    public String getMessageText() {
        return message;
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("status", status);
        obj.put("message", message);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("CONNECT_ACK expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.status = obj.getString("status");
            this.message = obj.getString("message");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid CONNECT_ACK: " + e.getMessage());
        }
    }
}
