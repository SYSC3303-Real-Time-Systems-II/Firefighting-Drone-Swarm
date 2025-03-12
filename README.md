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
13. **Severity.java** -  Represents different levels of severity with associated amount of water/foam needed for each event severity
14. **Systems.java** - A enum Represents different subsystems within the program.
15. **Drone.java** - Represents the drone having its states and a unique identifier .
16. **SchedulerState.java** -  A enum to represent the different types of states for the scheduler.
17. **DroneState.java** -  A enum to represent the different types of states for the drone.
18. **DroneSubsystemState.java** - A enum to represent the different types of states for the drone subsystem.


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
   - Finds an available drone to be sent to the zone that was requested. 
   - Sends confirmation back to the Scheduler.

4. **Buffers**:
   - `RelayBuffer` is used for communication between the FireIncidentSubsystem and Scheduler.
   - `EventBuffer` is used for communication between the Scheduler and DroneSubsystem.

5. **Drone**
   - Sent out by the drone subsystem to go a fire zone or a drone requested zone.
   - Transitions through its states such to travel, take out a fire, and return to the station.
   - Alerts the drone subsystem when it has arrived to a zone which tells the scheduler.



## Input Files

1. **Sample_event_file.csv**:
   - Contains fire incident events with columns: `time`, `zone_id`, `event_type`, `severity`.

2. **sample_zone_file.csv**:
   - Contains zone information with columns: `zone_id`, `zone_start`, `zone_end`.


## Output
The program outputs messages to the console, showing the flow of events and confirmations between subsystems.\
Example output:

FIS: SENDING --> ZONE_PKG_1 TO: Scheduler\
FIS: SENDING --> INPUT_EVENT_1 TO: Scheduler\
Scdlr: Added zones: {1=Zone ID: 1 Zone Start: (0.0, 0.0) Zone End: (700.0, 600.0) Zone Center: (350.0, 300.0), 2=Zone ID: 2 Zone Start: (0.0, 600.0) Zone End: (650.0, 1500.0) Zone Center: (325.0, 1050.0)}\
Scdlr: RECEIVED AN EVENT <-- INPUT_EVENT_1 FROM: FireIncidentSubsystem\
Scdlr: SENDING THE EVENT --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: DroneSubsystem\
DS: RECEIVED EVENT FROM SCHEDULER --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
DS: HANDLING EVENT: Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
Drone1: AVAILABLE TO HANDLE --> : Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
Drone1: TRAVELING TO ZONE: 1 : AT TIME: 14:00:15\
Drone1: ARRIVED AT ZONE: 1 : AT TIME: 14:00:37\
Drone1: COMPLETED EVENT (ARRIVED AT ZONE): Time: 14:00:37 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
DS: SENDING EVENT TO SCHEDULER --> Time: 14:00:37 Zone: 1 Event Type: FIRE_DETECTED Severity: High\
Drone1: DROPPING WATER: AT TIME: 14:00:37\
Drone1: WATER DROPPED, RETURNING TO BASE: AT TIME: 14:00:57\
Scdlr: RECEIVED EVENT <-- Time: 14:00:37 Zone: 1 Event Type: FIRE_DETECTED Severity: High FROM: DroneSubsystem\
Drone1: ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: 14:01:15\
Scdlr: SENDING CONFIRMATION FOR --> Time: 14:00:37 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: FireIncidentSubsystem\
FIS: SENDING --> INPUT_EVENT_2 TO: Scheduler\
Scdlr: RECEIVED AN EVENT <-- INPUT_EVENT_2 FROM: FireIncidentSubsystem\
Scdlr: SENDING THE EVENT --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate TO: DroneSubsystem\
DS: RECEIVED EVENT FROM SCHEDULER --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate\
FIS: Received <-- DRONE_CONFIRMATION FROM: Scheduler\

## Contributions (Iteration 2)

1. **Rami Ayoub**
   - Implemented the Drone class including the states of the drones and updated the Scheduler and DroneSubsystem classes to also have states and handle the communication. 
   - Ensured that the logic of the Firefighting Drone Swarm System aligned with iteration 2.
   - Updated the read me file. 

2. **Louis Pantazopoulos**
   - Created test classes and ensured correct logic for both updated and new classes.
   - Helped in the contribution of the Drone class by brainstorming and implementing the states of the drone. 

3. **Sarah AlSaady**
   - Updated the UML class diagram including new and updated classes and well as class relationships. 
   - Helped in the contribution of the state machine for the DroneSubsystem class.
   
4. **Liam Bennet**
   - Created the state machine diagrams for the DroneSubsystem and Scheduler classes. 
   - Helped contribute towards the state machine for the DroneSubsystem class. 

5. **Tharusha Herath**
   - Created test classes and ensured correct logic for both updated and new classes.
   - Suggested the implementation of the Drone, DroneSubsystem and Scheduler to be a switch case state machine and contributed to implementation of the Drone class states. 

6. **Ranveer Dhaliwal**
   - Suggested how to approach iteration 2 by planning the core logic for the communication between the Scheduler and DroneSubsystem and the state transitions.
   - Updated the sequence diagram and helped contribute to the state machine for the Scheduler class. 
