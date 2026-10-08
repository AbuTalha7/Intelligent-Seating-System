import javax.swing.*;
import java.awt.*;
import java.util.*;

public class StatisticsPanel extends JPanel {

    public StatisticsPanel() {
        setLayout(new BorderLayout(0, 0)); setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        JLabel title = new JLabel("Statistics");
        title.setFont(Theme.F_TITLE); title.setForeground(Theme.C_TEXT);
        add(title, BorderLayout.NORTH);
        add(buildChartArea(), BorderLayout.CENTER);
    }

    private JPanel buildChartArea() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 16));
        panel.setOpaque(false); panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panel.add(wrapCard("Students per Class-Section", buildGroupBarChart()));
        panel.add(wrapCard("Room Utilisation", buildRoomUtilChart()));
        panel.add(wrapCard("Subject Group Distribution", buildSubjectPieChart()));
        panel.add(wrapCard("Special Needs Summary", buildSpecialNeedsChart()));
        return panel;
    }

    private CustomComponents.CardPanel wrapCard(String title, JPanel chart) {
        CustomComponents.CardPanel card = new CustomComponents.CardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JLabel t = new JLabel(title); t.setFont(Theme.F_SUB); t.setForeground(Theme.C_PRI);
        card.add(t, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        removeAll();
        JLabel title = new JLabel("Statistics");
        title.setFont(Theme.F_TITLE); title.setForeground(Theme.C_TEXT);
        add(title, BorderLayout.NORTH);
        add(buildChartArea(), BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ── Bar chart: students per class-section group ────────────────────────
    private JPanel buildGroupBarChart() {
        // Count per group
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Student s : DataManager.students) {
            String gk = s.groupKey();
            counts.put(gk, counts.getOrDefault(gk, 0) + 1);
        }
        return new BarChartCanvas(counts, Theme.CHART_PALETTE, "students");
    }

    // ── Bar chart: room utilisation ────────────────────────────────────────
    private JPanel buildRoomUtilChart() {
        Map<String, Integer> used = new LinkedHashMap<>();
        Map<String, Integer> cap  = new LinkedHashMap<>();
        for (Room r : DataManager.rooms) {
            cap.put(r.name.length() > 16 ? r.name.substring(0, 16) : r.name, r.capacity());
            int u = 0;
            if (DataManager.generated)
                for (SeatAllocation sa : DataManager.arrangement.values())
                    if (sa.roomId.equals(r.id)) u++;
            used.put(r.name.length() > 16 ? r.name.substring(0, 16) : r.name, u);
        }
        return new DualBarChartCanvas(used, cap);
    }

    // ── Pie chart: subject group distribution ─────────────────────────────
    private JPanel buildSubjectPieChart() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Student s : DataManager.students)
            counts.put(s.subjectGroup, counts.getOrDefault(s.subjectGroup, 0) + 1);
        return new PieChartCanvas(counts, Theme.CHART_PALETTE);
    }

    // ── Donut chart: special needs ─────────────────────────────────────────
    private JPanel buildSpecialNeedsChart() {
        int sn = 0, normal = 0;
        for (Student s : DataManager.students) { if (s.specialNeeds) sn++; else normal++; }
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("Special Needs", sn); m.put("Regular", normal);
        return new PieChartCanvas(m, new Color[]{Theme.C_WARN, Theme.C_PRI_LIGHT});
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inner canvas classes
    // ══════════════════════════════════════════════════════════════════════

    static class BarChartCanvas extends JPanel {
        Map<String, Integer> data; Color[] palette; String unit;
        BarChartCanvas(Map<String, Integer> data, Color[] palette, String unit) {
            this.data = data; this.palette = palette; this.unit = unit;
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (data.isEmpty()) { g2.setColor(Theme.C_TEXT3); g2.setFont(Theme.F_SMALL); g2.drawString("No data", 10, h/2); return; }

            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int n = data.size();
            int barW = Math.max(12, (w - 20) / n - 8);
            int gap  = Math.max(4, (w - 20 - barW * n) / Math.max(1, n + 1));
            int botY = h - 34;
            int topY = 10;
            int chartH = botY - topY;

            // Grid lines
            g2.setStroke(new BasicStroke(0.5f));
            g2.setColor(new Color(210, 220, 230));
            for (int i = 1; i <= 4; i++) {
                int gy = botY - (chartH * i / 4);
                g2.drawLine(10, gy, w - 10, gy);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9)); g2.setColor(Theme.C_TEXT3);
                g2.drawString(String.valueOf(maxVal * i / 4), 2, gy + 4);
                g2.setColor(new Color(210, 220, 230));
            }

            // Bars
            int x = 10 + gap;
            int ci = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int val = e.getValue();
                int bh = (int)((double) val / maxVal * chartH);
                Color c = palette[ci % palette.length];
                // bar
                g2.setColor(c); g2.fillRoundRect(x, botY - bh, barW, bh, 6, 6);
                // value label on top
                g2.setFont(new Font("SansSerif", Font.BOLD, 10)); g2.setColor(c);
                g2.drawString(String.valueOf(val), x + barW/2 - 4, botY - bh - 3);
                // key label below
                g2.setColor(Theme.C_TEXT2); g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                String lbl = e.getKey().length() > 7 ? e.getKey().substring(0, 7) : e.getKey();
                g2.drawString(lbl, x + barW/2 - lbl.length()*3, botY + 12);
                x += barW + gap; ci++;
            }
            // baseline
            g2.setStroke(new BasicStroke(1.5f)); g2.setColor(Theme.C_BORDER);
            g2.drawLine(10, botY, w - 10, botY);
        }
    }

    static class DualBarChartCanvas extends JPanel {
        Map<String, Integer> used, cap;
        DualBarChartCanvas(Map<String, Integer> used, Map<String, Integer> cap) {
            this.used = used; this.cap = cap; setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (cap.isEmpty()) { g2.setColor(Theme.C_TEXT3); g2.setFont(Theme.F_SMALL); g2.drawString("No data", 10, h/2); return; }

            int maxVal = cap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int n = cap.size();
            int pairW = Math.max(20, (w - 20) / n - 8);
            int barW  = pairW / 2 - 2;
            int gap   = Math.max(4, (w - 20 - pairW * n) / Math.max(1, n + 1));
            int botY  = h - 34, topY = 10, chartH = botY - topY;

            g2.setStroke(new BasicStroke(0.5f)); g2.setColor(new Color(210, 220, 230));
            for (int i = 1; i <= 4; i++) {
                int gy = botY - chartH * i / 4;
                g2.drawLine(10, gy, w - 10, gy);
            }

            int x = 10 + gap;
            String[] keys = cap.keySet().toArray(new String[0]);
            for (String key : keys) {
                int capV  = cap.getOrDefault(key, 0);
                int usedV = used.getOrDefault(key, 0);
                int capH  = (int)((double) capV  / maxVal * chartH);
                int useH  = (int)((double) usedV / maxVal * chartH);

                g2.setColor(Theme.C_PRI_LIGHT); g2.fillRoundRect(x, botY - capH, barW, capH, 4, 4);
                g2.setColor(Theme.C_SUCCESS);   g2.fillRoundRect(x + barW + 2, botY - useH, barW, useH, 4, 4);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 9)); g2.setColor(Theme.C_TEXT2);
                String lbl = key.length() > 8 ? key.substring(0, 8) : key;
                g2.drawString(lbl, x, botY + 12);
                x += pairW + gap;
            }
            g2.setStroke(new BasicStroke(1.5f)); g2.setColor(Theme.C_BORDER);
            g2.drawLine(10, botY, w - 10, botY);

            // Mini legend
            g2.setColor(Theme.C_PRI_LIGHT); g2.fillRect(w - 90, 4, 10, 8);
            g2.setColor(Theme.C_TEXT2); g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.drawString("Capacity", w - 77, 12);
            g2.setColor(Theme.C_SUCCESS); g2.fillRect(w - 90, 16, 10, 8);
            g2.setColor(Theme.C_TEXT2); g2.drawString("Used", w - 77, 24);
        }
    }

    static class PieChartCanvas extends JPanel {
        Map<String, Integer> data; Color[] palette;
        PieChartCanvas(Map<String, Integer> data, Color[] palette) {
            this.data = data; this.palette = palette; setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (data.isEmpty()) { g2.setColor(Theme.C_TEXT3); g2.setFont(Theme.F_SMALL); g2.drawString("No data", w/2 - 20, h/2); return; }

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) return;

            int size = Math.min(w - 140, h - 40);
            int px = 10, py = (h - size) / 2;

            double startAngle = 0;
            int ci = 0;
            java.util.List<String> keys = new ArrayList<>(data.keySet());
            for (String key : keys) {
                int val = data.get(key);
                double angle = (double) val / total * 360;
                g2.setColor(palette[ci % palette.length]);
                g2.fillArc(px, py, size, size, (int) startAngle, (int) angle);
                startAngle += angle; ci++;
            }
            // Donut hole
            g2.setColor(Theme.C_CARD);
            int hole = size / 3;
            g2.fillOval(px + size/2 - hole/2, py + size/2 - hole/2, hole, hole);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13)); g2.setColor(Theme.C_TEXT);
            String tot = String.valueOf(total);
            g2.drawString(tot, px + size/2 - tot.length()*4, py + size/2 + 5);

            // Legend
            int ly = py + 4; ci = 0;
            int lx = px + size + 12;
            for (String key : keys) {
                int val = data.get(key);
                g2.setColor(palette[ci % palette.length]);
                g2.fillRoundRect(lx, ly, 12, 12, 4, 4);
                g2.setColor(Theme.C_TEXT); g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                String lbl = key.length() > 14 ? key.substring(0, 14) : key;
                g2.drawString(lbl, lx + 16, ly + 10);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9)); g2.setColor(Theme.C_TEXT2);
                g2.drawString(val + " (" + (val * 100 / total) + "%)", lx + 16, ly + 22);
                ly += 30; ci++;
                if (ly > py + size - 10) break;
            }
        }
    }
}
