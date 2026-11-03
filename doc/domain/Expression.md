# Expression Functions

This page is the reference for the SpEL functions registered by [`FunctionRegistry`](../../snark/src/main/java/org/enoch/snark/expression/FunctionRegistry.java) and the coordinate selectors handled by [`ExpressionService`](../../snark/src/main/java/org/enoch/snark/expression/ExpressionService.java). SpEL functions are called with `#`, for example `#all_planets()`.

## How to read the parameter lists

- **Required** means the parameter must be present. **Optional** means it may be omitted.
- A function that takes a parameter map uses SpEL map syntax: `#function({key: value})`.
- Aliases in one parameter row are interchangeable names for the same value.
- Coordinate strings use `p[galaxy:system:position]` for a planet and `m[galaxy:system:position]` for a moon. A coordinate list is separated with semicolons.
- `List<PlanetData>` means a list of database-backed coordinate data. Functions registered through a wrapper may declare `List<?>`, but return coordinate data in normal use.
- Functions in **Conditions** create an `AbstractCondition`; they do not themselves return a boolean. Technology comparison functions are direct boolean queries.

## Quick index

| What you need | Functions |
|---|---|
| Get planets, moons, or colonies | `#all_planets()`, `#all_moons()`, `#all()`, `#none()`, `#all_positions()` |
| Convert or move coordinates | `#planets(...)`, `#moons(...)`, `#swap(...)`, `#farm(...)`, `#next(...)`, `#prev(...)`, `#space(...)` |
| Get coordinates from a cache entry | `#cacheKey(...)` |
| Filter or inspect event fleets | `#eventFleetFilter(...)`, `#endingPlanet(...)` |
| Work with a list | `#first(...)` |
| Build an action condition | Functions in [Conditions](#conditions) |
| Compare a technology level | `#hasTechnologyGreaterThan(...)`, `#hasTechnologyLessThan(...)` |

## Coordinate selection

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#all_planets()` | Returns all owned planets. | None | `List<PlanetData>` |
| `#all_moons()` | Returns all owned moons. | None | `List<PlanetData>` |
| `#all()` | Returns all owned planets and moons. | None | `List<PlanetData>` |
| `#allPositionsFunc()` / `#all_positions()` | Returns the colony selected for each planet position. | None | `List<PlanetData>` |
| `#none()` | Returns an empty coordinate list. | None | `List<PlanetData>` |
| `#planets(value)` | Resolves coordinates to their planet counterparts. | `value` — required; `String` coordinate/list or `List<PlanetData>` | `List<PlanetData>` |
| `#moons(value)` | Resolves coordinates to their moon counterparts. | `value` — required; `String` coordinate/list or `List<PlanetData>` | `List<PlanetData>` |

The following are selectors for `ExpressionService.evaluateCoordinates(...)`, rather than standalone SpEL functions:

| Selector | Result |
|---|---|
| `planets` | Same as `#all_planets()` |
| `moons` or `all` | Same as `#all_moons()` |
| `none` | Empty list |
| Empty string | Same as `#allPositionsFunc()` |
| `space` | Evaluates `#spaceFunc(#source, 0)` |
| `swap` | Evaluates `#swapFunc(#source)` |
| Coordinate literal, e.g. `p[1:2:3];m[1:2:3]` | Resolves the listed coordinates |

An expression passed to `evaluateCoordinates` may also return a `String`, one `PlanetData`, or a `List<PlanetData>`.

## Coordinate transformations

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#swap(value)` | Changes each coordinate from planet to moon or moon to planet. | `value` — required; `String` coordinate/list or `List<PlanetData>` | `List<PlanetData>` |
| `#swapStr(coordinates)` | String form of `#swap`. | `coordinates` — required; `String` coordinate/list | `List<PlanetData>` |
| `#swapFunc(coordinates)` | List form of `#swap`. | `coordinates` — required; `List<PlanetData>` | `List<PlanetData>` |
| `#farm(value)` | Resolves the farm coordinates for a source coordinate or coordinate list. | `value` — required; `String` coordinate/list or `List<PlanetData>` | `List<PlanetData>` |
| `#next(current, trip)` | Returns the coordinate after `current` in the configured trip, wrapping at the end. | `current` — required; `String` coordinate or `List<PlanetData>`; `trip` — required; `String` coordinate list | `List<PlanetData>` |
| `#prev(current, trip)` | Returns the coordinate before `current` in the configured trip, wrapping at the beginning. | `current` — required; `String` coordinate or `List<PlanetData>`; `trip` — required; `String` coordinate list | `List<PlanetData>` |
| `#space(coordinate, moveSystem)` | Moves the coordinate by a system offset and converts it to a space position. | `coordinate` — required; `String` coordinate/list or `List<PlanetData>`; `moveSystem` — required; integer `String` | `List<PlanetData>` |
| `#spaceStr(coordinates, moveSystem)` | String form of `#space`. | `coordinates` — required; `String` coordinate/list; `moveSystem` — required; integer `String` | `List<PlanetData>` |
| `#spaceFunc(coordinates, moveSystem)` | List form of `#space`. | `coordinates` — required; `List<PlanetData>`; `moveSystem` — required; integer `String` | `List<PlanetData>` |
| `#cacheKey(key)` / `#cache_key(key)` | Resolves the coordinates stored in a cache entry. | `key` — required; `String` cache-entry key | `List<PlanetData>` |

## Event fleets

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#eventFleetFilter(filters)` / `#event_fleet_filter(filters)` | Filters the current event-fleet list by the supplied criteria. | `filters` — required; `Map<String, Object>`; all filter entries below are optional | `List<EventFleet>` |
| `#endingPlanet(eventFleet)` | Gets the planet where the fleet is considered to end. | `eventFleet` — required; `EventFleet` | `Planet` |

Supported `eventFleetFilter` map keys:

| Key | Expected type | Description |
|---|---|---|
| `arrivalTimeLessThan` | Duration `String`, e.g. `'30M'` | Keeps fleets arriving before the current time plus this duration. |
| `arrivalTimeGreaterThan` | Duration `String`, e.g. `'1H'` | Keeps fleets arriving after the current time plus this duration. |
| `isHostile` | `Boolean` | Keeps fleets whose hostile flag matches the value. |
| `countGreaterThan` | Integer `Number` or integer `String` | Keeps fleets whose displayed fleet count is strictly greater. |
| `countLessThan` | Integer `Number` or integer `String` | Keeps fleets whose displayed fleet count is strictly less. |
| `mission` | Semicolon-separated `String` of mission names | Keeps fleets whose mission matches one of the names. Names are case-insensitive enum names; for example `'ATTACK;TRANSPORT'`. |

With an empty filter map, `eventFleetFilter` returns the current event-fleet list without applying criteria.

## Lists

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#first(values)` | Returns the first element in a non-empty list. | `values` — required; `List<T>` | `T`, the list element type |

## Conditions

Each function below returns an `AbstractCondition` that can be used in action condition expressions. In each row, parameter aliases are interchangeable. Resource, ship, planet, and duration values are passed as strings unless another type is specified.

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#hasResourcesCount(params)` | Requires the selected planet to have at least the requested total resource count. | `planet` or `source` — required; `String` coordinate/reference; `count` or `resourcesCount` — required; `String` amount, e.g. `'3m'` | `AbstractCondition` (`HasResourcesCountCondition`) |
| `#hasResources(params)` | Requires the selected planet to have the requested resources, optionally preserving a reserve. | `planet` or `source` — required; `String`; `have` or `resources` — required; `String` resource amounts; `nothing`, `leave`, or `leaveResources` — optional; `String` reserve amounts, default empty/no reserve | `AbstractCondition` (`HasResourcesCondition`) |
| `#areShips(params)` | Requires the selected planet to have the requested ships, optionally preserving ships. | `planet` or `source` — required; `String`; `need` or `shipsMap` — required; `String` ship counts; `leave` or `none` — optional; `String` ship counts, default no ships to preserve | `AbstractCondition` (`AreShipsCondition`) |
| `#isFleetExpired(params)` | Creates a condition that waits for matching fleets to expire. | `source` or `planet` — required; `String`; `missions` or `blockingMissions` — required; `String` mission names separated by `\|` or `;`; `timeout` or `duration` — optional; duration `String`, default `'1H'` | `AbstractCondition` (`IsFleetExpiredCondition`) |
| `#isExpressionTrue(params)` | Creates a condition comparing a configured term with a value. | `term` — required; `String`; `op` or `operator` — required; `String`; `value` — required; `String` | `AbstractCondition` (`IsExpressionTrueCondition`) |
| `#hasTechnology(params)` | Reserved placeholder; it currently throws `UnsupportedOperationException` and cannot be used to create a condition. | No supported parameter contract | Declared `AbstractCondition`; no value is returned at runtime |
| `#hasNoMissions(params)` | Creates a condition requiring that no selected blocking missions remain on the source planet. | `source` or `planet` — required; `String`; `missions` or `blockingMissions` — optional; `String` mission names separated by `\|` or `;`, defaults to all known missions; `timeout` or `duration` — optional; duration `String`, default `'1H'` | `AbstractCondition` (`HasNoMissionsCondition`) |
| `#hasFleetSlots(params)` | Requires the player to have at least the requested number of free fleet slots. | `freeSlots` or `required` — required; integer `String` | `AbstractCondition` (`HasFleetSlotsCondition`) |
| `#isAttackNotExpired(params)` | Creates a condition that remains valid while the specified target's attack has not expired. | `target` — required; `String` coordinate/reference; `timeout` or `duration` — optional; duration `String`, default `'1H'` | `AbstractCondition` (`IsAttackNotExpiredCondition`) |
| `#isSpyNotExpired(params)` | Creates a condition that remains valid while the specified target's spy operation has not expired. | `target` — required; `String` coordinate/reference; `timeout` or `duration` — optional; duration `String`, default `'1H'` | `AbstractCondition` (`IsSpyNotExpiredCondition`) |
| `#isModifiedAfter(params)` | Creates a condition comparing modification times for two terms. | `first` or `firstTerm` — required; `String`; `second` or `secondTerm` — required; `String` | `AbstractCondition` (`IsModifiedAfterCondition`) |

Duration strings use a number followed by `H`, `M`, or `S`, for example `'1H'`, `'30M'`, or `'45S'`. Mission names use the `Mission` enum names.

## Technology level comparisons

Both functions compare a technology's current level strictly against `level`; equality returns `false` for either function.

| Function | Description | Parameters | Returns |
|---|---|---|---|
| `#hasTechnologyGreaterThan(params)` | Checks whether a technology level is greater than the requested level. | `name` — required; `String` exact `Research`, `Building`, or `LFBuilding` enum name; `level` — required; numeric `Number` or `String`; `coordinate` — required for `Building` and `LFBuilding`, `String` coordinate; unused and optional for `Research` | `Boolean` |
| `#hasTechnologyLessThan(params)` | Checks whether a technology level is less than the requested level. | `name` — required; `String` exact `Research`, `Building`, or `LFBuilding` enum name; `level` — required; numeric `Number` or `String`; `coordinate` — required for `Building` and `LFBuilding`, `String` coordinate; unused and optional for `Research` | `Boolean` |

Research is read from the main player. Buildings and lifeform buildings are read from the colony at the supplied coordinate. A null stored level is treated as zero. Unknown technology names and missing required values fail explicitly.

## Variables and evaluation

`ExpressionService.evaluate(expression, context, returnType)` and `evaluateNoCached(...)` make the entries in `context` available as SpEL variables. Refer to a variable as `#variableName`; values can be strings, objects, or collections. A string context value containing `#` is itself evaluated as a SpEL expression before being added to the context. Cached database entries are also added as variables.

`evaluate` caches results for the configured TTL; `evaluateNoCached` does not. Both parse general SpEL expressions, so standard SpEL property access, operators, and function composition can be combined with the registered functions on this page.
