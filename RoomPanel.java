import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class RoomPanel extends JPanel {
    private AppFrame frame;
    private DefaultTableModel model;
    private JTable table;
    private CustomComponents.SearchField searchField;

    public RoomPanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0)); setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false);

        // ── Title row: BoxLayout so title and buttons never overlap ──────────
        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

        JLabel titleLbl = new JLabel("Room Management");
        titleLbl.setFont(Theme.F_TITLE); titleLbl.setForeground(Theme.C_TEXT);
        titleLbl.setAlignmentY(Component.CENTER_ALIGNMENT);
        titleRow.add(titleLbl);
        titleRow.add(Box.createHorizontalGlue());

        // Buttons
        CustomComponents.RoundedButton addBtn        = new CustomComponents.RoundedButton("+ Add Room",    Theme.C_SUCCESS,           new Color(55, 195, 110));
        CustomComponents.RoundedButton editBtn       = new CustomComponents.RoundedButton("✎ Edit",        Theme.C_PRI,               Theme.C_PRI_LIGHT);
        CustomComponents.RoundedButton resBtn        = new CustomComponents.RoundedButton("⚑ Reserved",    Theme.C_WARN,              new Color(250, 205, 50));
        CustomComponents.RoundedButton viewBtn       = new CustomComponents.RoundedButton("▦ Preview",     Theme.C_ACCENT2,           Theme.C_ACCENT2.darker());
        CustomComponents.RoundedButton clearSeatsBtn = new CustomComponents.RoundedButton("↺ Clear Seats", new Color(140, 80, 200),   new Color(160, 100, 220));
        CustomComponents.RoundedButton delBtn        = new CustomComponents.RoundedButton("✕ Remove",      Theme.C_DANGER,            new Color(235, 80, 75));

        Dimension btnSize = new Dimension(120, 36);
        for (CustomComponents.RoundedButton b :
                new CustomComponents.RoundedButton[]{addBtn, editBtn, resBtn, viewBtn, clearSeatsBtn, delBtn}) {
            b.setPreferredSize(btnSize); b.setMaximumSize(btnSize); b.setMinimumSize(btnSize);
            b.setAlignmentY(Component.CENTER_ALIGNMENT);
        }

        addBtn       .addActionListener(e -> showRoomDialog(null));
        editBtn      .addActionListener(e -> editSelected());
        delBtn       .addActionListener(e -> removeSelected());
        resBtn       .addActionListener(e -> toggleReserved());
        viewBtn      .addActionListener(e -> previewRoom());
        clearSeatsBtn.addActionListener(e -> clearSelectedRoomSeats());

        for (CustomComponents.RoundedButton b :
                new CustomComponents.RoundedButton[]{addBtn, editBtn, resBtn, viewBtn, clearSeatsBtn, delBtn}) {
            titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(b);
        }

        top.add(titleRow, BorderLayout.NORTH);

        // ── Search row ───────────────────────────────────────────────────────
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); searchRow.setOpaque(false);
        searchField = new CustomComponents.SearchField("Search rooms…");
        searchField.setPreferredSize(new Dimension(280, 32));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JLabel ico = new JLabel("  ⌕  "); ico.setFont(Theme.F_SUB); ico.setForeground(Theme.C_TEXT2);
        searchRow.add(ico); searchRow.add(searchField);
        top.add(searchRow, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Rows", "Cols", "Capacity", "Reserved", "Invigilator"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new CustomComponents.StyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });
        int[] widths = {60, 200, 55, 55, 75, 75, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table); sp.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);
    }

    public void refresh() { populateTable(DataManager.rooms); }

    private void populateTable(java.util.List<Room> list) {
        model.setRowCount(0);
        for (Room r : list)
            model.addRow(new Object[]{r.id, r.name, r.rows, r.cols, r.capacity(), r.reserved.size(), r.invigilator});
    }

    private void filterTable() {
        String q = searchField.getSearchText().toLowerCase();
        if (q.isEmpty()) { populateTable(DataManager.rooms); return; }
        java.util.List<Room> filtered = new ArrayList<>();
        for (Room r : DataManager.rooms)
            if (r.id.toLowerCase().contains(q) || r.name.toLowerCase().contains(q) || r.invigilator.toLowerCase().contains(q))
                filtered.add(r);
        populateTable(filtered);
    }

    private void showRoomDialog(Room existing) {
        boolean editing = existing != null;
        JDialog dlg = new JDialog(frame, editing ? "Edit Room" : "Add Room", true);
        dlg.setSize(440, 340); dlg.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Theme.C_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12); gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Room ID:", "Room Name:", "Rows:", "Columns:", "Invigilator:"};
        JTextField[] fields = {
            new JTextField(editing ? existing.id   : "", 20),
            new JTextField(editing ? existing.name : "", 20),
            new JTextField(editing ? String.valueOf(existing.rows) : "6", 20),
            new JTextField(editing ? String.valueOf(existing.cols) : "5", 20),
            new JTextField(editing ? existing.invigilator : "", 20)
        };
        if (editing) { fields[0].setEditable(false); fields[0].setForeground(Theme.C_TEXT2); }
        for (JTextField f : fields) f.setFont(Theme.F_BODY);

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel l = new JLabel(labels[i]); l.setFont(Theme.F_BODY); p.add(l, gbc);
            gbc.gridx = 1; gbc.weightx = 1; p.add(fields[i], gbc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Theme.C_BG);
        CustomComponents.RoundedButton save   = new CustomComponents.RoundedButton(editing ? "Update" : "Save", Theme.C_SUCCESS, new Color(55, 195, 110));
        CustomComponents.RoundedButton cancel = new CustomComponents.RoundedButton("Cancel", Theme.C_TEXT2, Theme.C_TEXT);
        save.addActionListener(e -> {
            try {
                String id = fields[0].getText().trim();
                if (id.isEmpty()) { JOptionPane.showMessageDialog(dlg, "ID required!"); return; }
                int rows = Integer.parseInt(fields[2].getText().trim());
                int cols = Integer.parseInt(fields[3].getText().trim());
                if (rows < 1 || rows > 20 || cols < 1 || cols > 20) throw new NumberFormatException();
                if (!editing) {
                    for (Room r : DataManager.rooms) if (r.id.equals(id)) { JOptionPane.showMessageDialog(dlg, "Duplicate ID!"); return; }
                    DataManager.rooms.add(new Room(id, fields[1].getText().trim(), rows, cols, fields[4].getText().trim()));
                } else {
                    Set<String> res = existing.reserved;
                    DataManager.rooms.replaceAll(r -> {
                        if (r.id.equals(id)) {
                            Room nr = new Room(id, fields[1].getText().trim(), rows, cols, fields[4].getText().trim());
                            nr.reserved = res; return nr;
                        }
                        return r;
                    });
                }
                DataManager.generated = false;
                DataManager.log((editing ? "Edited" : "Added") + " room: " + id);
                refresh(); dlg.dispose();
                frame.showToast(editing ? "Room updated" : "Room added", Theme.C_SUCCESS);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Rows/Cols must be 1-20!"); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(save); btns.add(cancel);
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2; p.add(btns, gbc);
        dlg.setContentPane(new JScrollPane(p)); dlg.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a room first", Theme.C_WARN); return; }
        String id = (String) model.getValueAt(row, 0);
        Room rm = DataManager.findRoomById(id);
        if (rm != null) showRoomDialog(rm);
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a room first", Theme.C_WARN); return; }
        String id   = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        long seatCount = DataManager.arrangement.values().stream()
                .filter(sa -> sa.roomId.equals(id)).count();
        String extra = seatCount > 0
                ? "\n\nThis will also remove " + seatCount + " seat allocation(s) for this room." : "";

        int res = JOptionPane.showConfirmDialog(frame,
                "Remove room \"" + name + "\" (" + id + ")?" + extra,
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            DataManager.clearArrangementForRoom(id);
            DataManager.rooms.removeIf(r -> r.id.equals(id));
            DataManager.log("Removed room: " + id + " (and its seat allocations)");
            refresh(); frame.refreshAll();
            frame.showToast("Room \"" + id + "\" removed along with its seating data", Theme.C_DANGER);
        }
    }

    private void clearSelectedRoomSeats() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a room first", Theme.C_WARN); return; }
        String id   = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        long seatCount = DataManager.arrangement.values().stream()
                .filter(sa -> sa.roomId.equals(id)).count();
        if (seatCount == 0) { frame.showToast("No seating data for this room", Theme.C_WARN); return; }

        int res = JOptionPane.showConfirmDialog(frame,
                "Clear " + seatCount + " seat allocation(s) for room \"" + name + "\"?\n"
                + "The room itself will remain.",
                "Confirm Clear Seats", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            DataManager.clearArrangementForRoom(id);
            DataManager.log("Cleared seat allocations for room: " + id);
            frame.refreshAll();
            frame.showToast("Seat allocations cleared for room " + id, Theme.C_WARN);
        }
    }

    private void toggleReserved() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a room first", Theme.C_WARN); return; }
        String id = (String) model.getValueAt(row, 0);
        Room rm = DataManager.findRoomById(id);
        if (rm == null) return;
        String pos = JOptionPane.showInputDialog(frame,
                "Enter seat (row,col) to toggle reserved (1-based):\nRoom: " + rm.name
                        + "  (" + rm.rows + " rows × " + rm.cols + " cols)",
                "Toggle Reserved", JOptionPane.PLAIN_MESSAGE);
        if (pos == null || pos.isBlank()) return;
        try {
            String[] parts = pos.split(",");
            int sr = Integer.parseInt(parts[0].trim()) - 1;
            int sc = Integer.parseInt(parts[1].trim()) - 1;
            if (sr < 0 || sr >= rm.rows || sc < 0 || sc >= rm.cols) throw new Exception();
            String key = sr + "-" + sc;
            if (rm.reserved.contains(key)) rm.reserved.remove(key);
            else rm.reserved.add(key);
            DataManager.generated = false; refresh();
            frame.showToast("Reserved seat toggled", Theme.C_WARN);
        } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid position!"); }
    }

    private void previewRoom() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a room first", Theme.C_WARN); return; }
        String id = (String) model.getValueAt(row, 0);
        Room rm = DataManager.findRoomById(id);
        if (rm == null) return;
        JDialog dlg = new JDialog(frame, "Room Preview: " + rm.name, true);
        SeatChartPanel chart = new SeatChartPanel(rm);
        dlg.setContentPane(new JScrollPane(chart));
        dlg.setSize(Math.min(900, rm.cols * 60 + 120), Math.min(700, rm.rows * 60 + 140));
        dlg.setLocationRelativeTo(frame); dlg.setVisible(true);
    }
}
