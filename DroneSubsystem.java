public class DroneSubsystem implements Runnable{

    public String name;

    public DroneSubsystem(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println("["+name +"] - "+ i);
        }
    }
}
