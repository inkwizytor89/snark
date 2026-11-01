# Discord commands and Gemini

Discord is a conductor-owned interface. When a bot token and at least one valid instance channel are configured, the conductor starts the JDA bot and sends instance status changes to each instance's configured channel. See [Discord instance status notifications](../bugfix/Discord%20instance%20status%20notifications.md) for lifecycle and delivery behavior.

The bot also routes a limited set of explicit text commands through the conductor. Optional Gemini-assisted natural-language translation is not implemented. Worker operations remain conductor-owned; snark must not connect to Discord or execute Discord commands locally.

Commands are parsed as a hierarchy of command and action nodes. Each node can define child actions and its own short description; append `help` to the last command/action to display that description. This structure allows future paths such as `<command> <action1> <action2> help` without changing the parsing model. Global `help` lists top-level commands.

Current command combinations:

| Command | Behavior |
| --- | --- |
| `ping` | Replies with `pong`. |
| `ping help` | Explains `ping`. |
| `context` | Displays the selected instance. |
| `context help` | Explains `context`. |
| `set <id>` | Selects the context instance. |
| `set help` | Explains `set`. |
| `list` | Lists all configured instances. |
| `list help` | Explains `list`. |
| `start` / `stop` / `restart` | Starts, stops, or restarts the context instance through the conductor process service. |
| `start help` / `stop help` / `restart help` | Explains the selected lifecycle command. |
| `status` | Shows the context instance state. For a running instance, it reports the GUI fleet/expedition summary and its `HH:mm:ss` update time, without the Discord channel. |
| `status help` | Explains `status`. |
| `status thread` | Lists the active threads exactly as the GUI's “Active threads” field. |
| `status thread help` | Explains the `status thread` action. |
| `status deque` | Shows the command deque exactly as the GUI's “Command deque” field. |
| `status deque help` | Explains the `status deque` action. |

`list` includes every configured instance regardless of its state or Discord channel. If no context is selected, the conductor resolves its configured default instance when available.

Gemini-assisted input is not enabled. Any future Gemini output must be validated against the conductor's allowed command schema and must never be treated as an executable expression.

## Related implementation

- [`TerminalCli`](../../conductor/src/main/java/org/enoch/conductor/cli/TerminalCli.java) is the existing conductor command entry point.
- [`WorkerSocketHandler`](../../conductor/src/main/java/org/enoch/conductor/ws/WorkerSocketHandler.java) sends requests to registered snark instances.
- [WebSocket protocol](WebSocket%20protocol.md) documents the worker request and response envelope.
