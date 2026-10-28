# Conductor-worker WebSocket protocol

The conductor is the WebSocket server at `/ws`; each snark instance is a client. The `common` module's `CommandMessage` is the envelope used in both directions.

External command interfaces belong to the conductor. Discord commands and optional Gemini-assisted translation are a future concept and are not yet usable; see [Discord commands and Gemini](Discord%20commands%20and%20Gemini.md).

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

Do not move message-specific fields to the envelope, infer a message type from its payload, or reply to a response. Requests receive exactly one correlated response; unsolicited updates must have their own documented message type. Worker-originated messages are limited to 16 KiB UTF-8. Snark refuses to send larger messages; the conductor's WebSocket text buffer is configured to 64 KiB and its handler rejects messages over the 16 KiB application limit.

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

## Status summary and updates

The conductor may send `request:status` with a unique ID, `from: "conductor"`, `to` set to the registered instance ID, and `status: 100`. Snark responds with a compact `response:status`, copying the request ID, setting `from` to its own instance ID, and `to` to the request sender. The response payload is an object with:

| Field | Meaning |
|---|---|
| `instanceId` | The responding instance ID. |
| `state` | Worker state; currently `running` when the response is produced. |
| `revision` | Monotonically increasing status revision for this worker process. |
| `serverStatus` | Bounded summary containing Navigator status, active threads, command deque, and notifications. It contains no planet or moon data. |
| `hostname` | Worker host name. |
| `version` | Worker version. |

After a successful registration acknowledgement, and every 10 seconds while connected, snark sends an unsolicited `update:status` message. It uses a unique message ID, `from` set to its registered instance ID, `to: "conductor"`, and `status: 200`. Its payload has the same summary fields as `response:status`, including the revision. The conductor accepts the update only from the currently registered session for the matching identity, and applies strictly newer revisions. It updates the status freshness timestamp on each accepted update. Snark skips a periodic update when its WebSocket outbound queue already contains at least 16 KiB; the next scheduled update carries the current summary instead.

The summary contains `navigatorStatus`, `activeThreads`, `commandDeque`, and `notifications`; its `coordinates` field is `null`. `navigatorStatus` contains `fleetStatus` and `lastUpdate` (an ISO-8601 local date-time). Snark does not query colony data to build a standard status summary. To bound summaries, snark includes at most 32 active thread names (each at most 40 characters), at most 512 characters of notification text, a 256-character command queue, and a 128-character fleet status.

## Restart-required event

When snark encounters a fatal runtime condition that requires a fresh process, it sends an unsolicited `event:restart-required` message on its registered WebSocket session. The message has a unique `id`, `from` set to the instance ID, `to: "conductor"`, `status: 200`, and a payload containing a non-empty `reason`. The conductor accepts it only from the active session registered for that instance, marks the instance `restarting`, then stops the tracked worker process, forcibly terminating it if needed, and relaunches the same profile after a one-second delay. This event is not a request and receives no response. A worker waiting through the configured lobby pause must not send this event; its process and registered WebSocket remain healthy.

## Paginated colony details

The coordinates tab displays all planets and moons by requesting colony data on demand, only after the user clicks its load/refresh action. The conductor makes one or more bounded `request:status:details` requests to retrieve the full inventory. Each request uses the standard request envelope and a payload of `{"offset": 0, "limit": 25}`. `offset` must be non-negative and `limit` must be between 1 and 25. Snark returns the correlated `response:status:details` with payload fields `instanceId` and `details`. `details` contains `offset`, `limit`, `total`, `planetCount`, `moonCount`, and `colonies`. A page contains at most the requested number of colony objects, each with coordinate, resources, energy, and ship counts. Invalid ranges receive `error:status:details`; a page-generation failure receives the same error type with status `500`.

The conductor exposes this request through `GET /api/instances/{id}/status/colonies?offset=0&limit=25`. The HTTP request waits up to five seconds for its correlated response. Invalid ranges return `400`; offline workers return `409`; a response timeout returns `504`.

During rolling upgrades, the conductor continues to accept the previous `payload.message` string only in a correlated `response:status` and exposes it as the Navigator fleet status. The conductor accepts status responses only when the response ID matches an outstanding request for the same registered session, the response comes from that session, and `to` is `conductor`. Missing or malformed status data is not a successful status update. Unsolicited updates do not answer requests and do not weaken request/response correlation.

## Compatibility and tests

Message type names, identity locations, response correlation, revision ordering, frame and page limits, and payload schemas are protocol requirements. Changes must update both producer and consumer in the same change and include tests that assert serialized message shape and handling on the receiving side. Do not preserve competing aliases such as `REGISTER` alongside `request:register`; an alternate format requires an explicit versioned migration.
