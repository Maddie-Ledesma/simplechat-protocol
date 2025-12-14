package edu.merrimack.simplechat.common.config;

import edu.merrimack.simplechat.common.JsonSerializable;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * Represents a single host alias entry from the hosts configuration file.
 */
public class HostEntry implements JsonSerializable {

    private String alias;
    private String host;
    private int port;

    /** Default constructor for JSON deserialization. */
    public HostEntry() {
    }

    /** Creates a host entry with explicit alias, hostname, and port. */
    public HostEntry(String alias, String host, int port) {
        this.alias = alias;
        this.host = host;
        this.port = port;
    }

    /**
     * Alias key clients can reference.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Hostname or IP for the alias.
     */
    public String getHost() {
        return host;
    }

    /**
     * TCP port for the alias.
     */
    public int getPort() {
        return port;
    }

    /**
     * Serializes the entry to JSON.
     */
    @Override
    public merrimackutil.json.types.JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("alias", alias);
        obj.put("host", host);
        obj.put("port", port);
        return obj;
    }

    /**
     * Populates fields from a JSON object and validates required fields.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Expected JSON object");
        }
        JSONObject obj = (JSONObject) jsonType;
        try {
            this.alias = obj.getString("alias");
            this.host = obj.getString("host");
            this.port = obj.getInt("port");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid host entry: " + e.getMessage());
        }
        if (alias == null || alias.isBlank()) {
            throw new InvalidObjectException("Alias required");
        }
        if (host == null || host.isBlank()) {
            throw new InvalidObjectException("Host required");
        }
        if (port < 1 || port > 65535) {
            throw new InvalidObjectException("Port out of range");
        }
    }
}
