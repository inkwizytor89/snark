# ConfigService domain

[`ConfigService`](../../snark/src/main/java/org/enoch/snark/config/ConfigService.java)
manages modules, threads, and their configuration in the worker database.
It is the application entry point for loading configuration and for changing
the module and thread structure.

## Available operations

- `run(...)` loads the configured properties file when the application starts.
- `loadFromFile(path)` loads configuration terms from a properties file and
  updates the matching threads.
- `updateThreads(configTerms)` creates missing modules and threads, then applies
  each configuration term to its thread.
- `changeThreadModule(term, moduleName)` moves an existing thread to the named
  module. The module is created when it does not exist; an exception is thrown
  when the source thread cannot be found.
- `deleteThread(term)` removes a thread and its configuration entries.
- `deleteModule(term)` removes a module, all of its threads, and their
  configuration entries.
- `listModuleDefinitions()` returns module names and current `time`/`conditions`
  settings for GUI and WebSocket consumers.
- `updateModule(term)` updates one module setting (`time` or `conditions`) and
  creates the module when it does not exist.

## Configuration term

Configuration is identified by `module.thread.key=value`. A two-part property
uses the default module, and a one-part property targets the `main` thread.
Common thread settings such as `time`, `pause`, `debug`, `command_limit`, and
`conditions` are stored on the thread entity; other keys are stored as
thread-specific configuration entries.
