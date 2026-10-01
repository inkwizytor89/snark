# Decision: action execution flow

The conductor owns the list of actions that can be executed for each managed instance. These actions are modeled as explicit operations in the conductor layer and are exposed to external interfaces only through the conductor API, GUI, or CLI.

The GUI and terminal are not allowed to execute operations directly on worker instances. Instead, they must request the action through the conductor, which validates the request, decides whether it is permitted, and then triggers the corresponding workflow.

This keeps execution centralized and prevents bypassing lifecycle controls, status checks, and instance isolation rules. In practice, the conductor is the single entry point for start, stop, restart, status polling, and other instance-related actions.
