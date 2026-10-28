# Instance lifecycle and recovery

The conductor owns the process lifecycle of each configured snark instance. Snark loads its behavior from the instance properties file, starts the configured threads, and keeps its worker process and WebSocket connection active while those threads run.

## Startup and normal work

The conductor starts profiles marked for auto-start when it launches and tracks instances started explicitly through its API or CLI. The GUI reports `starting` from process launch until the worker connects and sends its first status; only then does it report `running`. A previously registered worker whose WebSocket disconnects is shown as `disconnected`, and an absent or stopped process is shown as `stopped`. Snark loads the configured properties and starts the corresponding threads. `ConsumerThread` owns the command deque and executes its commands through the shared ChromeDriver session.

The consumer opens the configured game server and records the browser window handle for that server. A healthy server session requires both a live ChromeDriver and that specific window to remain available. Closing the game window is a fatal worker condition; snark closes its application context and stops rather than silently creating a replacement browser or continuing with a different tab.

## Lobby pause and session restart

When the active game window returns to the lobby, `ConsumerThread` pauses for the configured `restart_duration`, then reopens the lobby and starts a new server session. This pause is local to the consumer thread: the worker process and its WebSocket client remain active, and the worker continues sending status updates. The conductor must not treat a live process with a registered WebSocket as stopped just because the consumer is waiting in the lobby.

## Failure recovery

If ChromeDriver or the recorded server window becomes unavailable, snark closes its application context and exits with a failure code. Core stops its configured threads and their executors during shutdown. The conductor supervises each managed instance:

- If the worker process exits, the conductor starts a fresh process using the same profile and properties.
- If snark detects a fatal condition such as losing its ChromeDriver browser window, it sends `event:restart-required` over its currently registered WebSocket session, including a reason. The conductor accepts the event only from that instance's active session, marks it `restarting`, stops the tracked process (forcibly if graceful termination times out), waits one second, and launches the same profile again. This lets recovery proceed even if worker shutdown is blocked.
- If the worker has registered before but remains disconnected beyond `conductor.worker-disconnect-timeout` (default `30000` milliseconds), the conductor stops the process, forcibly terminates it if needed, and starts it again.
- The supervisor checks at `conductor.instances-monitor-interval` (default `5000` milliseconds). These settings can be overridden in conductor configuration.
- An explicit stop removes the instance from supervision; it is not automatically restarted.

This keeps browser-session recovery at the process boundary: the consumer does not recover from a lost server window, and the conductor restarts the full worker from its configured startup state.

## Related implementation

- [`ProcessService`](../../conductor/src/main/java/org/enoch/conductor/process/ProcessService.java) supervises managed worker processes.
- [`ConsumerThread`](../../snark/src/main/java/org/enoch/snark/instance/si/module/consumer/ConsumerThread.java) owns command execution and browser-loss handling.
- [`GISession`](../../snark/src/main/java/org/enoch/snark/instance/si/module/consumer/gi/GISession.java) manages the lobby pause and server-session startup.
- [`Wd`](../../snark/src/main/java/org/enoch/snark/instance/si/module/consumer/gi/Wd.java) verifies ChromeDriver and browser-window availability.
- [WebSocket protocol](WebSocket%20protocol.md) describes worker registration and status updates.
