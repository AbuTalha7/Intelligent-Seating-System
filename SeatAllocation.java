public class SeatAllocation {
    public String studentId, roomId;
    public int row, col;

    public SeatAllocation(String studentId, String roomId, int row, int col) {
        this.studentId = studentId; this.roomId = roomId;
        this.row = row; this.col = col;
    }
}
