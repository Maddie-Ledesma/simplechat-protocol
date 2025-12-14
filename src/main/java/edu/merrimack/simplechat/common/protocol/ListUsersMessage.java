package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Client request asking the server to return the current list of connected usernames.
 */
public class ListUsersMessage extends BaseMessage {

    /** No-arg constructor for JSON deserialization. */
    public ListUsersMessage() {
        super(MessageType.LIST_USERS);
    }

    @Override
    public JSONType toJSONType() {
        // No additional fields beyond the type/timestamp envelope.
        return baseToJson();
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("LIST_USERS expects object");
        }
        baseFromJson((JSONObject) jsonType);
    }
}
