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
                    System.out.println(context.getName() + ": WAITING FOR EVENT");
                    context.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            context.setCurrentEvent(context.getAssignedEvent());
        }
        System.out.println(context.getName() + " GOT EVENT" + context.getCurrentEvent() + ": AT TIME: " + context.getLocalTime());
        context.setLocalTime(context.getCurrentEvent().getTime());
       // System.out.println(context.getName() + ": TRAVELING TO ZONE: " + context.getCurrentEvent().getZoneId() + " : AT TIME: " + context.getLocalTime()); // Prints out a message that the drone is on its way to the zone and the time it traveled
        context.setLocalTime(context.getLocalTime().plusSeconds((long) context.getACCELERATION_TIME()));  // Adds the local time
        context.sleepFor(context.getACCELERATION_TIME()); // Simulates the acceleration time
        context.setDroneState(new AscendingState()); // The drone becomes on route to the fire zone
    }
}

class AscendingState extends InBaseState {
    @Override
    public void handle(Drone context) {
        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent());
        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime)); // Adds the local time
        context.sleepFor(context.getACCELERATION_TIME());
        System.out.println(context.getName() + ": ASCENDING AT TIME: " + context.getLocalTime());
        context.setDroneState(new CruisingState());
    }
}

class CruisingState extends InFieldState {
    @Override
    public void handle(Drone context) {
        double travelZoneTime = context.calculateZoneTravelTime(context.getCurrentEvent());
        context.sleepFor(travelZoneTime);
        System.out.println(context.getName() + ": CRUISING TO ZONE: " + context.getCurrentEvent().getZoneId() +  " : AT TIME: " + context.getLocalTime());
        context.setDroneState(new DropAgentState());
    }
}

class ReCalculatingState extends InFieldState {
    @Override
    public void handle(Drone context) {

    }
}

class DropAgentState extends InFieldState {
    @Override
    public void handle(Drone context) {
        System.out.println(context.getName() + ": WATER DROPPED, RETURNING TO BASE: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the watter was dropped and that it's returning to base
        context.setLocalTime(context.getLocalTime().plusSeconds((long) context.getDECELERATION_TIME())); // Adds the local time
        context.sleepFor(context.getDECELERATION_TIME()); // Simulates the deceleration time
        context.setDroneState(new ReturningToBaseState()); // The drone is returning to base now
    }
}

class ReturningToBaseState extends InFieldState {
    @Override
    public void handle(Drone context){
        double travelZoneTime2 = context.calculateZoneTravelTime(context.getCurrentEvent());

        context.setLocalTime(context.getLocalTime().plusSeconds((long) travelZoneTime2 - 4));
        context.sleepFor(travelZoneTime2 - 4); // Simulates the travel zone time
        System.out.println(context.getName() + ": ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: " + context.getLocalTime()); // Prints out a message saying that the drone has arrived back and is now ready for the next event
        context.setCompletedEvent(context.getCurrentEvent());
        context.setCurrentEvent(null);
        context.setAssignedEvent(null);
        context.setDroneState(new AvailableState());
    }
}