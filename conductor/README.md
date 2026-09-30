# Conductor

Conductor is an application for managing multiple instances from different accounts. It enables centralized management, monitoring, and control of multiple instance processes running concurrently.

## Features

- 🚀 **Instance Management** - create, start, stop, delete, and toggle auto-start for instances
- 🔐 **Multi-Account Support** - handle profiles from different sources
- 📊 **Monitoring** - track instance status and logs
- ⚙️ **Configuration** - flexible configuration of instance directories
- 🧩 **Templates** - create instances from placeholder-based start properties
- ⚡ **Auto-Start** - automatically start instances marked with auto-start on application startup

## Requirements

- Java 21+
- Maven 3.6+

## Building the Project

```bash
mvn clean package
```

## Running the Application

Conductor starts the worker application that actually runs game instances. To make it work reliably, you can tell Conductor which JAR to use by setting the `--worker` argument.

### What is the worker?
The worker is the Java program that launches and manages a single instance. Conductor is only the manager that decides what to run and monitors it.

### Worker lookup order
Conductor looks for the worker in this order:
1. `--worker=/path/to/worker.jar` — the explicit path you provide
2. Any JAR in the current working directory, except files starting with `conductor`
3. A JAR in `snark/target`
4. If nothing is found, it stops with an exception

When the worker is found, Conductor logs the full absolute path used.

### Example
```bash
java -jar conductor.jar --worker=/opt/workers/game-worker.jar
```

### With Default Instance Directory

The application will store instances in the `instances` directory in the current working directory:

```bash
java -jar conductor.jar
```

### With Custom Instance Directory

You can specify a custom directory for storing instances using the `--instances-dir` parameter:

```bash
java -jar conductor.jar --instances-dir=/path/to/custom/directory
```

### With Custom Start Properties Directory

You can specify a custom directory for storing start-property templates using the `--start-properties-dir` parameter:

```bash
java -jar conductor.jar --start-properties-dir=/data/start-properties
```

## Startup Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `--worker=<path>` | Full path to the worker JAR used to run instances | auto-detect in working dir / `snark/target` |
| `--instances-dir=<path>` | Path to the directory where instances will be stored | `instances` |
| `--start-properties-dir=<path>` | Path to the directory where start-property templates are stored | `start-properties` |

## Auto-Start Feature

Instances can be configured to automatically start when the application starts. To enable auto-start for an instance, set the `autoStart` field to `true` in the instance's `config.json` file:

```json
{
  "id": "my-instance",
  "autoStart": true,
  "properties": "server.properties"
}
```

When the application starts, it will automatically launch all instances with `autoStart` set to `true`. This happens after all Spring beans are initialized.

## Start Properties Templates

The application creates a `start-properties/` directory on startup if it does not exist. It also seeds a default `start.properties` template from `src/main/resources/start-properties/start.properties`, for example:

```properties
main.time=on
define.fly_points=
define.transporterSmall=5000

fleet_save_module.main.template=../templates/fleet_save.properties
sleep_module.main.template=../templates/sleep.properties
clear_module.main.template=../templates/clean.properties
build_module.main.template=../templates/build.properties
expedition_module.main.template=../templates/expedition.properties
space_module.main.template=../templates/space.properties
swarn_module.main.template=../templates/swarn.properties
main.template=../snark/templates/global.properties

consumer.login=<login>
consumer.password=<password>
consumer.server=<server_name>
consumer.url=<url>
```

When creating a new instance, pick a template from the UI. Conductor copies it to `server.properties` inside the instance directory and then asks for values for every `<placeholder>` found in the template before writing the final file.

## Template Properties

The application also creates a `templates/` directory on startup if it does not exist and fills it with the bundled files from `src/main/resources/template-properties/`.

## Architecture

### Main Components

- **ProcessService** - core service for managing instance lifecycle (start, stop, restart, auto-start)
- **ProfileRepository** - manages instance profiles (loading, saving configuration)
- **StartPropertiesRepository** - manages start-property templates and placeholder replacement
- **InstanceProfile** - represents instance configuration
- **InstanceRuntime** - represents a running instance

### Directory Structure

```
instances/
├── instance-1/
│   ├── config.json      # Instance configuration
│   ├── server.properties
│   ├── logs/            # Application logs
│   └── data/            # Instance data
├── instance-2/
│   ├── config.json
│   ├── server.properties
│   ├── logs/
│   └── data/
└── ...
```

```
start-properties/
└── start.properties

templates/
├── build.properties
├── clean.properties
├── clear.properties
├── duty.properties
├── expedition.properties
├── global.properties
└── sleep.properties
```

## Configuration

The application is configured through command-line parameters. You can change the instance directory by passing the `--instances-dir` parameter when running the application.

Instances are configured through their `config.json` files, where you can set properties like `autoStart` and the relative `properties` path.

## License

See the LICENSE file

## Author

Enoch
