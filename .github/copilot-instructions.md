# Project instructions

## Project overview

This repository contains a multi-instance Java system composed of three main parts:

- `common`: shared implementations and common contracts used by both `conductor` and `snark`.
- `conductor`: the orchestration layer that manages multiple independent `snark` instances.
- `snark`: the worker/agent instance that executes the configuration assigned to it.

## Architecture

The `doc` folder contains the project documentation and should be treated as the primary source of design and domain explanations. It describes the architecture, domain rules, and implementation decisions in a structured way so that new contributors can find the relevant information without guessing. The `## Documentation` section is a short index explaining what is documented in more detail in the files inside `doc`.

`common` contains reusable code shared between the modules so the conductor and worker stay aligned on communication contracts and utility behavior.

`conductor` starts, stops, and monitors many independent `snark` instances. Each instance is identified by an `instanceId` and is assigned its own database configuration. Communication between the conductor and worker instances is performed through WebSockets. The conductor is responsible for lifecycle operations and for polling the current status of each instance.

`snark` is a single instance of the worker. It follows the task/configuration assigned by the conductor. If a database is provided to the instance, it uses that database. Otherwise, it creates and uses its own local H2 database. `snark` is a passive runtime component: it reacts to commands and status requests coming from the conductor.

## Operating model

- `conductor` owns orchestration, instance lifecycle, and status reporting.
- `snark` executes runtime logic for a single configured instance.
- Each `snark` instance is isolated by its `instanceId` and its assigned database context.
- The conductor may start, stop, and query the state of any `snark` instance at any time.

## Documentation and comments

All project documentation, code comments, and inline explanations must be written in English.

Keep comments clear, brief, and factual. Do not mix English and Polish in project docs or source comments.

## Documentation

### Domain

- Domain-specific documentation and behaviour definitions for this project are in the `doc/domain` folder; it describes the business rules and domain concepts used by the application.
- Player domain: see [doc/domain/Player.md](../doc/domain/Player.md) for the player model, main player, research ownership, and planet/moon relationships.
- Coordinate domain: see [doc/domain/Coordinate.md](../doc/domain/Coordinate.md) for coordinate types, parsing, and the relationship between planets, moons, targets, and debris.

### Architecture

- Architecture and system-level design notes are in the `doc/architecture` folder; it documents the project structure, component boundaries, and technical design.
- Action execution flow in conductor: see [doc/decisions/Action in conductor.md](../doc/decisions/Action%20in%20conductor.md). This document explains that the conductor owns the action list and that GUI/terminal actions must pass through the conductor instead of invoking worker logic directly.

### Decisions

- Decision records and implementation constraints are stored in `doc/decisions`; each note describes a design choice, the rationale behind it, and the relevant enforcement rules.
- Distributed documentation: see [doc/decisions/Distributed documentation.md](../doc/decisions/Distributed%20documentation.md) for the rule that overviews stay brief and point to one detailed source instead of repeating the same content.

## Repository guidance

- Prefer concise, readable, maintainable code.
- Keep architectural decisions explicit in code and documentation.
- Do not add unnecessary modules or duplicated logic.
- When the project evolves, maintain the separation of responsibilities between `common`, `conductor`, and `snark`.

## File size rule

This file must remain below 300 lines. If it grows beyond that limit, it should be reduced and restructured to stay within the limit.
