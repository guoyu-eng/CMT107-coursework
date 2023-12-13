package Objects;

// Corner： Used to represent corner points in images, and provides functions for comparison and string representation.
public class Corner implements Comparable<Corner> {
    public int x;
    public int y;
    public float strength;

    public Corner(int x, int y, float strength) {
        this.x = x;
        this.y = y;
        this.strength = strength;
    }

    @Override
    public int compareTo(Corner other) {
        //  strength Sort in descending order
        return Float.compare(other.strength, this.strength);
    }
    // the point with the value
    @Override
    public String toString() {
        return "(" + x + ", " + y + ") - Strength: " + strength;
    }
}
