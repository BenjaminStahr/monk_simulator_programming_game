# Monk Simulator - Programming Game
Monk Simulator is a puzzle simulation app developed for a university project during my bachelor. It allows users to create custom maps and program a Monk entity to solve puzzles by collecting good karma and neutralizing bad karma. The app demonstrates simulation logic and problem-solving through customizable game elements.

The overall UI is presented below:
![Overview UI](https://github.com/BenjaminStahr/monk_simulator_programming_game/blob/master/preview.png)

## Setup

Java: OpenJDK 1.8.0_311

To execute the project, the main function in src/App.java must be executed.

## Functionality

Monk Simulator includes four main features:

1. **Map Creation**: Users can design custom grids with elements like ground tiles, good karma, bad karma, and holes.
2. **Programmable Monk**: Users can write code to control the Monk’s actions, such as moving forward, collecting good karma, or neutralizing bad karma.
3. **Puzzle Solving**: The goal is to navigate the Monk through obstacles and solve puzzles by collecting and neutralizing karma.
4. **Simulation Control**: Users can start, pause, and stop the simulation to watch how the Monk executes programmed actions in real time.

## Architecture

The app is developed in Java with JavaFX for the graphical interface. Monk Simulator follows the MVC (Model-View-Controller) architecture:

- **Controller**: The `MainController` handles user input, simulation control, and network communication.
- **View**: `MainView` manages the user interface, showing the map and simulation progress.
- **Model**: `ModelMonk` and `ModelTerritory` store the Monk’s state and the map's elements, including all actions within the environment. Data is stored in an embedded database for persistence.
