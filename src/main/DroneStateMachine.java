interface DroneStateMachine {
    void handle(Drone context);
}

abstract class InBaseState implements DroneStateMachine {
    @Override
    public abstract void handle(Drone context);
}

abstract class InFieldState implements DroneStateMachine {
    @Override
    public abstract void handle(Drone context);
}

class AvailableState extends InBaseState {
    @Override
    public void handle(Drone context) {
        //lock and wait until a task is assigned
        synchronized (context) {

            while (context.getAssignedEvent() == null) {
                try {
                    System.out.println("["+context.getName() + "] WAITING FOR EVENT");
                    context.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            context.setCurrentEvent(context.getAssignedEvent()); //move the assingedEvent to Current Event
            context.setAssignedEvent(null);

        }
        context.setLocalTime(context.getCurrentEvent().getTime());
        System.out.println("["+context.getName() + "] GOT EVENT: " + context.getCurrentEvent().getEventID() + " AT TIME: " + context.getLocalTime());
        context.setLocalTime(context.getLocalTime().plusSeconds((long) context.getACCELERATION_TIME()));  // Adds the local time
        context.sleepFor(context.getACCELERATION_TIME()); // Simulates the acceleration time
        context.setDroneState(new AscendingState()); // The drone becomes on route to the fire zone
    }
}

class AscendingState extends InBaseState {
    @Override
    public void handle(Drone context) {
        context.checkIfTaskSwitch();    //check for change in task
        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent());
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime)); // Adds the local time
        context.sleepFor(context.getACCELERATION_TIME());
        System.out.println("["+context.getName() + "] ASCENDING AT TIME: " + context.getLocalTime());
        context.setDroneState(new CruisingState());
    }
}

class CruisingState extends InFieldState {

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


class DropAgentState extends InFieldState {
    @Override
    public void handle(Drone context) {
        int waterNeeded = context.getCurrentEvent().getSeverity().getValue();
        double currentCapacity = context.getWaterCapacity();
        if (currentCapacity >= waterNeeded) {
            System.out.println("["+context.getName() + "]: DROPPING WATER (" + waterNeeded + " L) at time: " + context.getLocalTime());
            context.setWaterCapacity(currentCapacity - waterNeeded);
            context.setLocalTime(context.getLocalTime().plusSeconds((long) context.getDROP_WATER_TIME()));
            context.drainBattery(context.getDROP_WATER_TIME());
        }
        else {
            System.out.println("["+context.getName() + "]: NOT ENOUGH WATER to handle severity ("
                    + context.getCurrentEvent().getSeverity() + ")!");
            context.setLocalTime(context.getLocalTime().plusSeconds((long) context.getDECELERATION_TIME())); // Adds the local time
        }
        System.out.println("["+context.getName() + "]: RETURNING TO BASE: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the watter was dropped and that it's returning to base
        // sleepFor(DECELERATION_TIME); // Simulates the deceleration time
        context.setDroneState(new ReturningToBaseState());
    }
}

class ReturningToBaseState extends InFieldState {
    @Override
    public void handle(Drone context){
        double travelZoneTime2 = context.calculateZoneTravelTime(context.getCurrentEvent());
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime2 - 4));
        context.sleepFor(travelZoneTime2); // Simulates the travel zone time
        System.out.println("["+context.getName() + "] ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the drone has arrived back and is now ready for the next event
        context.setCompletedEvent(context.getCurrentEvent());
        context.setChangedEvent(false);
        context.setCurrentEvent(null);
        context.setAssignedEvent(null);
        context.drainBattery(travelZoneTime2);
        context.setDroneState(new RefillState());
    }
}

class RefillState extends InFieldState {
    @Override
    public void handle(Drone context) {
        System.out.println(context.getName() + ": REFILLING WATER...");
        context.sleepFor(2); // 2 second refill delay
        context.setLocalTime(context.getLocalTime().plusSeconds(2));
        // Refill water capacity
        context.refillWater();
        System.out.println(context.getName() + ": WATER REFILLED. AVAILABLE AT TIME: " + context.getLocalTime());
        if (context.getBatteryCapacity() < context.getMAX_BATTERY_CAPACITY() * 0.8){
            context.setDroneState(new BatteryRechargingState());
        }
        else context.setDroneState(new AvailableState());
    }
}

class BatteryRechargingState extends InFieldState {
    @Override
    public void handle(Drone context) {
        System.out.println(context.getName() + ": BATTERY RECHARGING...");
        context.sleepFor(2); // 2-second recharge delay
        context.setLocalTime(context.getLocalTime().plusSeconds(2));
        context.setBatteryCapacity(context.getMAX_BATTERY_CAPACITY());
        context.setDroneState(new AvailableState());
    }
}

