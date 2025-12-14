# 15-Minute Presentation Outline (Slide-by-Slide)

1. **Title & Goals (1 min)**
   - Introduce SCP v1: custom JSON chat over TCP.
   - Learning objectives: framing, validation, multi-threading.

2. **Architecture Overview (1 min)**
   - Client/Server roles, threading model, dependency on merrimackutil.

3. **Protocol Basics (2 min)**
   - Message types, framing (4-byte length), required fields.

4. **Handshake Deep Dive (1 min)**
   - CONNECT / CONNECT_ACK flow, version checking, capacity enforcement.

5. **Messaging Paths (2 min)**
   - Broadcast vs DM, routing logic, error handling examples.

6. **Data Validation (1 min)**
   - Username/content constraints, unknown type handling, safe defaults.

7. **Logging & Config (1 min)**
   - Configurable log path, logback setup, config schema.

8. **Server Implementation (2 min)**
   - Thread-per-client via ExecutorService, registry for usernames, shutdown hook.

9. **Client UX (1 min)**
   - CLI commands (/all, /dm, /list, /quit), hosts alias resolution, receiver thread.

10. **Testing & Observability (1 min)**
    - Unit tests, Wireshark loopback capture, manual command runs with jars.

11. **Demo Script (2 min)**
    - Start server; start two clients; broadcast; DM; rename; quit; show log file.

12. **Roadmap & Q&A (1 min)**
    - Proposed extensions, constraints, questions.
