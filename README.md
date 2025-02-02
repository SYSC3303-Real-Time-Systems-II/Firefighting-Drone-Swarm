# Firefighting-Drone-Swarm -- ITERATION-1

#Overview
This project simulates a fire incident management system where a FireIncidentSubsystem detects fire events, a Scheduler prioritizes and schedules them, and a DroneSubsystem handles the events. The system uses buffers (RelayBuffer and EventBuffer) for communication between subsystems.



## Files Included

1. **Main.java** - The entry point of the application. Initializes and starts the subsystems.
2. **FireIncidentSubsystem.java** - Reads fire incident data and sends it to the Scheduler.
3. **Scheduler.java** - Prioritizes events and schedules them for the DroneSubsystem.
4. **DroneSubsystem.java** - Simulates a drone handling fire events.
5. **InputEvent.java** - Represents a fire incident event with details like time, zone, severity, and status.
6. **Coordinate.java** - Represents a 2D coordinate (x, y) used for zone and event locations.
7. **Zone.java** - Represents a geographical zone with start, end, and center coordinates.
8. **RelayPackage.java** - Represents a package used for communication between subsystems.
9. **EventBuffer.java** - A thread-safe buffer for storing and retrieving events.
10. **RelayBuffer.java** - A thread-safe buffer for storing and retrieving relay packages.
11. **Status.java** - An enum representing the status of an event (COMPLETE or UNRESOLVED).
12. **EventType.java** - A enum to represents different types of events.
13. **Serverity.java** -  Represents different levels of severity with associated amount of water/foam needed for each event severity
14. **Systems.java** - A enum Represents different subsystems within the program.



## Setup Instructions
1. **Prerequisites**:
   - Ensure you have Java Development Kit (JDK) installed on your system.

2. **File Structure**:
   - Ensure the input files are placed in a `\data` folder within the project directory.

3. **Run the Program**:
   - Once Program is complied, run the program by running the Main.java class. 	



## How It Works

1. **FireIncidentSubsystem**:
   - Reads fire incident data from `Sample_event_file.csv` and zone data from `sample_zone_file.csv`.
   - Sends zone information and fire events to the Scheduler via the RelayBuffer.
   - Checks RelayBuffer to see if it has gotten any acknowledgement for the packages it sent.  

2. **Scheduler**:
   - Receives events from the FireIncidentSubsystem and prioritizes them based on severity.
   - Sends the highest-priority events to the DroneSubsystem via the EventBuffer.
   - Receives confirmation from the DroneSubsystem and sends it back to the FireIncidentSubsystem.

3. **DroneSubsystem**:
   - Receives events from the Scheduler and simulates handling them.
   - Calculates travel time and updates the event status to COMPLETE.
   - Sends confirmation back to the Scheduler.

4. **Buffers**:
   - `RelayBuffer` is used for communication between the FireIncidentSubsystem and Scheduler.
   - `EventBuffer` is used for communication between the Scheduler and DroneSubsystem.



## Input Files

1. **Sample_event_file.csv**:
   - Contains fire incident events with columns: `time`, `zone_id`, `event_type`, `severity`.

2. **sample_zone_file.csv**:
   - Contains zone information with columns: `zone_id`, `zone_start`, `zone_end`.


## Output
The program outputs messages to the console, showing the flow of events and confirmations between subsystems.\
Example output:

FIS: SENDING --> ZONE_PKG_1 TO: Scheduler\
FIS: SENDING --> INPUT_EVENT_0 TO: Scheduler\
Scdlr: Added zones: {1=Zone ID: 1 Zone Start: (0.0, 0.0) Zone End: (700.0, 600.0) Zone Center: (350.0, 300.0), 2=Zone ID: 2 Zone Start: (0.0, 600.0) Zone End: (650.0, 1500.0) Zone Center: (325.0, 1050.0)}\
Scdlr: Received <-- INPUT_EVENT_0 FROM: FireIncidentSubsystem\
Scdlr: SENDING --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: DroneSubsystem\
Drone: HANDLING EVENT: Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
FIS: SENDING --> INPUT_EVENT_1 TO: Scheduler\
Drone: COMPLETED EVENT: Time: 14:01:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
Drone: SENDING --> Time: 14:01:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: Scheduler\
Scdlr: Received <-- Time: 14:01:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High FROM: DroneSubsystem\
Scdlr: SENDING CONFIRMATION FOR --> DRONE_CONFIRMATION TO: FireIncidentSubsystem\
Scdlr: Received <-- INPUT_EVENT_1 FROM: FireIncidentSubsystem\
Scdlr: SENDING --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate TO: DroneSubsystem\
Drone: HANDLING EVENT: Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate\
FIS: Received <-- DRONE_CONFIRMATION FROM: Scheduler\
Drone: COMPLETED EVENT: Time: 14:12:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate\
Drone: SENDING --> Time: 14:12:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate TO: Scheduler\
Scdlr: Received <-- Time: 14:12:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate FROM: DroneSubsystem\
Scdlr: SENDING CONFIRMATION FOR --> DRONE_CONFIRMATION TO: FireIncidentSubsystem\
FIS: Received <-- DRONE_CONFIRMATION FROM: Scheduler\
