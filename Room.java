import java.util.*;

public class Room {
    public String id, name, invigilator;
    public int rows, cols;
    public Set<String> reserved = new HashSet<>();

    public Room(String id, String name, int rows, int cols, String invigilator) {
        this.id = id; this.name = name; this.rows = rows; this.cols = cols;
        this.invigilator = invigilator == null ? "" : invigilator;
    }

    public int capacity()  { return rows * cols - reserved.size(); }
    public int total()     { return rows * cols; }
    public boolean available(int r, int c) { return !reserved.contains(r + "-" + c); }
}
