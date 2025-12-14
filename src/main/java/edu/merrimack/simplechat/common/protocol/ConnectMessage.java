package edu.merrimack.simplechat.common.protocol;

import edu.merrimack.simplechat.common.ProtocolConstants;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Client handshake message sent when first connecting to the server.
 */
public class ConnectMessage extends BaseMessage {

    private String clientId;
    private String username;
    private String version;

    /**
     * No-arg constructor for JSON deserialization.
     */
    public ConnectMessage() {
        super(MessageType.CONNECT);
    }

    /**
     * Constructs a client handshake with caller-supplied identifier and optional username.
     */
    public ConnectMessage(String clientId, String username) {
        super(MessageType.CONNECT);
        this.clientId = clientId;
        this.username = username;
        this.version = ProtocolConstants.VERSION;
    }

    /** Unique client identifier supplied by the caller. */
    public String getClientId() {
        return clientId;
    }

    /** Requested username (may be null/blank for default handling). */
    public String getUsername() {
        return username;
    }

    /** Protocol version the client supports. */
    public String getVersion() {
        return version;
    }

    /**
     * Serializes the connect message to JSON.
     */
    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        obj.put("clientId", clientId);
        obj.put("username", username);
        obj.put("version", version);
        return obj;
    }

    /**
     * Loads fields from JSON.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("CONNECT expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            this.clientId = obj.getString("clientId");
            this.username = obj.getString("username");
            this.version = obj.getString("version");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid CONNECT: " + e.getMessage());
        }
    }
}
