# SimpleChat Protocol (SCP) Specification

## 1. Introduction

The SimpleChat Protocol (SCP) is a lightweight, application-layer communication standard designed to facilitate real-time text exchange between a central server and multiple concurrent clients. Built atop the Transmission Control Protocol (TCP), SCP utilizes JavaScript Object Notation (JSON) for data serialization, ensuring that messages are both machine-parsable and human-readable. This specification defines version 1.0 of the protocol (SCP v1).

In an era dominated by complex, opaque messaging frameworks, SCP aims to provide a transparent model for educational and lightweight industrial applications. By enforcing strict message framing, deterministic state transitions, and robust error handling, the protocol serves as a reference implementation for reliable network programming patterns. This document outlines the architectural decisions, message structures, and operational rules governing the protocol.

## 2. Problem Statement

Developing a robust networked application requires solving challenges related to data delimiting, concurrency, and partial failures. Many ad-hoc protocols suffer from "TCP stream glue" issues—where multiple application messages are read as one—or fragmentation, where a single message arrives in pieces. Furthermore, without a strict schema, parsing logic often becomes brittle, leading to security vulnerabilities and unstable behavior.

SCP addresses these problems by enforcing a strict length-prefixed framing strategy and a rigid JSON schema. The goal is to eliminate ambiguity in the transport stream and provide a predictable contract between client and server, separating the concerns of transport reliability from application logic.

## 3. Design Goals

The architecture of SCP is driven by five primary objectives:

1.  **Clarity**: The protocol must be self-documenting. By using JSON with semantic field names, network traffic can be inspected and understood without specialized decoding tools.
2.  **Robustness**: The server must remain stable regardless of client behavior. Malformed input, unexpected disconnection, or protocol violations should result in defined error states rather than system crashes.
3.  **Determinism**: Message boundaries must be unambiguous. The use of length-prefix framing ensures that the receiver knows exactly how many bytes constitute a complete payload.
4.  **Extensibility**: The protocol design should allow for future enhancements—such as new message types or metadata—without breaking backward compatibility for fundamental transport operations.
5.  **Simplicity**: The implementation complexity should be minimized. SCP avoids binary packing, compression, or encryption at the protocol level, relying instead on the underlying transport guarantees and external tunneling for security if needed.

## 4. System Overview

The SimpleChat system follows a star topology (Client-Server model). A single logical server listens on a known TCP port, accepting connections from multiple clients. The server acts as the source of truth for user state and message routing.

*   **Client**: A user-agent that initiates a connection, negotiates a session, and interacts with the chat stream.
*   **Server**: The central node that manages socket connections, validates unique usernames, and broadcasts messages to connected peers.
*   **Wire Format**: All application data is serialized as UTF-8 encoded JSON objects.

The communication is asynchronous and bidirectional. Once a session is established, both client and server may send messages independently, though specific request-response patterns are mandated for the handshake and specific commands.

## 5. Transport Layer Selection

SCP exclusively runs over **TCP (Transmission Control Protocol)**. UDP was rejected due to the requirement for reliable, ordered delivery of chat history; missing or out-of-order messages would degrade the user experience.

While raw TCP provides a reliable stream of bytes, it does not respect message boundaries. Therefore, SCP implements an application-level framing layer to delimit JSON payloads. TLS (Transport Layer Security) is not enforced natively in SCP v1 to maintain simplicity for educational analysis, though production deployments are expected to tunnel traffic via SSH or wrap sockets in TLS/SSL.

## 6. SimpleChat Protocol (SCP v1)

The protocol operates in three distinct phases:
1.  **Connection Establishment**: The client connects and sends a handshake request.
2.  **Session Loop**: The client exchanges chat messages and commands.
3.  **Termination**: The session is closed via a disconnect message or socket termination.

All implementations must respect the sequence of these phases. A violation (e.g., sending a chat message before a successful handshake) results in immediate termination of the connection by the server.

## 7. Message Framing Strategy

To solve the issue of stream fragmentation, every SCP message is encapsulated in a length-prefixed frame.

**Frame Structure:**
```text
+----------------+------------------------------------------+
| Length (4 bytes)|               Payload (N bytes)          |
+----------------+------------------------------------------+
| Big Endian Int |          UTF-8 Encoded JSON Data         |
+----------------+------------------------------------------+
```

1.  **Length Header**: A 4-byte, signed integer (big-endian) indicating the number of bytes in the payload. Note that while the integer is signed in Java/DataInputStream, the protocol treats it as an unsigned quantity for logic, strictly positive.
2.  **Payload**: Exactly *N* bytes of JSON text.

Receivers must read the 4-byte header, interpret the length *L*, and then block until exactly *L* bytes have been read from the stream before attempting to parse the JSON. This prevents "partial JSON" parse errors.

## 8. Message Definitions

All JSON messages must include two standard fields:
*   `type`: A string literal identifying the message purpose.
*   `timestamp`: A 64-bit integer representing the number of milliseconds since the Unix Epoch (UTC).

### 8.1 CONNECT
Sent by the Client immediately upon opening the socket. It identifies the client and requests a session.

**JSON Structure:**
```json
{
  "type": "CONNECT",
  "timestamp": 1702483200000,
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "User123",
  "version": "1.0"
}
```

*   `clientId`: A unique UUID generated by the client to identify the instance.
*   `username`: (Optional) The requested handle. If omitted or empty, the server may assign a default.
*   `version`: Must effectively match "1.0".

### 8.2 CONNECT_ACK
Sent by the Server in response to `CONNECT`. This is the only message that can follow a `CONNECT` in the handshake phase.

**JSON Structure:**
```json
{
  "type": "CONNECT_ACK",
  "timestamp": 1702483200050,
  "status": "OK",
  "message": "Welcome to SimpleChat"
}
```

*   `status`: Either "OK" or "ERROR".
*   `message`: Human-readable description, useful for UI feedback.
    *   If `status` is "ERROR", the server will subsequently send an `ERROR` packet and close the socket.

### 8.3 SET_USERNAME
Sent by the Client to request a change of identity after the connection is established.

**JSON Structure:**
```json
{
  "type": "SET_USERNAME",
  "timestamp": 1702483200500,
  "username": "NewName"
}
```

If successful, the server broadcasts a `SERVER_BROADCAST` announcing the rename. If failed (e.g., name taken), the server sends an `ERROR` to the requester.

### 8.4 CHAT_MESSAGE
Used for both sending (Client -> Server) and receiving (Server -> Client) text content.

**JSON Structure:**
```json
{
  "type": "CHAT_MESSAGE",
  "timestamp": 1702483201000,
  "from": "Alice",
  "to": "Bob",
  "direct": true,
  "content": "Hello Bob!"
}
```

*   `from`: The username of the sender.
*   `to`: (Optional) The target username for Direct Messages (DMs). Set to `null` for public broadcasts.
*   `direct`: Boolean flag. `true` implies a private message; `false` implies a public broadcast.
*   `content`: The text body.
### 8.5 SERVER_BROADCAST
Sent by the Server to all clients to announce system events (e.g., user joins, quits, renames).

**JSON Structure:**
```json
{
  "type": "SERVER_BROADCAST",
  "timestamp": 1702483202000,
  "content": "Alice has joined the chat."
}
```

Clients should display these messages distinctly from user chat traffic.

### 8.6 ERROR
Sent by the Server to indicate a failure or protocol violation.

**JSON Structure:**
```json
{
  "type": "ERROR",
  "timestamp": 1702483203000,
  "code": "USERNAME_TAKEN",
  "message": "The username 'Admin' is reserved."
}
```

*   `code`: A machine-readable string enumerating the error type (e.g., `BAD_JSON`, `SERVER_BUSY`).
*   `message`: Descriptive text for logging or user display.

### 8.7 DISCONNECT
Sent by either party to initiate a graceful shutdown.

**JSON Structure:**
```json
{
  "type": "DISCONNECT",
  "timestamp": 1702483204000,
  "reason": "User quit application"
}
```

Upon sending or receiving this message, the socket should be cleanly closed.

### 8.8 LIST_USERS
Sent by the Client to request the current list of connected usernames.

**JSON Structure:**
```json
{
  "type": "LIST_USERS",
  "timestamp": 1702483204500
}
```

*   No additional fields are required. The server responds with `USER_LIST`.

### 8.9 USER_LIST
Sent by the Server in response to `LIST_USERS`, containing a snapshot of connected usernames at the time of processing.

**JSON Structure:**
```json
{
  "type": "USER_LIST",
  "timestamp": 1702483204510,
  "users": ["Alice", "Bob", "Charlie"]
}
```

*   `users`: Array of unique usernames currently registered on the server. May be empty if no one else is connected.

Some content needs to be put here to satisfy the tool but I actually want to revert the SECTION 8 changes. Wait, I can just remove the specific block.
However, I made multiple changes to protocol.md including reordering.
The current state has 8.8 and 8.9 at the end of Section 8 or 9?
Let's look at the file specifically around line 200.
I will read the file first to be safe, because I messed up the order before.

## 9. Protocol State Machine

The interaction rules are defined by a finite state machine (FSM).

### Client States
1.  **UNCONNECTED**: Initial state.
2.  **CONNECTING**: Socket open; `CONNECT` sent; awaiting `CONNECT_ACK`.
3.  **CONNECTED (Established)**: `CONNECT_ACK` (OK) received. Heart of the session. Client may send `CHAT_MESSAGE`, `SET_USERNAME`, or `DISCONNECT`.
4.  **DISCONNECTED**: Socket closed or `DISCONNECT` exchanged.

### Server States (Per Client)
1.  **LISTENING**: Awaiting connection.
2.  **HANDSHAKING**: Connection accepted. Reading first frame. Expects `CONNECT`.
3.  **REGISTERED**: `CONNECT` validated. User added to registry. Server sends `CONNECT_ACK`.
4.  **ACTIVE**: Normal operation. Server routes messages from/to this client.
5.  **TERMINATED**: Socket closed. User removed from registry.

## 10. Error Handling

System resilience is paramount. SCP defines specific behaviors for error scenarios:
*   **Malformed JSON**: If the parser fails (e.g., invalid syntax), the server sends `ERROR: BAD_JSON` and immediately disconnects.
*   **Protocol Violation**: Sending a `CHAT_MESSAGE` before `CONNECT` results in immediate disconnection.
*   **Business Logic Errors**: Non-fatal errors (e.g., sending a DM to a non-existent user) result in an `ERROR` message sent back to the sender, but the connection remains open (Status: `OK`).
*   **Resource Exhaustion**: If the server hits `maxClients`, it accepts the socket, reads the `CONNECT`, and replies with `ERROR: SERVER_BUSY` before closing.
*   **Client UX**: The reference CLI surfaces user-friendly messages for connection failures, send failures, unknown commands, and command exceptions; it prompts `/help` when input is not recognized.

## 11. Multi-threading Model

The SCP server implementation utilizes a multi-threaded architecture to handle concurrency:

*   **Acceptor Thread**: A dedicated thread runs the `ServerSocket.accept()` loop. It hands off new sockets to the client handler pool.
*   **Client Handler Threads**: Each connected client is assigned a dedicated runnable (often within a CachedThreadPool). This thread blocks on `read()` operations, ensuring that a slow client does not block the entire server.
*   **Synchronization**: Shared resources, specifically the "Client Registry" (Map<String, ClientHandler>), must be synchronized. SCP recommends using `ConcurrentHashMap` or explicit locks when modifying the list of active users to prevent race conditions during broadcasts.

## 12. Security Considerations

While SCP v1 focuses on mechanics rather than security hardening, the following considerations are acknowledged:

*   **Input Sanitization**: Users can inject arbitrary text. Clients must assume `content` fields are untrusted and sanitize them before rendering (e.g., to prevent XSS in web clients or terminal escape injection in CLIs).
*   **Denial of Service (DoS)**: The length-prefix framing mitigates simple buffer overflows, but a malicious client sending a massive length value could cause OutOfMemoryErrors. Implementations should enforce a standard MAX_MESSAGE_SIZE (e.g., 16KB) and disconnect violators.
*   **Authentication**: SCP v1 is anonymous. There is no password challenge. Identity is strictly "first-come-first-serve" based on username availability.

## 13. Limitations

*   **Identity Spoofing**: Without cryptographic signatures, one cannot prove they are the "real" owner of a username across different sessions.
*   **Plaintext Transport**: All traffic is visible to network sniffers.
*   **Scaling**: The thread-per-client model scales well up to hundreds of users but may become resource-heavy for thousands of concurrent connections.

## 15. Conclusion

The SimpleChat Protocol v1 provides a rigorous yet accessible standard for real-time text communication. By decoupling the transport mechanics from the application logic through clear JSON schemas and strictly enforcing state transitions, SCP serves as an ideal model for understanding distributed system design. It balances the need for academic clarity with the requirements of a functional, robust application.
