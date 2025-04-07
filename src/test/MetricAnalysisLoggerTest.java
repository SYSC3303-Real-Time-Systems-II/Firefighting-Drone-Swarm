import org.junit.jupiter.api.*;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetricAnalysisLoggerTest {
    @BeforeEach
    void resetMetrics() throws Exception {
        // Reset all static fields via reflection before each test
        var clazz = MetricAnalysisLogger.class;

        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            field.setAccessible(true);
            if (field.getType() == double.class) {
                field.set(null, 0.0);
            } else if (field.getType() == int.class) {
                field.set(null, 0);
            } else if (field.getType() == LocalTime.class) {
                field.set(null, null);
            } else if (Map.class.isAssignableFrom(field.getType())) {
                ((Map<?, ?>) field.get(null)).clear();
            }
        }
    }

    @Test
    void testDroneResponseAndUtilization() throws InterruptedException {
        String droneName = "DroneA";
        InputEvent event = new InputEvent("11:30:00", 3, "FIRE_DETECTED", "High", Status.UNRESOLVED, null);

        // STARTING
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.STARTING, null, null);
        LocalTime start = LocalTime.now();

        // WAITING_FOR_TASK
        Thread.sleep(1000);
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.WAITING_FOR_TASK, null, droneName);

        // RECEIVED_EVENT
        Thread.sleep(1000);
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.RECEIVED_EVENT, event, null);

        // ASSIGNED_EVENT
        Thread.sleep(1000);
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASSIGNED_EVENT, event, droneName);

        // ASCENDING (simulate response)
        Thread.sleep(1000);
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASCENDING, event, droneName);

        // FIRE_EXTINGUISHED
        Thread.sleep(1000);
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.FIRE_EXTINGUISHED, event, droneName);

        // Assertions
        assertTrue(MetricAnalysisLogger.getDroneResponseTime() > 0);
        assertTrue(MetricAnalysisLogger.getFireExtinguishedResponseTime() > 0);
        assertTrue(MetricAnalysisLogger.getThroughput() > 0);

        Map<String, Double> utilization = MetricAnalysisLogger.getDronesUtilization();
        assertTrue(utilization.containsKey(droneName));
        assertTrue(utilization.get(droneName) > 0);
    }
}
