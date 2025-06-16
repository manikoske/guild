# Logging System

## Design
The logging system is stateless and uses the final `Encounter.State` to generate all logs:
1. The simulation runs without any logging
2. At the end, the final `Encounter.State` is passed to the logging system
3. The logging system generates all logs from this state

Benefits:
- Stateless design is easier to test and reason about
- Decoupled from the simulation process
- All logging logic is centralized
- Can generate different views of the same data without re-running the simulation

## Usage
- `StatelessLoggingManager.configure()` is used to set up logging preferences
- `StatelessLoggingManager.debug()` can be used for debug logging
- `StatelessLoggingManager.logEncounter()` is the main entry point for logging an encounter
- `StatelessLoggingManager.logSimulationResults()` provides summary information for tests

## Implementation Details
- The simulation in `Encounter.kt` runs without logging
- At the end, the final `Encounter.State` is passed to `StatelessLoggingManager.logEncounter()`
- The logging manager then generates all logs from this state, including round and turn details
- No logging calls are needed in `Round.kt` and `Turn.kt`
