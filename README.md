# Firefighting-Drone-Swarm -- ITERATION-5

## Overview

This project simulates a fire incident management system where a FireIncidentSubsystem detects fire events, a Scheduler prioritizes and schedules them, and a DroneSubsystem 
finds an available drone to handle the event. The communication between the three subsystems is done using a RPC communication through datagram sockets and packets.

## Files Included

1. **FireIncidentSubsystem.java** - Reads fire incident data and sends it to the Scheduler.
2. **Scheduler.java** - Prioritizes events and schedules them for the DroneSubsystem.
3. **DroneSubsystem.java** - Simulates a drone handling fire events.
4. **InputEvent.java** - Represents a fire incident event with details like time, zone, severity, and status.
5. **Coordinate.java** - Represents a 2D coordinate (x, y) used for zone and event locations.
6. **Zone.java** - Represents a geographical zone with start, end, and center coordinates.
7. **RelayPackage.java** - Represents a package used for communication between subsystems.
8. **DroneStateMachine.java** - An interface that handles the drone state transitions for a drone when it has been scheduled to take action by the drone subsystem. 
9. **Status.java** - An enum representing the status of an event (COMPLETE or UNRESOLVED).
10. **EventType.java** - A enum to represents different types of events.
11. **Severity.java** -  Represents different levels of severity with associated amount of water/foam needed for each event severity
12. **Systems.java** - A enum Represents different subsystems within the program.
13. **Drone.java** - Represents the drone having its states and a unique identifier .
14. **SchedulerState.java** -  A enum to represent the different types of states for the scheduler.
15. **DroneState.java** -  A enum to represent the different types of states for the drone.
16. **DroneSubsystemState.java** - A enum to represent the different types of states for the drone subsystem.
17. **FireIncidentSubsystemStatus.java** - A enum to represent the current state of the fire incident subsystem as it sends a relay package to the scheduler and receives the confirmation.


## Setup Instructions
1. **Prerequisites**:
   - Ensure you have Java Development Kit (JDK) installed on your system.

2. **File Structure**:
   - Ensure the input files are placed in a `\data` folder within the project directory.

3. **Run the Program**:
   - Once Program is complied, run the program by running the three subsystem. First run the drone subsystem main thread, then the scheduler main thread and finally the fire incident subsystem main thread.

   
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

4. **Drone**
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

*FireIncident*

- [FIS] subsystem started...
- [FIS] SENDING --> ZONE_PKG_1 TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_0 TO: Scheduler
- [FIS] Received confirmation for DRONE_CONFIRMATION
- [FIS] SENDING --> INPUT_EVENT_1 TO: Scheduler
- [FIS] Received confirmation for DRONE_CONFIRMATION

*Scheduler*

- [Scdlr] subsystem started...
- [Scdlr] Added zones: {1=Zone ID: 1 Zone Start: (0.0, 0.0) Zone End: (700.0, 600.0) Zone Center: (350.0, 300.0), 2=Zone ID: 2 Zone Start: (0.0, 600.0) Zone End: (650.0, 1500.0) Zone Center: (325.0, 1050.0)}
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_0 FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: DroneSubsystem
- [Scdlr] RECEIVED EVENT <-- Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_1 FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate TO: DroneSubsystem
- [Scdlr] RECEIVED EVENT <-- Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate TO: FireIncidentSubsystem

*Drone*

- Drone2: WAITING FOR EVENT
- Drone5: WAITING FOR EVENT
- Drone4: WAITING FOR EVENT
- Drone1: WAITING FOR EVENT
- Drone3: WAITING FOR EVENT
- [DS] subsystem started with 5 drones
- [DS] received event: Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High
- [DS] Assigned Drone1 to event
- Drone1 GOT EVENTTime: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High
- Drone1: TRAVELING TO ZONE: 1 : AT TIME: 14:00:15
- Drone1: ASCENDING AT TIME: 14:00:37
- Drone1: CRUISING TO ZONE: 1 : AT TIME: 14:00:37
- Drone1: WATER DROPPED, RETURNING TO BASE: AT TIME: 14:00:37
- Drone1: ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: 14:00:55
- Drone1: WAITING FOR EVENT
- [DS] Drone1: COMPLETED EVENT
- [DS] SENDING EVENT TO SCHEDULER --> Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High
- [DS] received event: Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate
- [DS] Assigned Drone1 to event
- Drone1 GOT EVENTTime: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate
- Drone1: TRAVELING TO ZONE: 2 : AT TIME: 14:10:01
- Drone1: ASCENDING AT TIME: 14:10:53
- Drone1: CRUISING TO ZONE: 2 : AT TIME: 14:10:53
- Drone1: WATER DROPPED, RETURNING TO BASE: AT TIME: 14:10:53
- Drone1: ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: 14:11:41
- Drone1: WAITING FOR EVENT
- [DS] Drone1: COMPLETED EVENT
- [DS] SENDING EVENT TO SCHEDULER --> Time: 14:10:01 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate


## Contributions (Iteration 5)

1. **Rami Ayoub & Louis Pantazopoulos & Liam Bennet** 
   - Implemented the RPC communication between the three subsystems creating the packets and the sockets and through serialization.
   - Updated and implemented new test cases. 
   - Updated the read me file.

2. **Ranveer Dhaliwal & Tharusha Herath & Sarah AlSaady**
   - Implemented the drone interface class that handled the state transitioning for a drone.
   - Added the logic for the drone to have a battery life, water tank volume and an algorithm to determine the closest drone to send to a zone. 
   - Updated the UML, sequence and state machine diagrams. 
