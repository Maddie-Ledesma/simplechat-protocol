package edu.merrimack.simplechat.common.config;

import edu.merrimack.simplechat.common.JsonSerializable;
import merrimackutil.json.InvalidJSONException;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;

/**
 * Represents the server configuration file with port, logging, and capacity settings.
 */
public class ServerConfig implements JsonSerializable {

    private int port;
    private String logFile;
    private int maxClients;

    /** Default constructor for JSON deserialization. */
    public ServerConfig() {
    }

    /** Builds a config with explicit port, log file path, and max client count. */
    public ServerConfig(int port, String logFile, int maxClients) {
        this.port = port;
        this.logFile = logFile;
        this.maxClients = maxClients;
    }

    /**
     * Loads and validates server configuration from disk.
     */
    public static ServerConfig load(String path) throws FileNotFoundException, InvalidJSONException, InvalidObjectException {
        JSONObject obj = JsonIO.readObject(new File(path));
        ServerConfig config = new ServerConfig();
        config.deserialize(obj);
        config.validate();
        return config;
    }

    /**
     * Ensures all required values are present and in range.
     */
    private void validate() throws InvalidObjectException {
        if (port < 1025 || port > 65535) {
            throw new InvalidObjectException("Port must be between 1025 and 65535");
        }
        if (logFile == null || logFile.isBlank()) {
            throw new InvalidObjectException("logFile is required");
        }
        if (maxClients <= 0) {
            throw new InvalidObjectException("maxClients must be positive");
        }
    }

    /**
     * Server listen port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Log file path for server logging.
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * Maximum concurrent clients allowed.
     */
    public int getMaxClients() {
        return maxClients;
    }

    /**
     * Serializes the configuration to JSON.
     */
    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("port", port);
        obj.put("logFile", logFile);
        obj.put("maxClients", maxClients);
        return obj;
    }

    /**
     * Populates fields from JSON without running validation.
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Expected JSON object");
        }
        JSONObject obj = (JSONObject) jsonType;
        try {
            this.port = obj.getInt("port");
            this.logFile = obj.getString("logFile");
            this.maxClients = obj.getInt("maxClients");
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid server config: " + e.getMessage());
        }
    }
}
