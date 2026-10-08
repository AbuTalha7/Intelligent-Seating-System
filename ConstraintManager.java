import java.util.*;

public class ConstraintManager {
    private int separationDistance = 1; // minimum seat distance between same-group students

    public void setSeparationDistance(int d) { separationDistance = Math.max(1, d); }
    public int getSeparationDistance() { return separationDistance; }

    public boolean checkCapacity(Room rm, int assigned) {
        return assigned < rm.capacity();
    }

    public boolean checkReserved(Room rm, int r, int c) {
        return rm.available(r, c);
    }

    public boolean checkSpecial(Student s, Room rm, int r, int c) {
        return !s.specialNeeds || r <= Math.min(1, rm.rows - 1);
    }

    /**
     * Returns true if placing student 'sid' at (r,c) in room 'rm' causes
     * a conflict with any adjacent (within separationDistance) seat.
     */
    public boolean checkAdjacent(ConflictGraph g, String sid, Room rm, int r, int c,
                                  Map<String, SeatAllocation> arr) {
        for (int dr = -separationDistance; dr <= separationDistance; dr++) {
            for (int dc = -separationDistance; dc <= separationDistance; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < rm.rows && nc >= 0 && nc < rm.cols) {
                    for (SeatAllocation sa : arr.values()) {
                        if (sa.roomId.equals(rm.id) && sa.row == nr && sa.col == nc) {
                            if (g.areConflicting(sid, sa.studentId)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
