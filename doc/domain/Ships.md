# Ships domain

Ships describe fleet composition and fleet movement.

## Fleet model

The fleet is stored in the `fleet` table from [`V0001_0003__fleet.sql`](../../snark/src/main/resources/db/migration/V0001_0003__fleet.sql). [`FleetEntity`](../../snark/src/main/java/org/enoch/snark/db/entity/FleetEntity.java) maps that data in the worker runtime.

Fleet records store the ship counts together with source, target, mission, speed, and timing data.

## Ship types

[`Ship`](../../snark/src/main/java/org/enoch/snark/instance/model/technology/Ship.java) defines the available ship types used by the fleet model.

## Ship maps

[`ShipsMap`](../../snark/src/main/java/org/enoch/snark/instance/model/to/ShipsMap.java) holds a fleet as a map of ship type to amount. A fleet can be stationed on a planet or moon, or it can be traveling between coordinates.

Text configuration uses the `ship:count` format, for example:

`transporterLarge:10,explorer:1,battleship:1`

## Fleet commands

[`FleetSendCommand`](../../snark/src/main/java/org/enoch/snark/action/command/FleetSendCommand.java) describes a fleet dispatch request. [`FleetSendProcessor`](../../snark/src/main/java/org/enoch/snark/action/processor/FleetSendProcessor.java) validates the request, converts the ship selection, persists the fleet, and starts the dispatch flow.
