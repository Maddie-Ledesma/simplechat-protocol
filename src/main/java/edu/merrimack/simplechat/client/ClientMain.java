package edu.merrimack.simplechat.client;

import edu.merrimack.simplechat.common.config.HostEntry;
import edu.merrimack.simplechat.common.config.HostsConfig;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point for the console chat client; parses CLI options and drives interactive commands.
 */
public class ClientMain {

    /** Prints CLI usage help for launching the client. */
    private static void printUsage() {
        System.out.println("Usage: java -jar simplechat-protocol-<version>-client.jar [options]");
        System.out.println("Options:");
        System.out.println("  --host <hostname>      Server host (default 127.0.0.1)");
        System.out.println("  --port <port>          Server port (default 9000)");
        System.out.println("  --name <alias>         Lookup host/port by alias in hosts file");
        System.out.println("  --username <name>      Username to present to the server (default guest)");
        System.out.println("  --hosts <path>         Path to hosts file (default ./hosts.json)");
        System.out.println("  --help                 Show this help and exit");
        System.out.println();
        printCommandHelp();
    }

    /** Prints available in-session slash commands. */
    private static void printCommandHelp() {
        System.out.println("Commands during session:");
        System.out.println("  /all <message>         Broadcast to all users");
        System.out.println("  /dm <user> <message>   Direct message a user");
        System.out.println("  /name <new>            Request a username change");
        System.out.println("  /list                  Show connected users");
        System.out.println("  /help                  Show this command list");
        System.out.println("  /quit                  Disconnect and exit");
    }

    /**
     * Parses CLI options, connects to the server, and runs the interactive loop.
     */
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 9000;
        String alias = null;
        String username = "guest";
        String hostsPath = "./hosts.json";
        boolean showUsage = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    host = args[++i];
                    break;
                case "--port":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("Port must be a number");
                        showUsage = true;
                    }
                    break;
                case "--name":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    alias = args[++i];
                    break;
                case "--username":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    username = args[++i];
                    break;
                case "--hosts":
                    if (i + 1 >= args.length) {
                        showUsage = true;
                        break;
                    }
                    hostsPath = args[++i];
                    break;
                case "--help":
                    printUsage();
                    return;
                default:
                    System.err.println("Unknown arg: " + args[i]);
                    showUsage = true;
            }
        }

        if (showUsage) {
            printUsage();
            return;
        }

        if (alias != null) {
            try {
                HostsConfig cfg = HostsConfig.load(hostsPath);
                Optional<HostEntry> entry = cfg.findByAlias(alias);
                if (entry.isEmpty()) {
                    System.err.println("Alias not found: " + alias);
                    printUsage();
                    return;
                }
                host = entry.get().getHost();
                port = entry.get().getPort();
            } catch (FileNotFoundException e) {
                System.err.println("hosts file not found: " + hostsPath);
                printUsage();
                return;
            } catch (Exception e) {
                System.err.println("Failed to read hosts file: " + e.getMessage());
                printUsage();
                return;
            }
        }

        ChatClient client = new ChatClient(host, port, username);
        try {
            client.connect();
            System.out.printf("Connected to %s:%d as %s%n", host, port, username);
        } catch (Exception e) {
            System.err.printf("Could not connect to %s:%d. Is the server running and reachable? (%s)%n", host, port, e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            printCommandHelp();
            while (true) {
                String line = scanner.nextLine();
                if (line == null) {
                    break;
                }
                try {
                    handleCommand(line, client);
                } catch (Exception e) {
                    System.out.printf("Sorry, that command failed: %s%n", e.getMessage());
                }
            }
        }
    }

    /**
     * Processes a single user-entered command, emitting friendly feedback for unknown or malformed commands.
     */
    private static void handleCommand(String line, ChatClient client) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (line.startsWith("/help")) {
            printCommandHelp();
        } else if (line.startsWith("/quit")) {
            client.disconnect();
            System.out.println("Disconnected. Bye!");
            System.exit(0);
        } else if (line.startsWith("/all ")) {
            String msg = line.substring(5);
            client.sendChatToAll(msg);
        } else if (line.startsWith("/dm ")) {
            String[] parts = line.split("\\s+", 3);
            if (parts.length < 3) {
                System.out.println("Usage: /dm <user> <message>");
                return;
            }
            client.sendDirect(parts[1], parts[2]);
        } else if (line.startsWith("/name ")) {
            String newName = line.substring(6);
            client.requestUsernameChange(newName);
        } else if (line.startsWith("/list")) {
            client.requestUserList();
        } else {
            System.out.println("Unknown command. Type /help for the command list.");
        }
    }
}
