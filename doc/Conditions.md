## 🧩 Available Conditions

### 🟦 `RESOURCE_COUNT`

**Description:**  
Checks whether the specified planet has at least the given total amount of resources.

**Parameters:**

| Field | Type | Description |
|--------|------|-------------|
| `type` | `"RESOURCE_COUNT"` | Identifier of the condition type |
| `planet` | `String` / `Planet` | The planet to check |
| `resourcesCount` | `String` | Minimum required total resource amount (e.g. `"100k"`, `"2M"`) |

**Example:**
```json
{
  "type": "RESOURCE_COUNT",
  "planet": "source",
  "resourcesCount": "100k"
}
```

### 🟩 `RESOURCE`

**Description:**  
Checks whether the specified planet has the required resources and optionally defines a reserve amount that must remain untouched.

**Parameters:**

| Field | Type | Description |
|--------|------|-------------|
| `type` | `"RESOURCE"` | Identifier of the condition type |
| `planet` | `String` / `Planet` | The planet on which to check resource availability |
| `resources` | `Resources` | The required resource amounts for the action |
| `leaveResources` | `Resources` _(optional)_ | The amount of each resource that should remain on the planet after the action |

**Example:**
```json
{
  "type": "RESOURCE",
  "planet": "source",
  "resources": "m1k c2kk d3m",
  "leaveResources": "m1 c2k d1m"
}
```

### 🟨 `SHIPS`

**Description:**  
Verifies that the specified planet has the required number of ships, and optionally defines how many ships should remain on the planet after the action.

**Parameters:**

| Field | Type | Description |
|-------|------|-------------|
| `type` | `"SHIPS"` | Identifier of the condition type |
| `planet` | `String` / `Planet` | The planet to check for ships |
| `shipsMap` | `ShipsMap` | The required number of ships to perform the action |
| `leave` | `ShipsMap` _(optional)_ | Ships that should remain on the planet |

**Example:**
```json
{
  "type": "SHIPS",
  "planet": "source",
  "shipsMap": "transporterLarge:1000,explorer:2,battleship:1",
  "leave": "transporterLarge:1000,explorer:2,battleship:1"
}
```

### 🟥 `ATTACK_NOT_EXPIRED`

**Description:**  
Checks whether a specified amount of time (in seconds) has passed since a fleet-related event, such as a mission or return.

**Parameters:**

| Field | Type | Description |
|-------|------|-------------|
| `type` | `"ATTACK_NOT_EXPIRED"` | Identifier of the condition type |
| `seconds` | `Long` | Number of seconds after which the condition becomes valid |
| `is` | `Boolean` | Whether the condition checks if time *has passed* (`true`) or *has not passed yet* (`false`) |
| `target` | `String` / `Planet` | Target planet of the fleet |

**Example:**
```json
{
  "type": "EXPIRED_FLEET",
  "seconds": 300,
  "is": true,
  "target": "target"
}
```


### 🟪 `SPY_NOT_EXPIRED`

**Description:**  
Checks whether a specified amount of time (in seconds) has passed since a fleet-related event, such as a mission or return.

**Parameters:**

| Field | Type | Description |
|-------|------|-------------|
| `type` | `"SPY_NOT_EXPIRED"` | Identifier of the condition type |
| `seconds` | `Long` | Number of seconds after which the condition becomes valid |
| `is` | `Boolean` | Whether the condition checks if time *has passed* (`true`) or *has not passed yet* (`false`) |
| `target` | `String` / `Planet` | Target planet of the fleet |

**Example:**
```json
{
  "type": "EXPIRED_FLEET",
  "seconds": 300,
  "is": true,
  "target": "target"
}
```

### 🟪 `FLEET_SLOT`

**Description:**  
Checks whether the player has at least the required number of free fleet slots available to perform an action.

**Parameters:**

| Field | Type | Description |
|-------|------|-------------|
| `type` | `"FLEET_SLOT"` | Identifier of the condition type |
| `freeSlots` | `Integer` | Minimum number of free fleet slots required |

**Example:**
```json
{
  "type": "FLEET_SLOT",
  "freeSlots": 3
}
```

### 🟫 `NO_MISSIONS`

**Description:**  
Checks whether the specified source planet does **not** have any ongoing missions that would block the action.  
You can specify which types of missions should be considered as blocking.

**Parameters:**

| Field | Type | Description |
|-------|------|-------------|
| `type` | `"NO_MISSIONS"` | Identifier of the condition type |
| `source` | `Planet` | The source planet to check for ongoing missions |
| `blockingMissions` | `List<Mission>` _(optional)_ | List of mission types that should block the action. If `null`, all missions are considered non-blocking |

**Example:**
```json
{
  "type": "NO_MISSIONS",
  "source": "source",
  "blockingMissions": ["ATTACK", "TRANSPORT"]
}
```