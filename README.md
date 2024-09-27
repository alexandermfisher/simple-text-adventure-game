
# Simple Text Adventure Game (STAG) Engine

This project implements a socket-server game engine for running text adventure games. The server communicates with one or more game clients, handling player commands and managing game state based on configuration files defining game entities and actions.

## Features

The STAG engine supports the following core functionality:

### Standard Built-in Commands

| **Command** | **Description** |
|-------------|-----------------|
| **inventory (inv)** | Lists all artefacts currently held by the player. |
| **get**       | Picks up a specified artefact from the current location. |
| **drop**      | Drops an artefact from the player's inventory. |
| **goto**      | Moves the player to a specified location if a path exists. |
| **look**      | Displays entities in the current location and lists available paths. |

### Custom Actions

In addition to the built-in commands, game-specific actions are defined in an `actions` configuration file, with triggers that players can invoke based on the current game state. Each action may involve:
- Trigger phrases.
- Subject entities that must be available for the action.
- Consumed and produced entities.
- A narration describing the result of the action.

### Game Entities

Entities in the game include:
- **Locations**: Represent places within the game world.
- **Artefacts**: Items that can be collected by the player.
- **Furniture**: Non-collectible objects within a location.
- **Characters**: Non-player entities that interact with the player.
- **Players**: Represent the users playing the game.

Entities are loaded from a configuration file in DOT format, which defines the structure of the game world and the relationships between locations and entities.

### Multiple Players

The game engine supports multiple players simultaneously, with each player:
- Having their own inventory and location.
- Being able to see other players in the same location using the `look` command.
  
### Health System

Players have a health level that can be affected by certain actions (e.g., consuming potions). The player's health starts at 3, and if it drops to 0, they lose their inventory and are transported back to the start location.

## Configuration Files

Two configuration files are used to define game behavior:
- **Entities File**: Describes the locations, artefacts, characters, and other entities in the game world.
- **Actions File**: Defines the possible actions that can be performed in the game.

These files allow different game scenarios to be loaded into the same engine, providing versatility.

## Prerequisites

To run the STAG engine, you need the following installed on your system:

- **Java 17**: Ensure that Java 17 or higher is installed. You can check your Java version by running:
  ```bash
  java -version
  ```

- **Maven**: This project uses Maven for build and dependency management. You can either install Maven on your system or use the included Maven wrapper (`mvnw`).

- **Graphviz Library (JPGD)**: The project uses the JPGD parser for handling DOT files. Ensure the `libs` directory contains the `jpgd.jar` file.

To ensure you have the necessary dependencies, these are automatically resolved when building with Maven.

## Running the Project

1. **Clone the repository**:
   ```bash
   git clone https://github.com/alexandermfisher/simple-text-adventure-game.git
   cd simple-text-adventure-game
   ```

2. **Build and run the server**:
   ```bash
   ./mvnw clean package
   ./mvnw exec:java@server
   ```

3. **Run the client**:
   To connect to the server with a player name, run:
   ```bash
   ./mvnw exec:java@client -Dexec.args="player_name"
   ```

4. **Run unit tests**:
   To run the unit tests, use:
   ```bash
   ./mvnw test
   ```

## Project Structure

The project is structured using Maven and includes the following key components:
- **GameServer**: The main server class responsible for handling player commands and managing game state.
- **EntityParser**: Responsible for loading and parsing the entities configuration file.
- **ActionParser**: Responsible for loading and parsing the actions configuration file.
- **CommandHandler**: Processes player commands and triggers the appropriate actions.
- **JUnit Tests**: Unit tests to ensure correct functionality.

## Acknowledgments

This project is an assignment from the **Object-Oriented Programming with Java** course at the University of Bristol (2023/24), taught by **Dr. Simon Lock** and **Dr. Sion Hannuna**. The project was built using a base Maven project template provided by the course.

Special thanks to Simon for guidance and support throughout the project. You can find more about his work and contributions on his [GitHub profile](https://github.com/drslock).