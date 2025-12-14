package edu.merrimack.simplechat.server;

import edu.merrimack.simplechat.common.LogUtil;
import edu.merrimack.simplechat.common.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;

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

        try {
            ServerConfig config = ServerConfig.load(configPath);
            LogUtil.configureLogging(config.getLogFile());
            log.info("Loaded config from {}", configPath);

            ChatServer server = new ChatServer(config);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        } catch (InvalidObjectException e) {
            System.err.println("Invalid config: " + e.getMessage());
        } catch (Exception e) {
            log.error("Fatal server error", e);
        }
    }
}
