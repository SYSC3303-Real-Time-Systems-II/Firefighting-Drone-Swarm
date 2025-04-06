import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Interface representing the state of a drone in the state machine.
 */
interface DroneStateMachine {
    /**
     * Handles the state transition and logic for the drone.
     *
     * @param context The current drone instance.
     */
    void handle(Drone context);
}

/**
 * Abstract class representing a state where the drone is in the base.
 */
abstract class InBaseState implements DroneStateMachine {
    @Override
    public abstract void handle(Drone context);
}

/**
 * Abstract class representing a state where the drone is in the field.
 */
abstract class InFieldState implements DroneStateMachine {
    @Override
    public abstract void handle(Drone context);
}

/**
 * State where the drone is available and waiting for an event assignment.
 */
class AvailableState extends InBaseState {
    /**
     * Handles the available state by waiting for an assigned event.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context) {
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.WAITING_FOR_TASK, null, context.getName());

        try{
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            context.getSendReceiveSocket().receive(packet); // Gets the event

            InputEvent event = context.deserializeEvent(packet.getData()); // Deserializes the data

            context.setAssignedEvent(event); // Sets the assigned event of the drone from the one it received
            MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASSIGNED_EVENT, event, context.getName());

            event.setHandlingDrone(context.getName());

            context.setLocalTime(context.getAssignedEvent().getTime());
            System.out.println("["+context.getName() + "] GOT INPUT_EVENT_" + context.getAssignedEvent().getEventID() + " (" + context.getAssignedEvent().toString() + ")" + " AT TIME: " + context.getLocalTime());
            context.setLocalTime(context.getLocalTime().plusNanos((long) (context.ACCELERATION_TIME * 1000000000)));  // Adds the local time
            context.sleepFor(context.ACCELERATION_TIME); // Simulates the acceleration time
            context.setDroneState(new AscendingState()); // The drone becomes on route to the fire zone
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }
//    @Override
//    public void handle(Drone context) {
//        // Lock and wait until a task is assigned
//        context.waitForTask();
//        context.setLocalTime(context.getCurrentEvent().getTime());
//        System.out.println("["+context.getName() + "] GOT INPUT_EVENT_" + context.getCurrentEvent().getEventID() + " (" + context.getCurrentEvent().toString() + ")" + " AT TIME: " + context.getLocalTime());
//        context.setLocalTime(context.getLocalTime().plusNanos((long) (context.ACCELERATION_TIME * 1000000000)));  // Adds the local time
//        context.sleepFor(context.ACCELERATION_TIME); // Simulates the acceleration time
//        context.setDroneState(new AscendingState()); // The drone becomes on route to the fire zone
//    }
}

/**
 * State where the drone is ascending after an event assignment.
 */
class AscendingState extends InBaseState {
    /**
     * Handles the ascending state by simulating the travel to the target zone.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context) {
        double travelZoneTime = context.calculateZoneTravelTime(context.getAssignedEvent()); // Calculates the travel time of the zone
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime)); // Adds the local time
        context.sleepFor(context.ACCELERATION_TIME);
        System.out.println("[" + context.getName() + "] ASCENDING AT TIME: " + context.getLocalTime());
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASCENDING, context.getAssignedEvent(), context.getName());

        // Handle if the drone has a fault or not
        if (context.getAssignedEvent().getFaultType() == FaultType.STUCK) { //
            context.setDroneState(new StuckState()); // Goes to stuck state
        } else if (context.getAssignedEvent().getFaultType() == FaultType.CORRUPT) {
            context.setDroneState(new CorruptState()); // Goes to corrupt state

        } else if (context.getAssignedEvent().getFaultType() == FaultType.JAMMED) {
            context.setDroneState(new JammedState()); // Goes to jammed state
        } else {
            context.setDroneState(new CruisingState()); // Moves on to the next state normally
        }
    }
            //    @Override
//    public void handle(Drone context) {
//        //context.checkIfTaskSwitch();    //check for change in task
//        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent()); // Calculates the travel time of the zone
//        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime)); // Adds the local time
//        context.sleepFor(context.ACCELERATION_TIME);
//        System.out.println("["+context.getName() + "] ASCENDING AT TIME: " + context.getLocalTime());
//        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASCENDING, context.getCurrentEvent(), context.getName());
//
//        // Handle if the drone has a fault or not
//        if (context.getCurrentEvent().getFaultType() == FaultType.STUCK) { //
//            context.setDroneState(new StuckState()); // Goes to stuck state
//        }
//        else if (context.getCurrentEvent().getFaultType() == FaultType.CORRUPT) {
//            context.setDroneState(new CorruptState()); // Goes to corrupt state
//        }
//        else if (context.getCurrentEvent().getFaultType() == FaultType.JAMMED) {
//            context.setDroneState(new JammedState()); // Goes to jammed state
//        }
//        else {
//            context.setDroneState(new CruisingState()); // Moves on to the next state normally
//        }
//    }
}

/**
 * State where the drone has not arrived on time to the zone meaning that drone got stuck mid-flight.
 */

class StuckState extends InFieldState {
    /**
     * Handles the scenario where the drone is stuck mid-flight and makes it offline/unavailable.
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context){
        try {
            System.out.println("["+context.getName() + "] GOT STUCK MID-FLIGHT AND IS GOING OFFLINE."); // Prints a message that the drone got stuck and will be set to offline
            Thread.sleep(2000);
            System.out.println("[" + context.getName() + "] NOW OFFLINE."); // Makes the drone offline
            byte[] data = context.serializeEvent(context.getAssignedEvent()); // Serializes the event
            // Sends the event back to be rescheduled
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 6001);

            context.getSendReceiveSocket().send(packet);
            context.setAssignedEvent(null); // Sets the assigned event as null

            Thread.sleep(900000000); // Thread goes offline similar by sleeping for a long time
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
//    public void handle(Drone context) {
//        System.out.println("["+context.getName() + "] GOT STUCK MID-FLIGHT AND IS GOING OFFLINE."); // Prints a message that the drone got stuck and will be set to offline
//        context.setHandledEvent(context.getCurrentEvent()); // Sets the handled event as the current event
//        context.setCurrentEvent(null); // Sets the current event as null
//        context.setAssignedEvent(null); // Sets the assigned event as null
//        context.waitForTask(); // Calls the wait for tasks to be removed as a drone thread
//    }
}

/**
 * State where the drone nozzle gets jammed.
 */
class JammedState extends InFieldState {
    @Override
    public void handle(Drone context){
        try {
            System.out.println("["+context.getName() + "] NOZZLE IS JAMMED."); // Prints a message that the Nozzle got stuck
            Thread.sleep(2000);
            System.out.println("[" + context.getName() + "] NOW OFFLINE."); // Makes the drone offline
            MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.OFFLINE, context.getAssignedEvent(), context.getName());
            byte[] data = context.serializeEvent(context.getAssignedEvent()); // Serializes the event
            // Resends the packet to be sent back and rescheduled
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 6001);

            context.getSendReceiveSocket().send(packet);

            context.setAssignedEvent(null); // Sets the assigned event as null

            Thread.sleep(900000000); // Thread goes offline similar by sleeping for a long time
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//    public void handle(Drone context) {
//        System.out.println("["+context.getName() + "] NOZZLE IS JAMMED."); // Prints a message that the Nozzle got stuck
//        context.setHandledEvent(context.getCurrentEvent());  // Sets the handled event as the current event
//        context.setCurrentEvent(null); // Sets the current event as null
//        context.setAssignedEvent(null); // Sets the assigned event as null
//        context.waitForTask(); // Calls the wait for tasks to be removed as a drone thread
//    }
}

/**
 * State where the drone received a corrupted message.
 */
class CorruptState extends InFieldState {
    @Override
    public void handle(Drone context) {
        try {
            System.out.println("[" + context.getName() + "] MESSAGE RECEIVED IS CORRUPTED.");
            System.out.println("[" + context.getName() + "] RESTARTING DRONE...");
            Thread.sleep(10000);
            System.out.println("[" + context.getName() + "] DRONE RESTARTED.");

            context.getAssignedEvent().setHandlingDrone(context.getName());
            // Clear fault and transition to AvailableState
            context.getAssignedEvent().setFaultType(null); // Clear the fault
            context.setDroneState(new AvailableState());

            // Send updated event back to DSS
            byte[] data = context.serializeEvent(context.getAssignedEvent());
            // DSS's drone socket
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 6001);
            context.getSendReceiveSocket().send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void handle(Drone context) {
//        try {
//            System.out.println("["+context.getName() + "] MESSAGE RECEIVED IS CORRUPTED."); // Prints a message that the packet that was received got corrupted
//            context.setHandledEvent(context.getCurrentEvent());  // Sets the handled event as the current event
//            context.setCurrentEvent(null); // Sets the current event as null
//            context.setAssignedEvent(null); // Sets the assigned event as null
//            System.out.println("["+context.getName() + "] RESTARTING DRONE...");
//            Thread.sleep(10000); // Sleeps for 10 second to simulate that its being restarted
//            System.out.println("["+context.getName() + "] DRONE RESTARTED.");
//            context.setDroneState(new AvailableState()); // Makes the drone available again
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}



/**
 * State where the drone is cruising toward the event zone.
 */
class CruisingState extends InFieldState {
    /**
     * Handles the cruising state by adjusting the drone's position over time.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context) {
        double travelZoneTime = context.calculateZoneTravelTime(context.getAssignedEvent());
        int currentTime = 0;

        while (currentTime < travelZoneTime) {
            // Check for task switch every iteration
//            if (context.checkIfTaskSwitch()) {
//                travelZoneTime = context.calculateZoneTravelTime(context.getAssignedEvent());
//            }

            // Update location and sleep
            if (travelZoneTime - currentTime < 1) {
                double timeLeft = travelZoneTime - currentTime;
                context.updateLocation(timeLeft);
                context.sleepFor(timeLeft);
            } else {
                context.updateLocation(1);
                context.sleepFor(1);
            }

            currentTime += 1;
        }

        System.out.println("[" + context.getName() + "] CRUISING TO ZONE: " + context.getAssignedEvent().getZoneId() + " AT TIME: " + context.getLocalTime());
        context.setDroneState(new DropAgentState());
    }
}


/**
 * State where the drone drops water to handle the event.
 */
class DropAgentState extends InFieldState {
    /**
     * Handles the drop agent state by dropping water at the event zone.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context){

        InputEvent event = context.getAssignedEvent();
        int dronesWaterLevel = (int) context.getWaterLevel();

        int remainingAgentNeeded = event.getRemainingAgentNeeded() - dronesWaterLevel;    //calculate the amount of agent still needed
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.FIRE_EXTINGUISHED, context.getAssignedEvent(), context.getName());
        event.setRemainingAgentNeeded(remainingAgentNeeded);

        context.setWaterLevel(context.getWaterLevel() - remainingAgentNeeded);
        context.sleepFor(context.DROP_WATER_TIME);
        context.drainBattery(context.DROP_WATER_TIME);
        context.sleepFor(context.DECELERATION_TIME); // Simulates the deceleration time
        int remainingAgent = event.getRemainingAgentNeeded();
        if (remainingAgent < 0){
            remainingAgent = 0;
        }
        System.out.println("["+context.getName() + "] DROPPED " + dronesWaterLevel + "L " + "(Remaining: " + remainingAgent + "L)");
        context.setDropCompleted(true);

        try{
            byte[] data = context.serializeEvent(context.getAssignedEvent()); // Serializes the event
            // Sends the data as a confirmation that it was correctly completed
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 6001);
            context.getSendReceiveSocket().send(packet); // Snd it to the drone subsystems

        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("["+context.getName() + "]: RETURNING TO BASE: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the watter was dropped and that it's returning to base
        context.setDroneState(new ReturningToBaseState());

    }
//    @Override
//    public void handle(Drone context) {
//        InputEvent event = context.getCurrentEvent();
//        int waterAvailable = (int) context.getWaterLevel();
//        int remainingAgentNeeded = event.getRemainingAgentNeeded() - waterAvailable;    //calculate the amount of agent still needed
//        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.FIRE_EXTINGUISHED, context.getCurrentEvent(), context.getName());
//        event.setRemainingAgentNeeded(remainingAgentNeeded);
//        context.setWaterLevel(context.getWaterLevel() - remainingAgentNeeded);
//        context.sleepFor(context.DROP_WATER_TIME);
//        context.drainBattery(context.DROP_WATER_TIME);
//        context.sleepFor(context.DECELERATION_TIME); // Simulates the deceleration time
//        int remainingAgent = event.getRemainingAgentNeeded();
//        if (remainingAgent < 0){
//            remainingAgent = 0;
//        }
//        System.out.println("["+context.getName() + "] DROPPED " + waterAvailable + "L " + "(Remaining: " + remainingAgent + "L)");
//        System.out.println("["+context.getName() + "]: RETURNING TO BASE: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the watter was dropped and that it's returning to base
//        context.setDropCompleted(true);
//        context.setDroneState(new ReturningToBaseState());
//    }
}

/**
 * State where the drone returns to the base after handling an event.
 */
class ReturningToBaseState extends InFieldState {
    @Override
    public void handle(Drone context) {
        // Calculate time to return to base (0,0) from current position
        double travelTime = context.calculateReturnTravelTime();
        int currentTime = 0;

        while (currentTime < travelTime) {

            // Recalculate in case position changed mid-flight
            travelTime = context.calculateReturnTravelTime();
            if (currentTime >= travelTime) break;

            double timeIncrement = (travelTime - currentTime) < 1 ?
                    (travelTime - currentTime) : 1;

            // Update position towards base
            context.updateReturnLocation(timeIncrement);
            context.sleepFor(timeIncrement);
            context.drainBattery(timeIncrement);

            currentTime += timeIncrement;
            context.setLocalTime(context.getLocalTime().plusSeconds((long) timeIncrement));
        }

        // Ensure final position is exactly at base
        context.setCurrentCoordinates(new Coordinate(0, 0));
        System.out.println("[" + context.getName() + "] ARRIVED BACK AT BASE AT TIME: " + context.getLocalTime());

        // Reset event state and transition to refill
        context.setAssignedEvent(null);
        context.drainBattery(travelTime);
        context.setDroneState(new RefillState());
    }
}

/**
 * State where the drone refills water at the base.
 */
class RefillState extends InFieldState {
    /**
     * Handles the refill state by refilling the drone's water tank.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context) {
        System.out.println("["+context.getName() + "] REFILLING WATER...");
        context.sleepFor(2); // 2 second refill delay
        context.setLocalTime(context.getLocalTime().plusSeconds(2));
        // Refill water capacity
        context.refillWater();
        System.out.println("["+context.getName() + "] WATER REFILLED. AVAILABLE AT TIME: " + context.getLocalTime());
        if (context.getBatteryLevel() < context.MAX_BATTERY_CAPACITY * 0.8){
            context.setDroneState(new BatteryRechargingState());
        }
        else context.setDroneState(new AvailableState());
    }
}

/**
 * State where the drone is recharging its battery.
 */
class BatteryRechargingState extends InFieldState {
    /**
     * Handles the battery recharging state by simulating the recharging process.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context) {
        System.out.println(context.getName() + ": BATTERY RECHARGING...");
        context.sleepFor(2); // 2-second recharge delay
        context.setLocalTime(context.getLocalTime().plusSeconds(2));
        context.chargeBattery();
        context.setDroneState(new AvailableState());
    }
}

