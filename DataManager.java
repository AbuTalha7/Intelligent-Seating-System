import java.util.*;
import java.io.*;

public class DataManager {
    public static List<Student> students = new ArrayList<>();
    public static List<Room> rooms = new ArrayList<>();
    public static Map<String, SeatAllocation> arrangement = new LinkedHashMap<>();
    public static boolean generated = false;
    public static ExamInfo examInfo = new ExamInfo();
    public static List<String> activityLog = new ArrayList<>();

    // ── Sample data (updated to match new BSE classes / A-B-C sections / subjects)
    // ──
    public static void loadSampleData() {
        students.clear();
        rooms.clear();
        arrangement.clear();
        generated = false;
        activityLog.clear();

        // Format: { class, section, subjectGroup }
        // Sections and subjects follow the dropdown mappings:
        // A → Programming & OOP | Thermodynamics | Circuit Analysis | Structural
        // Analysis
        // B → Data Structures | Mechanics | Digital Signals
        // C → Algorithms | Database Systems | Computer Networks
        String[][] groups = {
                { "BSE 1", "A", "Programming & OOP" },
                { "BSE 1", "B", "Data Structures" },
                { "BSE 1", "C", "Algorithms" },
                { "BSE 2", "A", "Thermodynamics" },
                { "BSE 2", "B", "Mechanics" },
                { "BSE 2", "C", "Database Systems" },
                { "BSE 3", "A", "Circuit Analysis" },
                { "BSE 3", "B", "Digital Signals" },
                { "BSE 4", "A", "Structural Analysis" },
                { "BSE 4", "C", "Computer Networks" },
                { "BSE 5", "A", "Programming & OOP" },
                { "BSE 5", "B", "Data Structures" },
                { "BSE 6", "A", "Circuit Analysis" },
                { "BSE 6", "C", "Algorithms" },
                { "BSE 7", "B", "Mechanics" },
                { "BSE 8", "A", "Thermodynamics" },
        };

        String[] firstNames = { "Ali", "Sara", "John", "Maria", "Ahmed", "Emma", "Zara", "Omar",
                "Lena", "Ravi", "Nina", "Leo", "Nour", "David", "Sofia", "Hamza" };
        String[] lastNames = { "Khan", "Smith", "Ali", "Brown", "Hassan", "Lee", "Wang", "Patel",
                "Garcia", "Kim", "Chen", "Müller", "Silva", "Nguyen", "Cohen", "Iqbal" };

        int id = 1001;
        Random rng = new Random(42);
        for (String[] grp : groups) {
            int count = 6 + rng.nextInt(5); // 6–10 per group
            for (int i = 0; i < count; i++) {
                String fn = firstNames[rng.nextInt(firstNames.length)];
                String ln = lastNames[rng.nextInt(lastNames.length)];
                String nm = fn + " " + ln;
                boolean sn = (i == 0); // first student in each group has special needs
                // IDs are purely numeric, matching the new ID validation
                students.add(new Student(String.valueOf(id), nm, "",
                        grp[0], grp[1], grp[2], sn));
                id++;
            }
        }

        rooms.add(new Room("R1", "Room 101 — Main Hall", 6, 5, "Prof. Adams"));
        rooms.add(new Room("R2", "Room 102 — Block B", 5, 6, "Prof. Baker"));
        rooms.add(new Room("R3", "Room 103 — Lab Wing", 4, 5, "Dr. Clark"));
        rooms.add(new Room("R4", "Room 201 — Science Wing", 5, 5, "Dr. Evans"));

        rooms.get(0).reserved.add("0-2");
        rooms.get(1).reserved.add("3-3");
        rooms.get(2).reserved.add("1-4");
        rooms.get(3).reserved.add("2-2");

        examInfo.examName = "Final Examination 2025";
        examInfo.date = "2025-12-15";
        examInfo.time = "09:00 AM";
        examInfo.venue = "Main Campus";
        examInfo.supervisor = "Dr. Smith";
        examInfo.institution = "University of Technology";

        log("Sample data loaded: " + students.size() + " students across "
                + groups.length + " groups (BSE 1–8), " + rooms.size() + " rooms");
    }

    // ── Activity log ─────────────────────────────────────────────────────────
    public static void log(String msg) {
        String ts = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        activityLog.add(0, "[" + ts + "] " + msg);
        if (activityLog.size() > 50)
            activityLog.remove(activityLog.size() - 1);
    }

    // ── Persistence: save/load to a simple key=value format ──────────────────
    public static void saveToFile(File f) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("## ExamSeatingSystem save file v2");
            pw.println("EXAM|" + examInfo.examName + "|" + examInfo.date + "|" + examInfo.time
                    + "|" + examInfo.venue + "|" + examInfo.supervisor + "|" + examInfo.institution);
            for (Student s : students)
                pw.println("STU|" + s.id + "|" + s.name + "|" + s.email + "|" + s.className
                        + "|" + s.section + "|" + s.subjectGroup + "|" + s.specialNeeds);
            for (Room r : rooms) {
                String res = String.join(",", r.reserved);
                pw.println("ROOM|" + r.id + "|" + r.name + "|" + r.rows + "|" + r.cols
                        + "|" + r.invigilator + "|" + res);
            }
            if (generated) {
                for (SeatAllocation sa : arrangement.values())
                    pw.println("SEAT|" + sa.studentId + "|" + sa.roomId + "|" + sa.row + "|" + sa.col);
            }
        }
        log("Session saved to " + f.getName());
    }

    public static void loadFromFile(File f) throws IOException {
        students.clear();
        rooms.clear();
        arrangement.clear();
        generated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("##") || line.isBlank())
                    continue;
                String[] p = line.split("\\|", -1);
                switch (p[0]) {
                    case "EXAM":
                        examInfo.examName = p[1];
                        examInfo.date = p[2];
                        examInfo.time = p[3];
                        examInfo.venue = p[4];
                        examInfo.supervisor = p[5];
                        examInfo.institution = p[6];
                        break;
                    case "STU":
                        students.add(new Student(p[1], p[2], p[3], p[4], p[5], p[6],
                                Boolean.parseBoolean(p[7])));
                        break;
                    case "ROOM":
                        Room rm = new Room(p[1], p[2], Integer.parseInt(p[3]),
                                Integer.parseInt(p[4]), p[5]);
                        if (!p[6].isBlank())
                            for (String r : p[6].split(","))
                                rm.reserved.add(r);
                        rooms.add(rm);
                        break;
                    case "SEAT":
                        arrangement.put(p[1], new SeatAllocation(p[1], p[2],
                                Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                        generated = true;
                        break;
                }
            }
        }
        log("Session loaded from " + f.getName() + ": " + students.size() + " students");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    public static int totalCapacity() {
        int cap = 0;
        for (Room r : rooms)
            cap += r.capacity();
        return cap;
    }

    public static Student findStudentById(String id) {
        for (Student s : students)
            if (s.id.equals(id))
                return s;
        return null;
    }

    public static Room findRoomById(String id) {
        for (Room r : rooms)
            if (r.id.equals(id))
                return r;
        return null;
    }

    /** Remove all seat allocations that belong to the given room. */
    public static void clearArrangementForRoom(String roomId) {
        arrangement.values().removeIf(sa -> sa.roomId.equals(roomId));
        if (arrangement.isEmpty())
            generated = false;
    }
}
