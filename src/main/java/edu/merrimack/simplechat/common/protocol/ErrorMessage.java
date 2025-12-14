package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Error response message sent when validation or processing fails.
 */
public class ErrorMessage extends BaseMessage {

    private String code;
    private String message;

    /** No-arg constructor for JSON deserialization. */
    public ErrorMessage() {
        super(MessageType.ERROR);
    }

    /** Creates an error response with a short code and descriptive text. */
    public ErrorMessage(String code, String message) {
        super(MessageType.ERROR);
        this.code = code;
        this.message = message;
    }

    /** Short machine-readable error code. */
    public String getCode() {
        return code;
    }

    /** Human-readable error description. */
    public String getMessageText() {
        return message;
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("code", code);
        obj.put("message", message);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("ERROR expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.code = obj.getString("code");
            this.message = obj.getString("message");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid ERROR: " + e.getMessage());
        }
    }
}
