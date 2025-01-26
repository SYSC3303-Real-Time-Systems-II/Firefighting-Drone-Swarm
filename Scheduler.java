public class Scheduler implements Runnable {

    public String name;
    public Scheduler(String name) {
        this.name = name;
    }

    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println("["+name +"] - "+ i);
        }
    }
}
