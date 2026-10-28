# Discord commands and Gemini

Discord commands are a planned conductor interface, not a usable feature at present. The intended flow is for the conductor to receive a Discord command, optionally use Gemini to translate natural-language input into an approved conductor operation, validate the operation, and send a correlated WebSocket request to the selected snark instance. Responses and status should return through the conductor before being reported to Discord.

The conductor CLI already sends worker requests through the conductor-worker WebSocket connection. Discord and Gemini do not yet use this path: their conductor classes are placeholders, no Discord listener is started, and natural-language commands are not executed. Snark must not connect to Discord or execute conductor commands locally.

Before enabling this feature, define the allowed command schema, authorization and instance-selection rules, confirmation behavior for state-changing operations, response correlation and timeout handling, and safe Gemini output validation. Gemini output must never be treated as an executable expression or command without conductor-side validation.

## Related implementation

- [`TerminalCli`](../../conductor/src/main/java/org/enoch/conductor/cli/TerminalCli.java) is the existing conductor command entry point.
- [`WorkerSocketHandler`](../../conductor/src/main/java/org/enoch/conductor/ws/WorkerSocketHandler.java) sends requests to registered snark instances.
- [WebSocket protocol](WebSocket%20protocol.md) documents the worker request and response envelope.
