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
        // Lock and wait until a task is assigned
        context.waitForTask();
        context.setLocalTime(context.getCurrentEvent().getTime());
        System.out.println("["+context.getName() + "] GOT EVENT: " + context.getCurrentEvent().getEventID() + " AT TIME: " + context.getLocalTime());
        context.setLocalTime(context.getLocalTime().plusNanos((long) (context.ACCELERATION_TIME * 1000000000)));  // Adds the local time
        context.sleepFor(context.ACCELERATION_TIME); // Simulates the acceleration time
        context.setDroneState(new AscendingState()); // The drone becomes on route to the fire zone
    }
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
        context.checkIfTaskSwitch();    //check for change in task
        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent()); // Calculates the travel time of the zone
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime)); // Adds the local time
        context.sleepFor(context.ACCELERATION_TIME);
        System.out.println("["+context.getName() + "] ASCENDING AT TIME: " + context.getLocalTime());

        // Handle if the drone has a fault or not
        if (context.getCurrentEvent().getFaultType() == FaultType.STUCK) { //
            context.setDroneState(new StuckState()); // Goes to stuck state
        }
        else if (context.getCurrentEvent().getFaultType() == FaultType.CORRUPT) {
            context.setDroneState(new CorruptState()); // Goes to corrupt state
        }
        else if (context.getCurrentEvent().getFaultType() == FaultType.JAMMED) {
            context.setDroneState(new JammedState()); // Goes to jammed state
        }
        else {
            context.setDroneState(new CruisingState()); // Moves on to the next state normally
        }
    }
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
    public void handle(Drone context) {
        System.out.println("["+context.getName() + "] GOT STUCK MID-FLIGHT AND IS GOING OFFLINE."); // Prints a message that the drone got stuck and will be set to offline
        context.setHandledEvent(context.getCurrentEvent()); // Sets the handled event as the current event
        context.setCurrentEvent(null); // Sets the current event as null
        context.setAssignedEvent(null); // Sets the assigned event as null
        context.waitForTask(); // Calls the wait for tasks to be removed as a drone thread
    }
}

/**
 * State where the drone nozzle gets jammed.
 */
class JammedState extends InFieldState {
    @Override
    public void handle(Drone context) {
        System.out.println("["+context.getName() + "] NOZZLE IS JAMMED."); // Prints a message that the Nozzle got stuck
        context.setHandledEvent(context.getCurrentEvent());  // Sets the handled event as the current event
        context.setCurrentEvent(null); // Sets the current event as null
        context.setAssignedEvent(null); // Sets the assigned event as null
        context.waitForTask(); // Calls the wait for tasks to be removed as a drone thread
    }
}

/**
 * State where the drone received a corrupted message.
 */
class CorruptState extends InFieldState {
    @Override
    public void handle(Drone context) {
        System.out.println("["+context.getName() + "] MESSAGE RECEIVED IS CORRUPTED."); // Prints a message that the packet that was received got corrupted
        context.setHandledEvent(context.getCurrentEvent());  // Sets the handled event as the current event
        context.setCurrentEvent(null); // Sets the current event as null
        context.setAssignedEvent(null); // Sets the assigned event as null
        context.setDroneState(new AvailableState()); // Makes the drone available again
    }
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
        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent());
        int currentTime = 0;

        while (currentTime < travelZoneTime) {
            // Check for task switch every iteration
            context.checkIfTaskSwitch();

            // Recalculate travel time if the event changed
            travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent());
            if (currentTime >= travelZoneTime) break;

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

        System.out.println("[" + context.getName() + "] CRUISING TO ZONE: "
                + context.getCurrentEvent().getZoneId() + " AT TIME: " + context.getLocalTime());
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
    public void handle(Drone context) {
        int waterNeeded = context.getCurrentEvent().getSeverity().getValue();
        double currentCapacity = context.getWaterLevel();
        if (currentCapacity >= waterNeeded) {
            System.out.println("["+context.getName() + "]: DROPPING WATER (" + waterNeeded + " L) at time: " + context.getLocalTime());
            context.setWaterLevel(currentCapacity - waterNeeded);
            context.setLocalTime(context.getLocalTime().plusSeconds((long) context.DROP_WATER_TIME));
            context.drainBattery(context.DROP_WATER_TIME);
        }
        else {
            System.out.println("["+context.getName() + "]: NOT ENOUGH WATER to handle severity ("
                    + context.getCurrentEvent().getSeverity() + ")!");
            context.setLocalTime(context.getLocalTime().plusNanos((long) (context.DECELERATION_TIME * 1000000000))); // Adds the local time
        }
        System.out.println("["+context.getName() + "]: RETURNING TO BASE: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the watter was dropped and that it's returning to base
        // sleepFor(DECELERATION_TIME); // Simulates the deceleration time
        context.setDroneState(new ReturningToBaseState());
    }
}

/**
 * State where the drone returns to the base after handling an event.
 */
class ReturningToBaseState extends InFieldState {
    /**
     * Handles the returning to base state by simulating the travel back.
     *
     * @param context The current drone instance.
     */
    @Override
    public void handle(Drone context){
        double travelZoneTime2 = context.calculateZoneTravelTime(context.getCurrentEvent());
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime2 - 4));
        context.sleepFor(travelZoneTime2); // Simulates the travel zone time
        System.out.println("["+context.getName() + "] ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the drone has arrived back and is now ready for the next event
        context.setHandledEvent(context.getCurrentEvent());
        context.setChangedEvent(false);
        context.setCurrentEvent(null);
        context.setAssignedEvent(null);
        context.drainBattery(travelZoneTime2);
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
        System.out.println(context.getName() + ": REFILLING WATER...");
        context.sleepFor(2); // 2 second refill delay
        context.setLocalTime(context.getLocalTime().plusSeconds(2));
        // Refill water capacity
        context.refillWater();
        System.out.println(context.getName() + ": WATER REFILLED. AVAILABLE AT TIME: " + context.getLocalTime());
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

