import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class CustomComponents {

    // ── Gradient Panel ─────────────────────────────────────────────────────
    public static class GradientPanel extends JPanel {
        Color c1, c2; boolean vertical;
        public GradientPanel(Color c1, Color c2, boolean vert) {
            this.c1 = c1; this.c2 = c2; vertical = vert; setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(vertical ? new GradientPaint(0, 0, c1, 0, h, c2)
                                 : new GradientPaint(0, 0, c1, w, 0, c2));
            g2.fillRect(0, 0, w, h);
            super.paintComponent(g);
        }
    }

    // ── Card Panel (white, shadow, rounded) ────────────────────────────────
    public static class CardPanel extends JPanel {
        int arc = 14, shadow = 5;
        public CardPanel() {
            setOpaque(false); setBackground(Theme.C_CARD);
            setBorder(BorderFactory.createEmptyBorder(shadow + 8, shadow + 8, shadow + 8, shadow + 8));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // shadow
            g2.setColor(new Color(160, 175, 195, 55));
            g2.fillRoundRect(shadow, shadow, getWidth() - shadow * 2, getHeight() - shadow * 2, arc, arc);
            // body
            g2.setColor(Theme.C_CARD);
            g2.fillRoundRect(0, 0, getWidth() - shadow * 2, getHeight() - shadow * 2, arc, arc);
            super.paintComponent(g);
        }
    }

    // ── Animated Stat Card ─────────────────────────────────────────────────
    public static class StatCard extends CardPanel {
        private JLabel valLbl;
        private int targetVal;
        private Color accent;
        public StatCard(String title, int value, String suffix, Color accent) {
            this.accent = accent; this.targetVal = value;
            setLayout(new BorderLayout(4, 4));
            setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
            valLbl = new JLabel("0" + suffix);
            valLbl.setFont(new Font("SansSerif", Font.BOLD, 30));
            valLbl.setForeground(accent);
            JLabel ttl = new JLabel(title);
            ttl.setFont(Theme.F_SMALL); ttl.setForeground(Theme.C_TEXT2);
            // colour stripe on left
            JPanel stripe = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                }
            };
            stripe.setPreferredSize(new Dimension(6, 0)); stripe.setOpaque(false);
            add(stripe, BorderLayout.WEST);
            JPanel right = new JPanel(new BorderLayout(2, 2)); right.setOpaque(false);
            right.add(valLbl, BorderLayout.CENTER); right.add(ttl, BorderLayout.SOUTH);
            add(right, BorderLayout.CENTER);
            // count-up animation
            animate(value, suffix);
        }
        private void animate(int target, String suffix) {
            int steps = 20;
            javax.swing.Timer t = new javax.swing.Timer(30, null);
            final int[] cur = {0};
            t.addActionListener(e -> {
                cur[0]++;
                int v = (int)(target * (double) cur[0] / steps);
                valLbl.setText(v + suffix);
                if (cur[0] >= steps) { valLbl.setText(target + suffix); t.stop(); }
            });
            t.start();
        }
    }

    // ── Rounded Button ─────────────────────────────────────────────────────
    public static class RoundedButton extends JButton {
        Color bg, hoverBg, pressBg;
        boolean hovered = false, pressed = false;
        int radius = 10;
        public RoundedButton(String text, Color bg, Color hoverBg) {
            super(text); this.bg = bg; this.hoverBg = hoverBg;
            this.pressBg = darker(bg, 25);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(Theme.F_BTN); setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
                public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pressed ? pressBg : hovered ? hoverBg : bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            super.paintComponent(g);
        }
        private Color darker(Color c, int amt) {
            return new Color(Math.max(0, c.getRed() - amt), Math.max(0, c.getGreen() - amt),
                             Math.max(0, c.getBlue() - amt));
        }
    }

    // ── Sidebar Button ─────────────────────────────────────────────────────
    public static class SidebarButton extends JButton {
        boolean active = false;
        Color iconColor;
        public SidebarButton(String text, Color iconColor) {
            super(text); this.iconColor = iconColor;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(Theme.F_BODY); setForeground(new Color(165, 182, 200));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (!active) setForeground(Color.WHITE); repaint(); }
                public void mouseExited(MouseEvent e)  { if (!active) setForeground(new Color(165, 182, 200)); repaint(); }
            });
        }
        public void setActive(boolean a) {
            active = a; setForeground(a ? Color.WHITE : new Color(165, 182, 200)); repaint();
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(Theme.C_SB_ACT); g2.fillRect(0, 0, getWidth(), getHeight());
                // accent stripe
                GradientPaint gp = new GradientPaint(0, 0, iconColor, 0, getHeight(), iconColor.darker());
                g2.setPaint(gp); g2.fillRect(0, 0, 4, getHeight());
            } else if (getModel().isRollover()) {
                g2.setColor(Theme.C_SB_HOV); g2.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }

    // ── Styled Table ───────────────────────────────────────────────────────
    public static class StyledTable extends JTable {
        public StyledTable(TableModel m) {
            super(m); setRowHeight(36); setFont(Theme.F_BODY); setGridColor(Theme.C_BORDER);
            setSelectionBackground(new Color(62, 108, 182, 50)); setSelectionForeground(Theme.C_TEXT);
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setFont(Theme.F_SUB); getTableHeader().setForeground(Color.WHITE);
            getTableHeader().setBackground(Theme.C_TBL_HDR);
            getTableHeader().setPreferredSize(new Dimension(getTableHeader().getWidth(), 38));
            setIntercellSpacing(new Dimension(0, 1));
        }
        public Component prepareRenderer(TableCellRenderer r, int row, int col) {
            Component c = super.prepareRenderer(r, row, col);
            if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.C_TBL_ALT);
            if (c instanceof JComponent) ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            return c;
        }
    }

    // ── Search Field ───────────────────────────────────────────────────────
    public static class SearchField extends JTextField {
        public SearchField(String placeholder) {
            super(20); setFont(Theme.F_BODY);
            setForeground(Theme.C_TEXT); setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(Theme.C_BORDER, 1, 8),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
            // placeholder
            setText(placeholder); setForeground(Theme.C_TEXT3);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) { setText(""); setForeground(Theme.C_TEXT); }
                }
                public void focusLost(FocusEvent e) {
                    if (getText().isBlank()) { setText(placeholder); setForeground(Theme.C_TEXT3); }
                }
            });
        }
        public String getSearchText() {
            String t = getText().trim();
            return t.startsWith("Search") || t.startsWith("Filter") ? "" : t;
        }
    }

    // ── Rounded Line Border ────────────────────────────────────────────────
    public static class RoundedLineBorder extends AbstractBorder {
        Color color; int thick, arc;
        public RoundedLineBorder(Color c, int t, int a) { color = c; thick = t; arc = a; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thick));
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc); g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(thick, thick, thick, thick); }
    }

    // ── Badge Label ────────────────────────────────────────────────────────
    public static class BadgeLabel extends JLabel {
        Color bg;
        public BadgeLabel(String text, Color bg) {
            super(text); this.bg = bg;
            setFont(new Font("SansSerif", Font.BOLD, 10));
            setForeground(Color.WHITE); setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }

    // ── Toast Notification Manager ─────────────────────────────────────────
    public static class Toast extends JWindow {
        public static void show(JFrame parent, String message, Color accent) {
            Toast t = new Toast(parent, message, accent);
            t.setVisible(true);
            javax.swing.Timer hideTimer = new javax.swing.Timer(2800, e -> t.dispose());
            hideTimer.setRepeats(false); hideTimer.start();
        }
        private Toast(JFrame parent, String message, Color accent) {
            super(parent);
            JPanel p = new JPanel(new BorderLayout(8, 0)) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(28, 38, 56, 230));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
            };
            p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            JPanel dot = new JPanel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D) g).setColor(accent);
                    ((Graphics2D) g).fillOval(0, 4, 10, 10);
                }
            };
            dot.setPreferredSize(new Dimension(14, 18)); dot.setOpaque(false);
            JLabel lbl = new JLabel(message); lbl.setFont(Theme.F_BODY); lbl.setForeground(Color.WHITE);
            p.add(dot, BorderLayout.WEST); p.add(lbl, BorderLayout.CENTER);
            setContentPane(p); pack();
            if (parent != null) {
                Point loc = parent.getLocation();
                Dimension pSize = parent.getSize();
                setLocation(loc.x + pSize.width - getWidth() - 24,
                            loc.y + pSize.height - getHeight() - 40);
            }
        }
    }

    // ── Section Header (bold title + subtitle row) ─────────────────────────
    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));
        JLabel t = new JLabel(title); t.setFont(Theme.F_TITLE); t.setForeground(Theme.C_TEXT);
        p.add(t, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel s = new JLabel(subtitle); s.setFont(Theme.F_BODY); s.setForeground(Theme.C_TEXT2);
            p.add(s, BorderLayout.CENTER);
        }
        return p;
    }
}
