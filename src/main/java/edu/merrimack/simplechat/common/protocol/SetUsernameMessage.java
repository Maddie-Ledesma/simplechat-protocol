package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Client request to change the current username.
 */
public class SetUsernameMessage extends BaseMessage {

    private String username;

    /** No-arg constructor for JSON deserialization. */
    public SetUsernameMessage() {
        super(MessageType.SET_USERNAME);
    }

    /** Builds a request to change to the supplied username. */
    public SetUsernameMessage(String username) {
        super(MessageType.SET_USERNAME);
        this.username = username;
    }

    /** Username requested by the client. */
    public String getUsername() {
        return username;
    }

    /**
     * Serializes the message to JSON.
     */
    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("username", username);
        return obj;
    }

    /**
     * Loads fields from JSON.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("SET_USERNAME expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.username = obj.getString("username");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid SET_USERNAME: " + e.getMessage());
        }
    }
}
