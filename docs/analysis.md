# SCP v1 Analysis

## Drawbacks and Limitations
- **No built-in encryption or authentication**: SCP v1 relies on plain TCP. Confidentiality and integrity require external measures (TLS tunnel, VPN, SSH port forwarding).
- **Single server instance**: There is no clustering or federation. All routing occurs in a single JVM process, so horizontal scaling would require a front-end load balancer plus shared state.
- **Username namespace is global and volatile**: Usernames are unique per running server instance only; there is no persistence across restarts.
- **No delivery guarantees beyond TCP**: Messages are not persisted. If a client disconnects mid-broadcast, messages in flight are lost.
- **Length-prefixed framing only**: The protocol cannot be trivially proxied over line-oriented transports without adaptation.
- **Simple error taxonomy**: Errors are stringly-typed codes without numeric ranges; future versions may formalize codes.

## Testing Strategy
- **Unit tests**: Framing encode/decode and JSON round-trips (serialize → parse → validate) ensure deterministic transformations.
- **Integration sanity**: Manual multi-client sessions verify broadcast/DM paths and username enforcement.
- **Fault injection**: Send malformed JSON and unknown types to confirm `ERROR` responses and server stability.
- **Resource boundaries**: Connect beyond `maxClients` to assert rejection behavior.
- **Network capture**: Use Wireshark or tcpdump on loopback (`lo0`) to confirm 4-byte length prefix, UTF-8 payloads, and correct type routing.
- **Shutdown**: SIGINT handling is exercised to ensure clean socket closure and client notification.
