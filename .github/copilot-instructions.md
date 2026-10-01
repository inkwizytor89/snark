# Project instructions

## Project overview

This repository contains a multi-instance Java system composed of three main parts:

- `common`: shared implementations and common contracts used by both `conductor` and `snark`.
- `conductor`: the orchestration layer that manages multiple independent `snark` instances.
- `snark`: the worker/agent instance that executes the configuration assigned to it.

## Architecture

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

## Repository guidance

- Prefer concise, readable, maintainable code.
- Keep architectural decisions explicit in code and documentation.
- Do not add unnecessary modules or duplicated logic.
- When the project evolves, maintain the separation of responsibilities between `common`, `conductor`, and `snark`.

## File size rule

This file must remain below 300 lines. If it grows beyond that limit, it should be reduced and restructured to stay within the limit.
