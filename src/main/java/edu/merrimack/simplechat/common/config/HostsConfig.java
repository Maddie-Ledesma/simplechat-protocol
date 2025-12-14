package edu.merrimack.simplechat.common.config;

import edu.merrimack.simplechat.common.JsonSerializable;
import merrimackutil.json.InvalidJSONException;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the hosts.json file used by the client to resolve aliases to host/port pairs.
 */
public class HostsConfig implements JsonSerializable {

    private final List<HostEntry> hosts = new ArrayList<>();

    /** Default constructor used prior to populating hosts list. */
    public HostsConfig() {
    }

    /**
     * Loads hosts configuration from disk.
     */
    public static HostsConfig load(String path) throws FileNotFoundException, InvalidJSONException, InvalidObjectException {
        JSONObject obj = JsonIO.readObject(new File(path));
        HostsConfig config = new HostsConfig();
        config.deserialize(obj);
        return config;
    }

    /**
     * Finds a host entry by alias, case-insensitive.
     */
    public Optional<HostEntry> findByAlias(String alias) {
        return hosts.stream().filter(h -> h.getAlias().equalsIgnoreCase(alias)).findFirst();
    }

    /**
     * Returns an immutable copy of configured hosts.
     */
    public List<HostEntry> getHosts() {
        return List.copyOf(hosts);
    }

    /**
     * Serializes the hosts list to JSON.
     */
    @Override
    public merrimackutil.json.types.JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (HostEntry entry : hosts) {
            array.add(entry.toJSONType());
        }
        obj.put("hosts", array);
        return obj;
    }

    /**
     * Populates hosts list from a JSON object.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Expected JSON object");
        }
        JSONObject obj = (JSONObject) jsonType;
        try {
            JSONArray array = obj.getArray("hosts");
            for (int i = 0; i < array.size(); i++) {
                HostEntry entry = new HostEntry();
                entry.deserialize(array.getObject(i));
                hosts.add(entry);
            }
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid hosts config: " + e.getMessage());
        }
    }
}
