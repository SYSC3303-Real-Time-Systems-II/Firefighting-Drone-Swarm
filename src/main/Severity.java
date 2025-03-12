/**
 * Represents different levels of severity with associated amount of water/foam needed for each event severity
 */
 public enum Severity {
    High(30), Moderate(20), Low(10);

    private final int value;

    /**
     * Constructs a Severity enum with the specified amount of water/foam.
     *
     * @param value The amount of water/foam associated with the severity level.
     */
    Severity(int value) {
        this.value = value;
    }

    /**
     * Gets the amount of water/foam for the severity level.
     *
     * @return  the amount of water/foam for the severity level.
     */
    public int getValue() {
        return value;
    }
}
