import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SeatChartPanel extends JPanel {
    private Room room;
    private String highlightId;
    private String classFilter;   // null = show all; otherwise dim students not in this class
    private Map<String, Color> groupColors;
    private Map<Rectangle, String> tooltipAreas = new HashMap<>();

    private static final int SW = 56, SH = 50, GX = 10, GY = 10, OX = 50, OY = 50;

    public SeatChartPanel(Room rm) { this(rm, null, null); }

    public SeatChartPanel(Room rm, String highlightId) { this(rm, highlightId, null); }

    public SeatChartPanel(Room rm, String highlightId, String classFilter) {
        this.room = rm;
        this.highlightId = highlightId;
        this.classFilter = classFilter;
        groupColors = buildGroupColors();
        int w = rm.cols * (SW + GX) + OX + 30;
        int h = rm.rows * (SH + GY) + OY + 80;
        setPreferredSize(new Dimension(w, h));
        setBackground(Theme.C_BG);

        ToolTipManager.sharedInstance().registerComponent(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                for (Map.Entry<Rectangle, String> entry : tooltipAreas.entrySet()) {
                    if (entry.getKey().contains(e.getPoint())) {
                        setToolTipText(entry.getValue()); return;
                    }
                }
                setToolTipText(null);
            }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Theme.C_BG); g2.fillRect(0, 0, getWidth(), getHeight());

        if (room == null) return;
        tooltipAreas.clear();

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 15)); g2.setColor(Theme.C_PRI);
        String filterTag = (classFilter != null) ? "  [Showing: " + classFilter + "]" : "";
        g2.drawString(room.name + "   (" + room.rows + " rows × " + room.cols + " cols"
                + "  |  Invigilator: " + (room.invigilator.isEmpty() ? "—" : room.invigilator) + ")"
                + filterTag, OX, 28);

        // Column labels
        g2.setFont(new Font("SansSerif", Font.BOLD, 11)); g2.setColor(Theme.C_TEXT2);
        for (int c = 0; c < room.cols; c++) {
            int x = OX + c * (SW + GX) + SW / 2 - 5;
            g2.drawString("C" + (c + 1), x, OY - 6);
        }

        // Build seat lookup
        Map<String, SeatAllocation> roomSeats = new HashMap<>();
        for (SeatAllocation sa : DataManager.arrangement.values())
            if (sa.roomId.equals(room.id)) roomSeats.put(sa.row + "-" + sa.col, sa);

        Map<String, Student> stuMap = new HashMap<>();
        for (Student s : DataManager.students) stuMap.put(s.id, s);

        // Draw seats
        for (int r = 0; r < room.rows; r++) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11)); g2.setColor(Theme.C_TEXT2);
            g2.drawString("R" + (r + 1), 6, OY + r * (SH + GY) + SH / 2 + 4);

            for (int c = 0; c < room.cols; c++) {
                int x = OX + c * (SW + GX);
                int y = OY + r * (SH + GY);
                String key = r + "-" + c;
                SeatAllocation sa = roomSeats.get(key);
                boolean reserved = room.reserved.contains(key);

                drawSeat(g2, x, y, key, sa, reserved, stuMap);
                tooltipAreas.put(new Rectangle(x, y, SW, SH), buildTooltip(key, sa, reserved, stuMap));
            }
        }

        drawLegend(g2);
    }

    private void drawSeat(Graphics2D g2, int x, int y, String key,
                          SeatAllocation sa, boolean reserved, Map<String, Student> stuMap) {
        if (reserved) {
            g2.setColor(new Color(248, 232, 200));
            g2.fillRoundRect(x, y, SW, SH, 10, 10);
            g2.setColor(new Color(220, 185, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, SW, SH, 10, 10);
            g2.setColor(Theme.C_WARN.darker());
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("RESERVED", x + 5, y + SH / 2 + 3);

        } else if (sa != null) {
            Student s = stuMap.get(sa.studentId);
            Color baseColor = s != null ? groupColors.getOrDefault(s.groupKey(), Theme.C_PRI_LIGHT)
                                       : Theme.C_PRI_LIGHT;

            boolean isHighlighted = highlightId != null && sa.studentId.equals(highlightId);

            // Dim if: student search active and not the match,
            //         OR class filter active and student's class doesn't match
            boolean dim = false;
            if (highlightId != null && !isHighlighted) dim = true;
            if (classFilter != null && s != null && !s.className.equals(classFilter)) dim = true;
            // If both filters active, highlighted student wins over dim
            if (isHighlighted) dim = false;

            if (dim) {
                g2.setColor(new Color(200, 210, 220));
                g2.fillRoundRect(x, y, SW, SH, 10, 10);
                g2.setColor(new Color(180, 195, 210));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(x, y, SW, SH, 10, 10);
            } else {
                g2.setColor(isHighlighted ? Theme.C_WARN : baseColor);
                g2.fillRoundRect(x, y, SW, SH, 10, 10);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRoundRect(x + 3, y + 3, SW - 6, SH / 2 - 2, 7, 7);

                g2.setStroke(new BasicStroke(isHighlighted ? 2.5f : 1f));
                g2.setColor(isHighlighted ? Theme.C_WARN.darker() : new Color(0, 0, 0, 40));
                g2.drawRoundRect(x, y, SW, SH, 10, 10);

                if (s != null) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    String shortName = s.name.length() > 7 ? s.name.substring(0, 7) : s.name;
                    g2.drawString(shortName, x + 4, y + 18);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2.drawString(s.className + "-" + s.section, x + 4, y + 30);
                    if (s.specialNeeds) {
                        g2.setColor(Theme.C_WARN); g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                        g2.drawString("★", x + SW - 14, y + 14);
                    }
                }
                g2.setColor(new Color(255, 255, 255, 100)); g2.setFont(new Font("SansSerif", Font.PLAIN, 7));
                g2.drawString(sa.studentId, x + 4, y + SH - 4);
            }
        } else {
            // Empty
            g2.setColor(new Color(225, 233, 242));
            g2.fillRoundRect(x, y, SW, SH, 10, 10);
            g2.setColor(new Color(190, 205, 220));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, SW, SH, 10, 10);
            g2.setColor(Theme.C_TEXT3); g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.drawString("Empty", x + 10, y + SH / 2 + 3);
        }

        // Seat position top-right
        g2.setColor(new Color(140, 155, 170)); g2.setFont(new Font("SansSerif", Font.PLAIN, 7));
        String pos = (Integer.parseInt(key.split("-")[0]) + 1) + "," + (Integer.parseInt(key.split("-")[1]) + 1);
        g2.drawString(pos, x + SW - 15, y + 8);
    }

    private String buildTooltip(String key, SeatAllocation sa, boolean reserved, Map<String, Student> stuMap) {
        if (reserved) return "<html><b>Reserved seat</b><br>Position: " + key + "</html>";
        if (sa == null) return "<html><b>Empty seat</b><br>Position: " + key + "</html>";
        Student s = stuMap.get(sa.studentId);
        if (s == null) return "<html>Seat: " + sa.studentId + "</html>";
        return "<html><b>" + s.name + "</b><br>" +
                "ID: " + s.id + "<br>" +
                "Class: " + s.className + " – " + s.section + "<br>" +
                "Subject: " + s.subjectGroup + "<br>" +
                "Special Needs: " + (s.specialNeeds ? "Yes ★" : "No") + "</html>";
    }

    private void drawLegend(Graphics2D g2) {
        int ly = OY + room.rows * (SH + GY) + 14;
        g2.setFont(Theme.F_SMALL); g2.setColor(Theme.C_TEXT);
        g2.drawString("Groups:", OX, ly); ly += 16;
        int lx = OX;
        for (Map.Entry<String, Color> e : groupColors.entrySet()) {
            if (lx + 100 > getWidth() - 20) { lx = OX; ly += 18; }
            g2.setColor(e.getValue()); g2.fillRoundRect(lx, ly - 11, 14, 12, 4, 4);
            g2.setColor(Theme.C_TEXT); g2.setFont(Theme.F_SMALL);
            g2.drawString(e.getKey(), lx + 18, ly);
            lx += e.getKey().length() * 7 + 36;
        }
    }

    private Map<String, Color> buildGroupColors() {
        Map<String, Color> map = new LinkedHashMap<>();
        int ci = 0;
        for (Student s : DataManager.students) {
            String gk = s.groupKey();
            if (!map.containsKey(gk)) map.put(gk, Theme.CHART_PALETTE[ci++ % Theme.CHART_PALETTE.length]);
        }
        return map;
    }
}
