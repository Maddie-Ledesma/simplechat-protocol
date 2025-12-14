# SCP v1 Diagrams (ASCII)

## Handshake (CONNECT + ACK)
```
Client                          Server
  |                                |
  |-- LEN + CONNECT -------------->|
  |                                |
  |<------------- LEN + CONNECT_ACK|
  |                                |
  |-------- Ready for chat --------|
```

## Broadcast (/all)
```
Client A                        Server                        Client B
  |                                |                              |
  |-- LEN + CHAT_MESSAGE (broadcast)-->                          |
  |                                |-- LEN + CHAT_MESSAGE -----> |
  |                                |-- LEN + CHAT_MESSAGE -----> Client C
  |                                |                              |
```

## Direct Message (/dm)
```
Client A                        Server                        Client B
  |                                |                              |
  |-- LEN + CHAT_MESSAGE (to=B, direct=true)-->                   |
  |                                |-- LEN + CHAT_MESSAGE -----> |
  |                                |                              |
```

## List Users (/list)
```
Client                          Server
  |                                |
  |-- LEN + LIST_USERS ----------->|
  |                                |
  |<----------- LEN + USER_LIST ---| (users=["Alice","Bob","You"])
  |                                |
```

## Disconnect
```
Client                          Server
  |                                |
  |-- LEN + DISCONNECT ----------->|
  |                                |
  |   close socket                 |
  |<----------- SERVER_BROADCAST --| (notify others)
```
