# Thread system

Threads are the runtime units that execute the configured behavior. They run in a loop, checking conditions and pushing commands to the queue.

## Thread architecture

All threads extend [`AbstractThread`](../../snark/src/main/java/org/enoch/snark/instance/si/module/AbstractThread.java) and run in a loop that:

1. Checks time-based scheduling and conditions.
2. Calls `onStep()` to generate work-specific commands.
3. Pushes command batches to the core queue with a rate limit.
4. Sleeps for the configured pause duration.

Thread types include:

- `ConsumerThread` — pulls commands from the queue and dispatches to processors
- `BuildThread` — generates building and upgrade commands
- `FleetThread` — generates fleet dispatch commands
- `DefenseThread` — generates defense commands
- `ScanThread` — generates scan and analysis commands
- `SpaceThread` — tracks space operations and movement
- `HuntingThread` — hunting and targeting logic
- `UpdateThread` — updates research, colonies, and fleet data
- `DefineThread` — defines and scopes targets
- `ExpeditionThread` — expedition command generation
- `EventThread` — handles event processing

## Module and thread grouping

Modules group related threads together. Each module:

- Owns a collection of threads.
- Shares a time schedule across all threads.
- Has a `mainMap` configuration that is inherited by all its threads.

Threads inherit configuration from their module's `mainMap` through [`AbstractModule`](../../snark/src/main/java/org/enoch/snark/instance/si/module/AbstractModule.java). If a thread-specific value is not set, it falls back to the module-level value.

## Configuration system

Configuration is provided through property files. The naming scheme is:

```
module_name.thread_name.key=value
```

Example from `swarn.properties`:

```
attack.time=on
attack.start_conditions=[{"type": "RESOURCE_COUNT","planet": "target","resourcesCount": "4kk"}]
attack.source=#cache_key('swarm_nest')
attack.target=#farm(#source)
attack.mission=ATTACK
attack.ships_wave=espionageProbe:-2
attack.speed=50
attack.command_limit=2
attack.pause=4S
```

### Configuration resolution

When a thread reads a config key:

1. First check the thread's own config (e.g., `attack.start_conditions`).
2. If not found, check the module's `mainMap` (shared module config).
3. If still not found, use the provided default value.

Thread-specific configs override module-level ones, allowing global defaults with per-thread overrides.

## Global control configs

These configs control all threads uniformly:

| Config | Meaning | Example |
|---|---|---|
| `time` | Schedule when thread runs (on/off or time ranges) | `on`, `09:00-18:00` |
| `pause` | Sleep duration between loop iterations | `4S`, `10M` |
| `type` | Thread type (for instantiation) | `consumer`, `build`, `fleet` |
| `debug` | Enable debug logging | `true`, `false` |
| `command_limit` | Max commands to queue per loop | `2`, `4` |

## Thread-specific configs

Different thread types use domain-specific keys:

| Config | Used by | Meaning |
|---|---|---|
| `source` | Most work threads | Source coordinate(s) for commands |
| `target` | Send/dispatch threads | Destination coordinate(s) |
| `mission` | Fleet threads | Fleet mission type (ATTACK, TRANSPORT, etc.) |
| `ships` / `ships_wave` | Fleet threads | Ship composition or wave sequence |
| `start_conditions` | Most work threads | Conditions that must be true to run |
| `strategy` | Hunting/scan threads | Behavior strategy |
| `command_limit` | All threads | Max commands queued per iteration |
| `expired_time` | Work threads | TTL for generated commands |

## Coordinate and command expressions

Source and target use coordinate expressions:

- Literals: `p[2:4:7]`, `m[1:2:3]`
- Functions: `#farm(#source)`, `#next(#source)`, `#prev(#source)`
- Cache references: `#cache_key('swarm_nest')`
- Database queries: `Colony|SELECT ...`
- Symbolic: `#source`, `#target`, `#fly_points`

These are resolved by [`CoordinateSpelService`](../../snark/src/main/java/org/enoch/snark/expression/coordinate/CoordinateSpelService.java) at runtime.

## Configuration example

From `swarn.properties`:

```
attack.time=on
attack.start_conditions=[{"type": "RESOURCE_COUNT","planet": "target","resourcesCount": "4kk"}]
attack.source=#cache_key('swarm_nest')
attack.target=#farm(#source)
attack.mission=ATTACK
attack.ships_wave=espionageProbe:-2
attack.speed=50
attack.command_limit=2
attack.pause=4S
```

Broken down:

- `attack.time=on` — Attack thread always runs (within module schedule)
- `attack.source` — Source coordinate from cache
- `attack.target` — Target farm derived from source
- `attack.start_conditions` — Conditions that must pass before generating attack commands
- `attack.command_limit` — Queue at most 2 attack commands per loop
- `attack.pause` — Wait 4 seconds between iterations

## Important types

- [`AbstractThread`](../../snark/src/main/java/org/enoch/snark/instance/si/module/AbstractThread.java) — base class for all threads; handles loop, config loading, and queue management
- [`ThreadMap`](../../snark/src/main/java/org/enoch/snark/instance/si/module/ThreadMap.java) — configuration container and accessor with type-safe getters
- [`AbstractModule`](../../snark/src/main/java/org/enoch/snark/instance/si/module/AbstractModule.java) — groups threads and manages shared time schedule
