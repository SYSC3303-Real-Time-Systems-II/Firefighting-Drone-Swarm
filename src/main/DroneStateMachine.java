interface DroneBaseState{
    void hande(DroneBaseState context);
    void addNewTask(InputEvent event);
}

class InBaseState implements DroneBaseState {

    @Override
    public void hande(DroneBaseState context) {

    }

    @Override
    public void addNewTask(InputEvent event) {

    }


}

class InFieldState implements DroneBaseState{

    @Override
    public void hande(DroneBaseState context) {

    }

    @Override
    public void addNewTask(InputEvent event) {

    }
}

class AvailableState extends InBaseState {

}

class AscendingState extends InBaseState {

}

class CruisingState extends InFieldState {

}

class ReCalculatingState extends InFieldState {

}

class DropAgentState extends InFieldState {

}