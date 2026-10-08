import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChartPanel extends JPanel {
    private AppFrame frame;
    private JComboBox<String> roomCombo;
    private JComboBox<String> classCombo;
    private JPanel chartContainer;
    private CustomComponents.SearchField searchField;
    private String highlightId = null;

    private static final String[] BSE_CLASSES = {
            "All Classes", "BSE 1", "BSE 2", "BSE 3", "BSE 4",
            "BSE 5", "BSE 6", "BSE 7", "BSE 8"
    };

    public ChartPanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Top header block (3 rows stacked) ────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);

        // Row 1: Title (left) + Clear Arrangement button (right) — BoxLayout, no
        // overlap
        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

        JLabel titleLbl = new JLabel("Seating Chart");
        titleLbl.setFont(Theme.F_TITLE);
        titleLbl.setForeground(Theme.C_TEXT);
        titleLbl.setAlignmentY(Component.CENTER_ALIGNMENT);
        titleRow.add(titleLbl);
        titleRow.add(Box.createHorizontalGlue());

        CustomComponents.RoundedButton clearBtn = new CustomComponents.RoundedButton(
                "↺ Clear Arrangement", new Color(180, 70, 60), new Color(200, 90, 80));
        clearBtn.setPreferredSize(new Dimension(170, 36));
        clearBtn.setMaximumSize(new Dimension(170, 36));
        clearBtn.setMinimumSize(new Dimension(170, 36));
        clearBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> clearArrangement());
        titleRow.add(clearBtn);

        top.add(titleRow, BorderLayout.NORTH);

        // Row 2: Search field + Class filter + Room selector — their own dedicated row
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsRow.setOpaque(false);

        JLabel ico = new JLabel("⌕");
        ico.setFont(Theme.F_SUB);
        ico.setForeground(Theme.C_TEXT2);
        searchField = new CustomComponents.SearchField("Search student ID or name…");
        searchField.setPreferredSize(new Dimension(210, 32));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateHighlight();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateHighlight();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateHighlight();
            }
        });

        JLabel classLbl = new JLabel("Class:");
        classLbl.setFont(Theme.F_BODY);
        classLbl.setForeground(Theme.C_TEXT2);
        classCombo = new JComboBox<>(BSE_CLASSES);
        classCombo.setFont(Theme.F_BODY);
        classCombo.setPreferredSize(new Dimension(120, 32));
        classCombo.addActionListener(e -> refreshChart());

        JLabel roomLbl = new JLabel("Room:");
        roomLbl.setFont(Theme.F_BODY);
        roomLbl.setForeground(Theme.C_TEXT2);
        roomCombo = new JComboBox<>();
        roomCombo.setFont(Theme.F_BODY);
        roomCombo.setPreferredSize(new Dimension(210, 32));
        roomCombo.addActionListener(e -> refreshChart());

        controlsRow.add(ico);
        controlsRow.add(searchField);
        controlsRow.add(Box.createHorizontalStrut(6));
        controlsRow.add(classLbl);
        controlsRow.add(classCombo);
        controlsRow.add(Box.createHorizontalStrut(6));
        controlsRow.add(roomLbl);
        controlsRow.add(roomCombo);

        // Row 3: Legend strip
        JPanel legendStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legendStrip.setOpaque(false);
        addLegendItem(legendStrip, Theme.C_SUCCESS, "Occupied");
        addLegendItem(legendStrip, new Color(240, 225, 200), "Reserved");
        addLegendItem(legendStrip, new Color(220, 230, 240), "Empty");
        addLegendItem(legendStrip, Theme.C_WARN, "Highlighted / Special Needs");
        addLegendItem(legendStrip, new Color(200, 210, 220), "Dimmed (other class)");

        // Stack rows 2 + 3 in the CENTER slot of `top`
        JPanel middleRows = new JPanel(new BorderLayout(0, 4));
        middleRows.setOpaque(false);
        middleRows.add(controlsRow, BorderLayout.NORTH);
        middleRows.add(legendStrip, BorderLayout.SOUTH);
        top.add(middleRows, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // ── Chart scroll area ─────────────────────────────────────────────────
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Theme.C_BG);
        JScrollPane sp = new JScrollPane(chartContainer);
        sp.getViewport().setBackground(Theme.C_BG);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(sp, BorderLayout.CENTER);
    }

    private void addLegendItem(JPanel p, Color color, String label) {
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setColor(color);
                ((Graphics2D) g).fillRoundRect(0, 2, 14, 14, 4, 4);
            }
        };
        dot.setPreferredSize(new Dimension(16, 18));
        dot.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.F_SMALL);
        lbl.setForeground(Theme.C_TEXT2);
        p.add(dot);
        p.add(lbl);
    }

    public void refresh() {
        String prev = (String) roomCombo.getSelectedItem();
        roomCombo.removeAllItems();
        for (Room r : DataManager.rooms)
            roomCombo.addItem(r.id + " – " + r.name);
        if (prev != null) {
            for (int i = 0; i < roomCombo.getItemCount(); i++)
                if (roomCombo.getItemAt(i).equals(prev)) {
                    roomCombo.setSelectedIndex(i);
                    break;
                }
        }
        refreshChart();
    }

    private void updateHighlight() {
        String q = searchField.getSearchText().toLowerCase();
        highlightId = null;
        if (!q.isEmpty()) {
            for (Student s : DataManager.students) {
                if (s.id.toLowerCase().contains(q) || s.name.toLowerCase().contains(q)) {
                    highlightId = s.id;
                    break;
                }
            }
        }
        refreshChart();
    }

    private void refreshChart() {
        chartContainer.removeAll();
        if (!DataManager.generated || DataManager.rooms.isEmpty()) {
            JPanel empty = new JPanel(new FlowLayout());
            empty.setBackground(Theme.C_BG);
            JLabel lbl = new JLabel("Generate a seating arrangement first");
            lbl.setFont(Theme.F_HEAD);
            lbl.setForeground(Theme.C_TEXT2);
            empty.add(lbl);
            chartContainer.add(empty, BorderLayout.CENTER);
        } else {
            String sel = (String) roomCombo.getSelectedItem();
            if (sel != null) {
                String rid = sel.split("–")[0].trim();
                Room rm = DataManager.findRoomById(rid);
                if (rm != null) {
                    String selClass = (String) classCombo.getSelectedItem();
                    String classFilter = (selClass == null || selClass.equals("All Classes")) ? null : selClass;
                    SeatChartPanel chart = new SeatChartPanel(rm, highlightId, classFilter);
                    chartContainer.add(chart, BorderLayout.CENTER);
                }
            }
        }
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void clearArrangement() {
        if (!DataManager.generated || DataManager.arrangement.isEmpty()) {
            frame.showToast("No seating arrangement to clear", Theme.C_WARN);
            return;
        }
        int res = JOptionPane.showConfirmDialog(frame,
                "Clear the entire seating arrangement?\n"
                        + "(" + DataManager.arrangement.size() + " seat allocations across all rooms)\n\n"
                        + "Rooms and students will NOT be deleted.",
                "Confirm Clear Arrangement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("Seating arrangement cleared from Seating Chart panel");
            frame.refreshAll();
            frame.showToast("Seating arrangement cleared", Theme.C_WARN);
        }
    }
}
