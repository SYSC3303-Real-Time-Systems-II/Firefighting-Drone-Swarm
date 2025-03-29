# Firefighting-Drone-Swarm -- ITERATION-3

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
   - Contains fire incident events with columns: `time`, `zone_id`, `event_type`, `severity`, `fault_type` .

2. **sample_zone_file.csv**:
   - Contains zone information with columns: `zone_id`, `zone_start`, `zone_end`.


## Output
The program outputs messages to the console, showing the flow of events and confirmations between subsystems.\
Example output:

*FireIncident*

- [FIS] FIREINCIDENTSUBSYSTEM STARTED...
- [FIS] SENDING --> ZONE_PKG_1 TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_1 TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_2 TO: Scheduler
- [FIS] RECEIVED FAULT CONFIRMATION: FAULT_CONFIRMATION FOR INPUT_EVENT_1 WILL RESEND FOR RESCHEDULING TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_3 TO: Scheduler
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_2
- [FIS] SENDING --> INPUT_EVENT_4 TO: Scheduler
- [FIS] RECEIVED FAULT CONFIRMATION: FAULT_CONFIRMATION FOR INPUT_EVENT_3 WILL RESEND FOR RESCHEDULING TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_5 TO: Scheduler
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_4
- [FIS] SENDING --> INPUT_EVENT_6 TO: Scheduler
- [FIS] RECEIVED FAULT CONFIRMATION: FAULT_CONFIRMATION FOR INPUT_EVENT_5 WILL RESEND FOR RESCHEDULING TO: Scheduler
- [FIS] SENDING --> INPUT_EVENT_1 TO: Scheduler
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_6
- [FIS] SENDING --> INPUT_EVENT_3 TO: Scheduler
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_1
- [FIS] SENDING --> INPUT_EVENT_5 TO: Scheduler
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_3
- [FIS] RECEIVED COMPLETED CONFIRMATION: DRONE_CONFIRMATION FOR INPUT_EVENT_5

*Scheduler*

- [Scdlr] subsystem started...
- [Scdlr] Added zones: {1=Zone ID: 1 Zone Start: (0.0, 0.0) Zone End: (700.0, 600.0) Zone Center: (350.0, 300.0), 2=Zone ID: 2 Zone Start: (0.0, 600.0) Zone End: (650.0, 1500.0) Zone Center: (325.0, 1050.0)}
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) TO: DroneSubsystem
- [Scdlr] RECEIVED FAULT CONFIRMATION <-- INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) TO: DroneSubsystem
- [Scdlr] RECEIVED FAULT CONFIRMATION <-- INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: FireIncidentSubsystem-
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) TO: DroneSubsystem
- [Scdlr] RECEIVED FAULT CONFIRMATION <-- INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) TO: FireIncidentSubsystem
- [Scdlr] RECEIVED AN EVENT <-- INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: FireIncidentSubsystem
- [Scdlr] SENDING THE EVENT --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) TO: DroneSubsystem
- [Scdlr] RECEIVED COMPLETED CONFIRMATION <-- INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: DroneSubsystem
- [Scdlr] SENDING CONFIRMATION FOR --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) TO: FireIncidentSubsystem

*DroneSubsystem*

- [DS] SUBSYSTEM STARTED WITH 10 DRONES.
- [Drone5] WAITING FOR EVENT.
- [Drone7] WAITING FOR EVENT.
- [Drone2] WAITING FOR EVENT.
- [Drone8] WAITING FOR EVENT.
- [Drone4] WAITING FOR EVENT.
- [Drone1] WAITING FOR EVENT.
- [Drone9] WAITING FOR EVENT.
- [Drone3] WAITING FOR EVENT.
- [Drone6] WAITING FOR EVENT.
- [Drone10] WAITING FOR EVENT.
- [DS] RECEIVED EVENT --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_1 TO: Drone1
- [Drone1] GOT INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) AT TIME: 13:55:05
- [Drone1] ASCENDING AT TIME: 13:55:27.051
- [Drone1] MESSAGE RECEIVED IS CORRUPTED.
- [Drone1] RESTARTING DRONE...
- [DS] Drone1: FAILED TO COMPLETE INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low)
- [DS] RECEIVED EVENT --> INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_2 TO: Drone2
- [Drone2] GOT INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate) AT TIME: 13:58:54
- [Drone2] ASCENDING AT TIME: 13:59:16.051
- [DS] Drone2: COMPLETED INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_2 (Time: 13:58:54 Zone: 1 Event Type: DRONE_REQUEST Severity: Moderate)
- [DS] RECEIVED EVENT --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_3 TO: Drone3
- [Drone3] GOT INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) AT TIME: 14:00:15
- [Drone3] ASCENDING AT TIME: 14:01:07.051
- [Drone3] NOZZLE IS JAMMED.
- [Drone3] NOW OFFLINE.
- [DS] Drone3: FAILED TO COMPLETE INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High)
- [DS] RECEIVED EVENT --> INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_4 TO: Drone4
- [Drone4] GOT INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low) AT TIME: 14:06:42
- [Drone4] ASCENDING AT TIME: 14:07:34.051
- [Drone1] DRONE RESTARTED.
- [Drone1] WAITING FOR EVENT.
- [DS] Drone4: COMPLETED INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_4 (Time: 14:06:42 Zone: 2 Event Type: FIRE_DETECTED Severity: Low)
- [DS] RECEIVED EVENT --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_5 TO: Drone1
- [Drone1] GOT INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) AT TIME: 14:12:17
- [Drone1] ASCENDING AT TIME: 14:13:09.051
- [Drone1] GOT STUCK MID-FLIGHT AND IS GOING OFFLINE.
- [Drone1] NOW OFFLINE.
- [Drone2] CRUISING TO ZONE: 1 AT TIME: 13:59:16.051
- [Drone2]: DROPPING WATER (20 L) at time: 13:59:16.051
- Drone2: Remaining battery 98.00%
- [Drone2]: RETURNING TO BASE: AT TIME: 13:59:36.051
- [DS] Drone1: FAILED TO COMPLETE INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate)
- [DS] RECEIVED EVENT --> INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_6 TO: Drone5
- [Drone5] GOT INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High) AT TIME: 14:22:57
- [Drone5] ASCENDING AT TIME: 14:23:19.051
- [DS] Drone5: COMPLETED INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_6 (Time: 14:22:57 Zone: 1 Event Type: DRONE_REQUEST Severity: High)
- [DS] RECEIVED EVENT --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_1 TO: Drone6
- [Drone6] GOT INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low) AT TIME: 13:55:05
- [Drone6] ASCENDING AT TIME: 13:55:27.051
- [DS] Drone6: COMPLETED INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_1 (Time: 13:55:05 Zone: 1 Event Type: DRONE_REQUEST Severity: Low)
- [DS] RECEIVED EVENT --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_3 TO: Drone7
- [Drone7] GOT INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High) AT TIME: 14:00:15
- [Drone7] ASCENDING AT TIME: 14:01:07.051
- [DS] Drone7: COMPLETED INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_3 (Time: 14:00:15 Zone: 2 Event Type: FIRE_DETECTED Severity: High)
- [DS] RECEIVED EVENT --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) FROM: SCHEDULER
- [DS] ASSIGNED INPUT_EVENT_5 TO: Drone8
- [Drone8] GOT INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate) AT TIME: 14:12:17
- [Drone8] ASCENDING AT TIME: 14:13:09.051
- [Drone2] ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: 13:59:43.051
- Drone2: Remaining battery 96.90%
- Drone2: REFILLING WATER...
- [Drone5] CRUISING TO ZONE: 1 AT TIME: 14:23:19.051
- [Drone5]: DROPPING WATER (30 L) at time: 14:23:19.051
- Drone5: Remaining battery 98.00%
- [Drone5]: RETURNING TO BASE: AT TIME: 14:23:39.051
- [DS] Drone8: COMPLETED INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate)
- [DS] SENDING EVENT TO SCHEDULER --> INPUT_EVENT_5 (Time: 14:12:17 Zone: 2 Event Type: DRONE_REQUEST Severity: Moderate)
- Drone2: WATER REFILLED. AVAILABLE AT TIME: 13:59:45.051
- [Drone2] WAITING FOR EVENT.
- [Drone6] CRUISING TO ZONE: 1 AT TIME: 13:55:27.051
- [Drone6]: DROPPING WATER (10 L) at time: 13:55:27.051
- Drone6: Remaining battery 98.00%
- [Drone6]: RETURNING TO BASE: AT TIME: 13:55:47.051
- [Drone4] CRUISING TO ZONE: 2 AT TIME: 14:07:34.051
- [Drone4]: DROPPING WATER (10 L) at time: 14:07:34.051
- Drone4: Remaining battery 98.00%
- [Drone4]: RETURNING TO BASE: AT TIME: 14:07:54.051


## Contributions (Iteration 4)

1. **Rami Ayoub & Tharusha Herath & Ranveer Dhaliwal** 
   - Implemented the redirecting of the hard faults which included when a drone got stuck and the nozzle was jammed by adding new states to the drone to show that is offline. 
   - Updated the UML class diagram.
   - Updated the logic for the systems to deal with the redirecting the fault to be rescheduled by another drone. 

2. **Liam Bennet & Louis Pantazopoulos & Sarah AlSaady**
   - Implemented the redirecting of the transient faults which included the corrupted message making the drone temporarily offline and reboot after a set of time. 
   - Implemented the timing diagram for the drones to show at the states of idle, interrupted, running, offline, etc.  
   - Updated the logic for the systems to deal with the redirecting the fault to be rescheduled by another drone. 

