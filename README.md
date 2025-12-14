# SimpleChat Protocol (SCP) v1

SimpleChat Protocol is a custom JSON-based chat protocol implemented with Java and Gradle. It uses TCP transport with a 4-byte big-endian length prefix for framing. All JSON serialization/deserialization is provided by `merrimackutil.jar`.

## Features
- Multi-threaded TCP server with configurable port, log file, and client cap.
- CLI client with `/all`, `/dm`, `/list`, and `/quit` commands.
- Custom SCP v1 message types with strict validation.
- JSON handling exclusively via `libs/merrimackutil.jar`.
- Logging through SLF4J + Logback to a configurable file.
- Included protocol documentation, project planning artifacts, and tests.

## Prerequisites
- Java 17+
- Gradle (wrapper optional; use system Gradle if available)
- `libs/merrimackutil.jar` is already included in the repository.

## Building
```bash
./gradlew clean build
```
This produces two fat JARs with dependencies under `build/libs/`:
- `simplechat-protocol-1.0.0-server.jar`
- `simplechat-protocol-1.0.0-client.jar`

## Running the Server
```bash
java -jar build/libs/simplechat-protocol-1.0.0-server.jar --config ./server-config.json
```
- Default config path: `./server-config.json`.
- Example config: `server-config.example.json`.

## Running the Client
```bash
java -jar build/libs/simplechat-protocol-1.0.0-client.jar --host 127.0.0.1 --port 9000 --username alice
# or use a hosts alias
java -jar build/libs/simplechat-protocol-1.0.0-client.jar --name local --username alice
```
- Host aliases are resolved using `hosts.json` (see `hosts.example.json`).

## Commands (Client)
- `/all <message>`: broadcast to all connected users.
- `/dm <user> <message>`: direct message a specific user.
- `/list`: display currently connected usernames.
- `/quit`: cleanly disconnect from the server.

## Configuration
`server-config.json` fields:
- `port` (>=1025)
- `logFile` (path to log file)
- `maxClients` (positive integer)

`hosts.json` fields:
- `hosts`: array of `{ "alias": "...", "host": "...", "port": 1234 }`.

## Testing
```bash
./gradlew test
```
Tests cover framing and JSON message round-trips via merrimackutil.

## Logging
The server config drives the log destination. All server output is routed to the configured log file; client logs to stdout.

## Repository Layout
- `src/main/java`: protocol, server, and client code.
- `src/main/resources`: logging defaults.
- `docs`: protocol spec, diagrams, analysis, presentation outline, project management.

## Notes
- SCP v1 is intentionally simple and extensible; see `docs/protocol.md` for details.
- Ensure firewall rules permit TCP on the configured port.

## Authors
- Tarun Kannan
- Aidan Menzie
- Maddie Ledesma
