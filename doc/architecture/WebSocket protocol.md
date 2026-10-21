# Conductor-worker WebSocket protocol

The conductor is the WebSocket server at `/ws`; each snark instance is a client. The `common` module's `CommandMessage` is the envelope used in both directions.

## Envelope

Every message is a JSON object with these fields:

| Field | Meaning |
|---|---|
| `id` | Unique message ID. A response copies the request ID. |
| `type` | Exact, case-sensitive message type such as `request:status`. |
| `from` | Sender identity: `conductor` or the registered instance ID. |
| `to` | Intended recipient identity. |
| `timestamp` | Unix epoch time in milliseconds when the sender created the message. |
| `status` | Numeric outcome: `100` for a request, `200` for success, or `4xx` for an error. |
| `payload` | Message-specific JSON object. |
| `error` | Human-readable error for an `error:*` response; otherwise `null`. |
| `metadata` | Optional message-specific metadata. |

Do not move message-specific fields to the envelope, infer a message type from its payload, or reply to a response. Requests receive exactly one correlated response; unsolicited updates must have their own documented message type.

## Registration

After the WebSocket handshake, snark sends:

```json
{
  "id": "unique-id",
  "type": "request:register",
  "from": "server#account",
  "to": "conductor",
  "timestamp": 0,
  "status": 100,
  "payload": {
    "instanceId": "server#account",
    "version": "0.3.0-SNAPSHOT",
    "hostname": "worker-host"
  },
  "error": null,
  "metadata": null
}
```

`id` is required and `status` must be `100`. `from` and `payload.instanceId` must be non-empty and identical, and `to` must be `conductor`. The conductor associates that instance ID with the current WebSocket session and returns `response:register` with the same `id`, `from: "conductor"`, `to` set to the instance ID, `status: 200`, and `payload.registered: true`. Invalid registration receives a correlated `error:register` response and must not register the session; a registration without an ID is rejected by closing the session because it cannot be correlated. Snark handles status requests only after receiving the correlated successful registration response. The conductor must not consider a worker online merely because the WebSocket handshake succeeded.

## Status request and response

The conductor sends `request:status` with a unique ID, `from: "conductor"`, `to` set to the registered instance ID, and `status: 100`. Snark responds with `response:status`, copying the request ID, setting `from` to its own instance ID, and `to` to the request sender. The response payload is an object with:

| Field | Meaning |
|---|---|
| `instanceId` | The responding instance ID. |
| `state` | Worker state; currently `running` when the response is produced. |
| `serverStatus` | Structured server status containing Navigator status, active threads, command deque, and aggregated thread notifications. |
| `hostname` | Worker host name. |
| `version` | Worker version. |

The `serverStatus` object contains `navigatorStatus`, `activeThreads`, `commandDeque`, `notifications`, and `coordinates`. `navigatorStatus` contains `fleetStatus` and `lastUpdate` (an ISO-8601 local date-time). `coordinates` contains planet and moon counts plus a colony list with coordinate, resource, energy, and ship-count data. During rolling upgrades, the conductor also accepts the previous `payload.message` string and exposes it as the Navigator fleet status until the worker is upgraded. The conductor accepts status only when the response ID matches an outstanding status request, the response comes from the session registered for the matching `from` identity, and `to` is `conductor`. Missing or malformed status data is not a successful status update.

## Compatibility and tests

Message type names, identity locations, response correlation, and payload schemas are protocol requirements. Changes must update both producer and consumer in the same change and include tests that assert serialized message shape and handling on the receiving side. Do not preserve competing aliases such as `REGISTER` alongside `request:register`; an alternate format requires an explicit versioned migration.
