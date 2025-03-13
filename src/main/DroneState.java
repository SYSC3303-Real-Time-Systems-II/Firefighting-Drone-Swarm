/**
 * This class deals with the states of the drone telling the scheduler which drones are
 * available to be deployed to a fire zone and which drones are currently being used. Available means that the drone can be deployed to a fire zone,
 * on route means it is currently traveling to the zone, arrived means it has arrived to the zone, dropping water means that it is taking out the fire
 * and returning to base means it is going back.
 *
 * @author Rami Ayoub
 * @version 2.0
 */

public enum DroneState {
    AVAILABLE, ASCENDING, CRUISING, ON_ROUTE, ARRIVED, DROPPING_WATER, RETURNING_TO_BASE
}
