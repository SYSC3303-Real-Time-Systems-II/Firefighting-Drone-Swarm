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

    }
}

class AscendingState extends InBaseState {
    @Override
    public void handle(Drone context) {

    }
}

class CruisingState extends InFieldState {
    @Override
    public void handle(Drone context) {

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

    }
}