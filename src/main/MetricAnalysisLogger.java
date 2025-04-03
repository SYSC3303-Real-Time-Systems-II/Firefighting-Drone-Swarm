import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * A utility class for tracking, logging, and analyzing metrics related to drone fire response operations.
 * This class monitors various events in the drone fire response system, such as drone assignments,
 * fire extinguishing time, and calculates performance metrics like response time, throughput, and utilization.
 */
class MetricAnalysisLogger {

    /**
     * Enum representing different states a drone or event can be in.
     */
    public enum EventStatus {
        /** System is starting */
        STARTING,
        /** Drone is waiting for a task */
        WAITING_FOR_TASK,
        /** Drone is ascending to location */
        ASCENDING,
        /** System has received a new event */
        RECEIVED_EVENT,
        /** Event has been assigned to a drone */
        ASSIGNED_EVENT,
        /** Fire has been extinguished */
        FIRE_EXTINGUISHED,
        /** Drone is offline */
        OFFLINE
    }

    /**
     * Class to track drone utilization data, including current time and active operation time.
     */
    public static class DroneUtilizationData {
        private LocalTime currentTime;
        private double activeTime;

        /**
         * Constructs a new drone utilization data object.
         *
         * @param currentTime The current time when this data is created
         */
        public DroneUtilizationData(LocalTime currentTime) {
            this.currentTime = currentTime;
            this.activeTime = 0.0;
        }

        /**
         * Sets the current time for this utilization data.
         *
         * @param currentTime The time to set
         */
        public void setCurrentTime(LocalTime currentTime) {
            this.currentTime = currentTime;
        }

        /**
         * Gets the current time for this utilization data.
         *
         * @return The current time
         */
        public LocalTime getCurrentTime() {
            return currentTime;
        }

        /**
         * Gets the total active time for this drone.
         *
         * @return The active time in milliseconds
         */
        public double getActiveTime() {
            return activeTime;
        }

        /**
         * Adds duration to the drone's active time.
         *
         * @param duration Duration to add in milliseconds
         */
        public void addActiveTime(double duration) {
            this.activeTime += duration;
        }
    }

    /** Total system operational time in milliseconds */
    private static double totalTime = 0;
    /** System start time */
    private static LocalTime startingTime;

    /** Number of drone responses counted */
    private static int amountOfDroneResponses = 0;
    /** Total response time for all drones in milliseconds */
    private static double totalDroneResponseTime = 0;

    /** Number of fires extinguished */
    private static int amountOfFireExtinguished = 0;
    /** Total time to extinguish all fires in seconds */
    private static double totalFireExtinguishedResponseTime = 0;

    /** Average drone response time in milliseconds */
    public static double droneResponseTime;
    /** Average fire extinguishing response time in seconds */
    public static double fireExtinguishedResponseTime;
    /** System throughput (fires extinguished per minute) */
    public static double throughput;

    /** Maps drone names to their event assignment times */
    private static final Map<String, LocalTime> droneAssignedEvents = new HashMap<>();
    /** Maps event IDs to their received times */
    private static final Map<Integer, LocalTime> eventsInProgress = new HashMap<>();
    /** Maps drone names to their utilization data */
    private static final Map<String, DroneUtilizationData> droneInWaitingList = new HashMap<>();
    /** Maps drone names to their utilization percentages */
    private static final Map<String, Double> dronesUtilization = new HashMap<>();

    /**
     * Logs an event with the specified state, event details, and drone name.
     * Updates various metrics based on the event state.
     *
     * @param state The current state of the event
     * @param currentEvent The event details (can be null for certain states)
     * @param droneName The name of the drone involved (can be null for certain states)
     */
    public synchronized static void logEvent(EventStatus state, InputEvent currentEvent, String droneName) {
        LocalTime currentTime = LocalTime.now();

        if (state == EventStatus.STARTING) {
            startingTime = currentTime;
            return;
        }

        addTotalTime(currentTime);
        recalculateUtilization();

        switch (state) {
            case WAITING_FOR_TASK -> addDroneWaitingList(droneName, currentTime);
            case ASCENDING -> calculateDroneResponseTime(droneName, currentTime);
            case RECEIVED_EVENT -> eventsInProgress.put(currentEvent.getEventID(), currentTime);
            case ASSIGNED_EVENT -> {
                calculateUtilization(droneName, currentTime);
                droneAssignedEvents.put(droneName, currentTime);
            }
            case FIRE_EXTINGUISHED -> {
                System.out.println("Fire extinguished: " + Duration.between(startingTime, currentTime));
                fireExtinguishedResponseTime(currentEvent.getEventID(), currentTime);
                calculateThroughPut();
            }
        }
    }

    /**
     * Recalculates the utilization percentage for all drones in the waiting list.
     */
    private static void recalculateUtilization() {
        if (totalTime <= 0)
            return;

        for (String droneName : droneInWaitingList.keySet()) {
            double utilization = droneInWaitingList.get(droneName).getActiveTime() / totalTime * 100;
            dronesUtilization.put(droneName, utilization);
        }
    }

    /**
     * Adds a drone to the waiting list or updates its current time if it's already in the list.
     *
     * @param droneName The name of the drone to add or update
     * @param currentTime The current time
     */
    private static void addDroneWaitingList(String droneName, LocalTime currentTime) {
        if (droneInWaitingList.get(droneName) != null) {
            droneInWaitingList.get(droneName).setCurrentTime(currentTime);
        } else {
            droneInWaitingList.put(droneName, new DroneUtilizationData(currentTime));
        }

        dronesUtilization.computeIfAbsent(droneName, k -> (double) 0);
    }

    /**
     * Calculates the response time for a drone from assignment to ascending.
     *
     * @param droneName The name of the drone
     * @param endTime The time when the drone started ascending
     */
    private static void calculateDroneResponseTime(String droneName, LocalTime endTime) {
        LocalTime responseTime = droneAssignedEvents.get(droneName);

        if (responseTime == null)
            return;

        amountOfDroneResponses += 1;

        double duration = Duration.between(responseTime, endTime).toMillis();
        totalDroneResponseTime += duration;
        droneResponseTime = totalDroneResponseTime / amountOfDroneResponses;
        droneAssignedEvents.put(droneName, null);
    }

    /**
     * Calculates the response time from event receipt to fire extinguishing.
     *
     * @param eventID The ID of the event
     * @param endTime The time when the fire was extinguished
     */
    private static void fireExtinguishedResponseTime(int eventID, LocalTime endTime) {
        LocalTime responseTime = eventsInProgress.get(eventID);

        if (responseTime == null)
            return;

        amountOfFireExtinguished += 1;

        double duration = Duration.between(responseTime, endTime).toSeconds();
        totalFireExtinguishedResponseTime += duration;
        fireExtinguishedResponseTime = totalFireExtinguishedResponseTime / amountOfFireExtinguished;

        eventsInProgress.remove(eventID);
    }

    /**
     * Calculates the system throughput (fires extinguished per minute).
     */
    private static void calculateThroughPut() {
        throughput = amountOfFireExtinguished / (totalTime / 60000.0); // Convert ms to minutes
    }

    /**
     * Calculates the utilization percentage for a drone.
     *
     * @param droneName The name of the drone
     * @param endTime The current time for calculation
     */
    private static void calculateUtilization(String droneName, LocalTime endTime) {
        LocalTime utilStartingTime = droneInWaitingList.get(droneName).getCurrentTime();

        if (utilStartingTime == null)
            return;

        if (dronesUtilization.get(droneName) == null)
            return;

        double duration = Duration.between(utilStartingTime, endTime).toMillis();
        droneInWaitingList.get(droneName).addActiveTime(duration);
        double utilization = droneInWaitingList.get(droneName).getActiveTime() / totalTime * 100;
        dronesUtilization.put(droneName, utilization);

        droneInWaitingList.get(droneName).setCurrentTime(null);
    }

    /**
     * Updates the total system operational time.
     *
     * @param currentTime The current time
     */
    private static void addTotalTime(LocalTime currentTime) {
        double duration = Duration.between(startingTime, currentTime).toMillis();
        totalTime += duration;
    }

    /**
     * Gets the average drone response time in milliseconds.
     *
     * @return The average drone response time
     */
    public static double getDroneResponseTime() {
        return droneResponseTime;
    }

    /**
     * Gets the average fire extinguishing response time in seconds.
     *
     * @return The average fire extinguishing response time
     */
    public static double getFireExtinguishedResponseTime() {
        return fireExtinguishedResponseTime;
    }

    /**
     * Gets the system throughput (fires extinguished per minute).
     *
     * @return The system throughput
     */
    public static double getThroughput() {
        return throughput;
    }

    /**
     * Gets a map of drone utilization percentages.
     *
     * @return Map of drone names to their utilization percentages
     */
    public static Map<String, Double> getDronesUtilization() {
        return dronesUtilization;
    }
}