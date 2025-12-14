package edu.merrimack.simplechat.common.protocol;

import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server response that carries the list of currently connected usernames.
 */
public class UserListMessage extends BaseMessage {

    private final List<String> users = new ArrayList<>();

    /** No-arg constructor for JSON deserialization. */
    public UserListMessage() {
        super(MessageType.USER_LIST);
    }

    public UserListMessage(List<String> usernames) {
        this();
        if (usernames != null) {
            users.addAll(usernames);
        }
    }

    /** Immutable view of connected usernames. */
    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = baseToJson();
        JSONArray array = new JSONArray();
        for (String user : users) {
            array.add(user);
        }
        obj.put("users", array);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("USER_LIST expects object");
        }
        JSONObject obj = (JSONObject) jsonType;
        baseFromJson(obj);
        try {
            JSONArray array = obj.getArray("users");
            for (int i = 0; i < array.size(); i++) {
                users.add(array.getString(i));
            }
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid USER_LIST: " + e.getMessage());
        }
    }
}
