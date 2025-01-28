import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    private PriorityQueue<InputEvent> inputEvents;

    public Scheduler(String name) {
        Comparator<InputEvent> priorityComparator = Comparator.comparingInt(inputEvent -> {
            switch (inputEvent.getSeverity()) {
                case High: return 1; // Highest priority
                case Moderate: return 2;
                case Low: return 3; // Lowest priority
                default: throw new IllegalArgumentException("Unknown priority level");
            }
        });

        this.inputEvents = new PriorityQueue<>(priorityComparator);
        this.name = name;
        this.systemType = Systems.Scheduler;

    }

    public synchronized void addInputEvent(InputEvent inputEvent){
        System.out.println("Adding input event here");
        this.inputEvents.add(inputEvent);
    }

    public synchronized InputEvent takeInputEvent(){
        while (this.inputEvents.isEmpty()) {
            try{
                System.out.println("No available input events now waiting...");
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.inputEvents.poll();
    }



    public void run() {

    }
}
