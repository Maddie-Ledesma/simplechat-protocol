# Project Management Artifacts

## GitHub Project Board Columns
- Backlog
- In Progress
- In Review
- Testing
- Done
- Blocked

## Tasks (≥12) with Owners and Relative Dates
1. Define protocol message schema — Member A (Day 1)
2. Implement framing utilities — Member B (Day 1)
3. Build JsonSerializable message classes — Member A (Day 2)
4. Write MessageParser/Validator — Member A (Day 2)
5. Implement ClientRegistry — Member B (Day 3)
6. Implement ClientHandler and server accept loop — Member B (Day 3)
7. Add logging configuration and config loader — Member C (Day 3)
8. Build CLI client sender/receiver — Member C (Day 4)
9. Implement command parser (/all, /dm, /list, /quit) — Member C (Day 4)
10. Write unit tests (framing, message round-trip) — Member A (Day 5)
11. Documentation (protocol spec, diagrams) — Member B (Day 5)
12. Package runnable JARs and README — Member C (Day 5)
13. Manual multi-client test and fixes — Member A (Day 6)
14. Final review and polish — Member B (Day 7)

## GitHub Issues (≥8) with Acceptance Criteria
1. **Handshake fails gracefully**
   - Given malformed CONNECT, server returns ERROR and closes socket. Test passes.
2. **Username uniqueness enforced**
   - Second client using same username receives ERROR; original remains connected.
3. **Broadcast routing**
   - /all sends message to all connected clients except sender; observed in manual test.
4. **Direct message routing**
   - /dm reaches target only; no other clients receive it.
5. **Framing compliance**
   - Length prefix verified by FramingTest; Wireshark shows correct bytes.
6. **SIGINT shutdown**
   - Server stops accept loop, notifies clients, exits without stack traces.
7. **Logging to configured file**
   - Log file path from config is used; log entries appear during chat session.
8. **Hosts alias resolution**
   - Using --name with hosts.json connects to correct host/port.

## Commit Message Examples
- `feat(protocol): add chat message JSON validation`
- `feat(server): enforce maxClients and reject extras`
- `chore(logging): configure logback file appender from config`
- `feat(client): implement /dm and /all commands`
- `test(protocol): add message round-trip coverage`
- `docs: add SCP v1 diagrams and presentation outline`
