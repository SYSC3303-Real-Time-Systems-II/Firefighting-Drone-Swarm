import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

class MetricAnalysisLogger {

    public enum EventStatus {
        STARTING,
        WAITING_FOR_TASK,
        ASCENDING,
        RECEIVED_EVENT,
        ASSIGNED_EVENT,
        FIRE_EXSTINGUISHED,
        OFFLINE,
    }

    private static double totalTime = 0;

    private static double activeTimeUtil = 0;
    private static LocalTime startingTime;

    private static int amountOfDroneResponses = 0;
    private static double totalDroneResponseTime = 0;

    private static int amountOfFireExstinguished = 0;
    private static double totalFireExstinguishedResponseTime = 0;


    public static double droneResponseTime;
    public static double fireExstinguishedResponseTime;
    public static double throughPut;
    public static double utilization;


    private static final Map<String, LocalTime> droneAssignedEvents = new HashMap<>();
    private static final Map<String, LocalTime> droneInWaitingList = new HashMap<>();
    private static final Map<Integer, LocalTime> eventsInProgress = new HashMap<>();




    public synchronized static void logEvent(EventStatus state, InputEvent currentEvent, String droneName){
        LocalTime currentTime = LocalTime.now();

        if(state == EventStatus.STARTING){
            startingTime = currentTime;
            return;
        }

        addTotalTime(currentTime);
        recalculateUtilization();

        switch (state) {
            case WAITING_FOR_TASK -> droneInWaitingList.put(droneName, currentTime);
            case ASCENDING -> calculateDroneResponseTime(droneName, currentTime);
            case RECEIVED_EVENT -> eventsInProgress.put(currentEvent.getEventID(), currentTime);
            case ASSIGNED_EVENT -> {
                calculateUtilization(droneName, currentTime);
                droneAssignedEvents.put(droneName, currentTime);
            }
            case FIRE_EXSTINGUISHED -> {
                System.out.println("Fire exstinguished: " + Duration.between(startingTime, currentTime));
                fireExstinguishedResponseTime(currentEvent.getEventID(), currentTime);
                calculateThroughPut();
            }
        }

        System.out.println("METRICS----------------------------");
        System.out.println(droneResponseTime);
        System.out.println(fireExstinguishedResponseTime);
        System.out.println(throughPut);
        System.out.println(utilization);
        System.out.println("-----------------------------------");
    }

    private static void recalculateUtilization(){
        if (totalTime <= 0)
            return;
        utilization = activeTimeUtil / totalTime * 100;
    }

    private static void calculateDroneResponseTime(String droneName, LocalTime endTime){
        LocalTime responseTime = droneAssignedEvents.get(droneName);

        if (responseTime == null)
            return;

        // Increment of drone responses calculated
        amountOfDroneResponses += 1;

        double duration = Duration.between(responseTime, endTime).toMillis();
        totalDroneResponseTime +=  duration;
        droneResponseTime = totalDroneResponseTime / amountOfDroneResponses;
        droneAssignedEvents.put(droneName, null);
    }

    private static void fireExstinguishedResponseTime(int eventID, LocalTime endTime){
        LocalTime responseTime = eventsInProgress.get(eventID);

        if (responseTime == null)
            return;

        // Increment of drone responses calculated
        amountOfFireExstinguished += 1;

        // Perform Response time calculations
        double duration = Duration.between(responseTime, endTime).toSeconds();
        totalFireExstinguishedResponseTime += duration;
        fireExstinguishedResponseTime = totalFireExstinguishedResponseTime / amountOfFireExstinguished;

        // Removes event from list of active events
        eventsInProgress.remove(eventID);
    }

    private static void calculateThroughPut(){
        throughPut = amountOfFireExstinguished / totalTime ;
    }


    private static void calculateUtilization(String droneName, LocalTime endTime){
        LocalTime utilStartingTime = droneInWaitingList.get(droneName);

        if (utilStartingTime == null)
            return;

        double duration = Duration.between(utilStartingTime, endTime).toMillis();
        activeTimeUtil += duration;
        utilization = activeTimeUtil / totalTime * 100;
        droneInWaitingList.put(droneName, null);
    }

    private static void addTotalTime(LocalTime currentTime) {
        double duration = Duration.between(startingTime, currentTime).toMillis();

        totalTime += duration;
    }



    public static double getDroneResponseTime() {
        return droneResponseTime;
    }

    public static double getFireExstinguishedResponseTime(){
        return fireExstinguishedResponseTime;
    }

    public static double getThroughPut() {
        return throughPut;
    }

    public static double getUtilization() {
        return utilization;
    }
}
