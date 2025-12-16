package edu.merrimack.simplechat.server;

import edu.merrimack.simplechat.common.LogUtil;
import edu.merrimack.simplechat.common.config.ServerConfig;
import merrimackutil.json.InvalidJSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.net.BindException;
import java.net.SocketException;

/**
 * Entry point for the chat server; loads configuration and starts the server process.
 */
public class ServerMain {

    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

    /** Prints CLI usage for starting the server process. */
    private static void printUsage() {
        System.out.println("Usage: java -jar simplechat-protocol-<version>-server.jar [--config <path>] [--help]");
        System.out.println("Options:");
        System.out.println("  --config <path>   Path to server config JSON (default ./server-config.json)");
        System.out.println("  --help            Show this help and exit");
    }

    /**
     * Parses arguments, loads configuration, configures logging, and starts the server.
     */
    public static void main(String[] args) {
        String configPath = "./server-config.json";
        boolean showUsage = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    configPath = args[++i];
                    break;
                case "--help":
                    printUsage();
                    return;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    showUsage = true;
                    break;
            }
        }

        if (showUsage) {
            printUsage();
            return;
        }

        ServerConfig config;
        try {
            config = ServerConfig.load(configPath);
        } catch (FileNotFoundException e) {
            System.err.println("Config file not found at '" + configPath + "'. Use --config <path> to point to a valid file.");
            return;
        } catch (InvalidJSONException e) {
            System.err.println("Config file is not valid JSON: " + e.getMessage());
            return;
        } catch (InvalidObjectException e) {
            System.err.println("Config file is invalid: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("Unexpected error loading config: " + e.getMessage());
            return;
        }

        try {
            LogUtil.configureLogging(config.getLogFile());
            log.info("Loaded config from {}", configPath);
        } catch (Exception e) {
            System.err.println("Failed to configure logging. Check logFile path in config: " + e.getMessage());
            return;
        }

        ChatServer server = new ChatServer(config);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        try {
            server.start();
        } catch (BindException e) {
            System.err.println("Port " + config.getPort() + " is already in use. Update 'port' in " + configPath + " and restart.");
        } catch (SocketException e) {
            System.err.println("Network error while running the server: " + e.getMessage());
            log.error("Server socket error", e);
        } catch (Exception e) {
            System.err.println("Fatal server error: " + e.getMessage());
            log.error("Fatal server error", e);
        }
    }
}
