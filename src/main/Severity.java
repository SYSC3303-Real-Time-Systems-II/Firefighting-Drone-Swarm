public enum Severity {
    High(30), Moderate(20), Low(10);

    private final int value;

    Severity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
