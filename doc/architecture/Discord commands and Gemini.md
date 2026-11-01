# Discord commands and Gemini

Discord is a conductor-owned interface. When a bot token and at least one valid instance channel are configured, the conductor starts the JDA bot and sends instance status changes to each instance's configured channel. See [Discord instance status notifications](../bugfix/Discord%20instance%20status%20notifications.md) for lifecycle and delivery behavior.

The bot also routes a limited set of explicit text commands through the conductor. Optional Gemini-assisted natural-language translation is not implemented. Worker operations remain conductor-owned; snark must not connect to Discord or execute Discord commands locally.

Before enabling this feature, define the allowed command schema, authorization and instance-selection rules, confirmation behavior for state-changing operations, response correlation and timeout handling, and safe Gemini output validation. Gemini output must never be treated as an executable expression or command without conductor-side validation.

## Related implementation

- [`TerminalCli`](../../conductor/src/main/java/org/enoch/conductor/cli/TerminalCli.java) is the existing conductor command entry point.
- [`WorkerSocketHandler`](../../conductor/src/main/java/org/enoch/conductor/ws/WorkerSocketHandler.java) sends requests to registered snark instances.
- [WebSocket protocol](WebSocket%20protocol.md) documents the worker request and response envelope.
