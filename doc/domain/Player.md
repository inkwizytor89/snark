# Player domain

The `players` table stores player-scoped data.

## Main player

The main player is the controlled account for the current instance. It is resolved through `PlayerRepository#mainPlayer` in [`PlayerRepository.java`](../../snark/src/main/java/org/enoch/snark/db/repository/PlayerRepository.java).

Other player records represent competing players.

## Research

Research belongs to the player, not to individual planets. The research columns in [`V0001_0000__players.sql`](../../snark/src/main/resources/db/migration/V0001_0000__players.sql) and [`PlayerEntity.java`](../../snark/src/main/java/org/enoch/snark/db/entity/PlayerEntity.java) store that shared player state.

## Planets and moons

Each player owns planets. Planets are modeled separately in [`PlanetEntity.java`](../../snark/src/main/java/org/enoch/snark/db/entity/PlanetEntity.java).

Some planets are moons, which are represented by `ColonyType.MOON` in the same planetary model.

## Lifeform research

Lifeform research follows the same grouped-field approach as regular research, but it is stored on the planet model in `PlanetEntity`.
