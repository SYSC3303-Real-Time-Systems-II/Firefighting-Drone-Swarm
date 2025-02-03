public class Coordinate {
    private double x;
    private double y;

    /**
     * Constructs a Coordinate object.
     *
     * @param x     x-coordinate of the coordinate
     * @param y     y-coordinate of the coordinate
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate of the coordinate.
     *
     * @return      the x-coordinate as a double
     */
    public double getX() {
        return this.x;
    }

    /**
     * Returns the y coordinate of the coordinate.
     *
     * @return      the y-coordinate as a double
     */
    public double getY() {
        return this.y;
    }

    @Override
    public String toString(){
        return "("+ this.x + ", " + this.y + ")";
    }
}
