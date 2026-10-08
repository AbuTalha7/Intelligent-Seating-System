import java.util.*;

public class GreedyAllocator {
    private ConflictGraph graph = new ConflictGraph();
    private ConstraintManager cm;
    private Map<String, Integer> roomCount = new HashMap<>();
    private List<String> unassigned = new ArrayList<>();

    public GreedyAllocator(ConstraintManager cm) { this.cm = cm; }

    public List<String> getUnassigned() { return unassigned; }

    public Map<String, SeatAllocation> allocate(List<Student> sList, List<Room> rList) {
        graph.build(sList);
        Map<String, SeatAllocation> arr = new LinkedHashMap<>();
        roomCount.clear(); unassigned.clear();
        for (Room r : rList) roomCount.put(r.id, 0);

        // Sort: special needs first, then by degree (most constrained first)
        List<Student> sorted = new ArrayList<>(sList);
        sorted.sort((a, b) -> {
            if (a.specialNeeds != b.specialNeeds) return a.specialNeeds ? -1 : 1;
            return graph.degree(b.id) - graph.degree(a.id);
        });

        for (Student s : sorted) {
            boolean ok = false;
            for (Room rm : rList) {
                if (!cm.checkCapacity(rm, roomCount.get(rm.id))) continue;
                int[] seat = bestSeat(s, rm, arr);
                if (seat != null) {
                    arr.put(s.id, new SeatAllocation(s.id, rm.id, seat[0], seat[1]));
                    roomCount.put(rm.id, roomCount.get(rm.id) + 1);
                    ok = true; break;
                }
            }
            if (!ok) ok = backtrack(s, sList, arr, rList);
            if (!ok) unassigned.add(s.id);
        }
        return arr;
    }

    private int[] bestSeat(Student s, Room rm, Map<String, SeatAllocation> arr) {
        int[] best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int r = 0; r < rm.rows; r++) {
            for (int c = 0; c < rm.cols; c++) {
                if (!cm.checkReserved(rm, r, c)) continue;
                if (occupied(rm, r, c, arr)) continue;
                if (!cm.checkSpecial(s, rm, r, c)) continue;
                if (cm.checkAdjacent(graph, s.id, rm, r, c, arr)) continue;
                int score = -r * 10 - Math.abs(c - rm.cols / 2) * 2;
                if (score > bestScore) { bestScore = score; best = new int[]{r, c}; }
            }
        }
        return best;
    }

    private boolean occupied(Room rm, int r, int c, Map<String, SeatAllocation> arr) {
        for (SeatAllocation sa : arr.values())
            if (sa.roomId.equals(rm.id) && sa.row == r && sa.col == c) return true;
        return false;
    }

    private boolean backtrack(Student s, List<Student> all,
                              Map<String, SeatAllocation> arr, List<Room> rList) {
        for (Room rm : rList) {
            for (int r = 0; r < rm.rows; r++) {
                for (int c = 0; c < rm.cols; c++) {
                    if (!cm.checkReserved(rm, r, c)) continue;
                    String occ = occupant(rm, r, c, arr);
                    if (occ != null) {
                        SeatAllocation old = arr.remove(occ);
                        roomCount.put(rm.id, roomCount.get(rm.id) - 1);
                        if (!cm.checkAdjacent(graph, s.id, rm, r, c, arr)
                                && cm.checkSpecial(s, rm, r, c)) {
                            arr.put(s.id, new SeatAllocation(s.id, rm.id, r, c));
                            roomCount.put(rm.id, roomCount.get(rm.id) + 1);
                            Student displaced = findStudent(occ, all);
                            if (displaced != null) {
                                int[] ns = null;
                                for (Room rm2 : rList) {
                                    ns = bestSeat(displaced, rm2, arr);
                                    if (ns != null) {
                                        arr.put(displaced.id, new SeatAllocation(displaced.id, rm2.id, ns[0], ns[1]));
                                        roomCount.put(rm2.id, roomCount.get(rm2.id) + 1);
                                        return true;
                                    }
                                }
                            }
                            arr.remove(s.id);
                            roomCount.put(rm.id, roomCount.get(rm.id) - 1);
                        }
                        arr.put(occ, old);
                        roomCount.put(rm.id, roomCount.get(rm.id) + 1);
                    } else {
                        if (!cm.checkAdjacent(graph, s.id, rm, r, c, arr)
                                && cm.checkSpecial(s, rm, r, c)) {
                            arr.put(s.id, new SeatAllocation(s.id, rm.id, r, c));
                            roomCount.put(rm.id, roomCount.get(rm.id) + 1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String occupant(Room rm, int r, int c, Map<String, SeatAllocation> arr) {
        for (SeatAllocation sa : arr.values())
            if (sa.roomId.equals(rm.id) && sa.row == r && sa.col == c) return sa.studentId;
        return null;
    }

    private Student findStudent(String id, List<Student> all) {
        for (Student s : all) if (s.id.equals(id)) return s;
        return null;
    }
}
