# Coordinate domain

Coordinate describes a location in the game world. The first letter identifies the type: `p` for planet, `m` for moon, and `d` for debris.

After the type prefix, the coordinate contains three values in order: galaxy, system, and position.

Examples:

- `p[2:4:7]` — planet
- `m[2:4:7]` — moon
- `d[2:4:7]` — debris field

## Domain model

Players own planets and moons. Debris is a separate coordinate target and is not owned like a colony.

Main player data is stored in the `players` table, while regular colonies and moons are stored in `colonies`. Targets represent other players' locations and the shared target area for scanning and attacks.

The main coordinate value object is [`Planet`](../../snark/src/main/java/org/enoch/snark/instance/model/to/Planet.java). It is used by [`PlanetData`](../../snark/src/main/java/org/enoch/snark/instance/model/to/PlanetData.java) to keep an abstract coordinate together with the matching database entity.

`PlanetEntity`, `ColonyEntity`, and `TargetEntity` describe the database-backed coordinate structures in the worker runtime.

## Coordinate expressions

[`CoordinateSpelService`](../../snark/src/main/java/org/enoch/snark/expression/coordinate/CoordinateSpelService.java) translates textual configuration into coordinate objects. It also resolves named inputs such as `source` and supports expression-based selection.

Important selectors include:

- `target_expression`: `swap|next|prev|main_fleet_to|main_fleet_on|main_fleet|probe_swam|farm|p[2:4:7]`
- `colony_expression`: `moon|planet| |m[1:2:3];p[2:4:7]`

For colony selection, empty means "prefer moon if it exists, otherwise planet". `moon` selects only moons, and `planet` selects only planets.
