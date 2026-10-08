# Action processing in snark

`AbstractCommand` is the transfer object for work executed by `snark`. It carries status, hashes, tags, queue metadata, and optional follow-up actions.

## Processing flow

1. `ConsumerThread` pulls a command from `CommandDeque`.
2. The command is passed to `CommandProcessor`.
3. `CommandProcessor` marks the command as `IN_PROGRESS`.
4. The matching processor executes the command-specific workflow.
5. The processor returns an `ExecutionIssue`, and the consumer thread updates status, retries, or schedules follow-up work.

This keeps command creation, dispatch, and execution separated.

## Command to processor mapping

| Command | Processor | Notes |
|---|---|---|
| `BuildCommand` | `BuildProcessor` | Building and upgrading work |
| `FleetSendCommand` | `FleetSendProcessor` | Fleet dispatch flow |
| `GalaxyAnalyzeCommand` | `GalaxyAnalyzeProcessor` | Galaxy scan flow |
| `LoadColoniesCommand` | `LoadColoniesProcessor` | Colony refresh flow |
| `OpenPageCommand` | `OpenPageProcessor` | UI navigation flow |
| `ReadMessageCommand` | `ReadMessageProcessor` | Inbox reading flow |
| `RecallCommand` | `RecallProcessor` | Fleet recall flow |
| `SendMessageToPlayerCommand` | `SendMessageToPlayerProcessor` | Message sending flow |
| `UpdateFleetEventsCommand` | `UpdateFleetEventsProcessor` | Fleet event refresh flow |
| `UpdateResearchCommand` | `UpdateResearchProcessor` | Research refresh flow |

`UpdateHighScoreCommand` is also an `AbstractCommand`, but it is not routed through `CommandProcessor` in the current consumer flow.

## Important types

- [`AbstractCommand`](../../snark/src/main/java/org/enoch/snark/action/command/AbstractCommand.java) defines the shared command state.
- [`CommandProcessor`](../../snark/src/main/java/org/enoch/snark/action/processor/CommandProcessor.java) dispatches the command to a concrete processor.
- [`ConsumerThread`](../../snark/src/main/java/org/enoch/snark/instance/si/module/consumer/ConsumerThread.java) consumes queued commands and applies retry handling.
