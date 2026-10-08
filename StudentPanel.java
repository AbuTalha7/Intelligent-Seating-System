import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class StudentPanel extends JPanel {
    private AppFrame frame;
    private DefaultTableModel model;
    private JTable table;
    private CustomComponents.SearchField searchField;

    // Section → Subject Group mapping
    private static final Map<String, String[]> SECTION_SUBJECTS = new LinkedHashMap<>();
    static {
        SECTION_SUBJECTS.put("A", new String[]{
            "Programming & OOP", "Thermodynamics", "Circuit Analysis", "Structural Analysis"
        });
        SECTION_SUBJECTS.put("B", new String[]{
            "Data Structures", "Mechanics", "Digital Signals"
        });
        SECTION_SUBJECTS.put("C", new String[]{
            "Algorithms", "Database Systems", "Computer Networks"
        });
    }

    public StudentPanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0)); setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header (title row + search row) ─────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false);

        // Title row: use BoxLayout so title and buttons never overlap
        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

        JLabel titleLbl = new JLabel("Student Management");
        titleLbl.setFont(Theme.F_TITLE);
        titleLbl.setForeground(Theme.C_TEXT);
        titleLbl.setAlignmentY(Component.CENTER_ALIGNMENT);
        titleRow.add(titleLbl);
        titleRow.add(Box.createHorizontalGlue()); // push buttons to the right

        // Buttons
        CustomComponents.RoundedButton impXlsBtn = new CustomComponents.RoundedButton("⤓ Excel",    Theme.C_INFO,    Theme.C_INFO.darker());
        CustomComponents.RoundedButton impBtn    = new CustomComponents.RoundedButton("⤓ CSV",      Theme.C_ACCENT2, Theme.C_ACCENT2.darker());
        CustomComponents.RoundedButton addBtn    = new CustomComponents.RoundedButton("+ Add",      Theme.C_SUCCESS, new Color(55, 195, 110));
        CustomComponents.RoundedButton editBtn   = new CustomComponents.RoundedButton("✎ Edit",     Theme.C_PRI,     Theme.C_PRI_LIGHT);
        CustomComponents.RoundedButton delBtn    = new CustomComponents.RoundedButton("✕ Remove",   Theme.C_DANGER,  new Color(235, 80, 75));

        Dimension btnSize = new Dimension(110, 36);
        for (CustomComponents.RoundedButton b : new CustomComponents.RoundedButton[]{impXlsBtn, impBtn, addBtn, editBtn, delBtn}) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setMinimumSize(btnSize);
            b.setAlignmentY(Component.CENTER_ALIGNMENT);
        }

        impXlsBtn.addActionListener(e -> showExcelImportDialog());
        impBtn   .addActionListener(e -> showImportDialog());
        addBtn   .addActionListener(e -> showAddDialog(null));
        editBtn  .addActionListener(e -> editSelected());
        delBtn   .addActionListener(e -> removeSelected());

        titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(impXlsBtn);
        titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(impBtn);
        titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(addBtn);
        titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(editBtn);
        titleRow.add(Box.createHorizontalStrut(8)); titleRow.add(delBtn);

        top.add(titleRow, BorderLayout.NORTH);

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); searchRow.setOpaque(false);
        searchField = new CustomComponents.SearchField("Search by name, ID, class, section…");
        searchField.setPreferredSize(new Dimension(340, 32));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JLabel searchIcon = new JLabel("  ⌕  "); searchIcon.setFont(Theme.F_SUB); searchIcon.setForeground(Theme.C_TEXT2);
        searchRow.add(searchIcon); searchRow.add(searchField);
        top.add(searchRow, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Class", "Section", "Subject Group", "Special"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new CustomComponents.StyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        int[] widths = {70, 180, 80, 80, 200, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table); sp.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        // Status bar
        JLabel statusLbl = new JLabel(" "); statusLbl.setFont(Theme.F_SMALL); statusLbl.setForeground(Theme.C_TEXT2);
        statusLbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        add(statusLbl, BorderLayout.SOUTH);
        model.addTableModelListener(e -> statusLbl.setText(
            "  Showing " + model.getRowCount() + " of " + DataManager.students.size() + " students"));
    }

    public void refresh() {
        populateTable(DataManager.students);
    }

    private void populateTable(java.util.List<Student> list) {
        model.setRowCount(0);
        for (Student s : list)
            model.addRow(new Object[]{s.id, s.name, s.className, s.section, s.subjectGroup,
                    s.specialNeeds ? "Yes" : "No"});
    }

    private void filterTable() {
        String q = searchField.getSearchText().toLowerCase();
        if (q.isEmpty()) { populateTable(DataManager.students); return; }
        java.util.List<Student> filtered = new ArrayList<>();
        for (Student s : DataManager.students) {
            if (s.id.toLowerCase().contains(q) || s.name.toLowerCase().contains(q) ||
                s.className.toLowerCase().contains(q) || s.section.toLowerCase().contains(q) ||
                s.subjectGroup.toLowerCase().contains(q))
                filtered.add(s);
        }
        populateTable(filtered);
    }

    private void showAddDialog(Student existing) {
        boolean editing = existing != null;
        JDialog dlg = new JDialog(frame, editing ? "Edit Student" : "Add Student", true);
        dlg.setSize(460, 360); dlg.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Theme.C_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12); gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── ID field (numeric only) ──────────────────────────────────────────
        JTextField idField = new JTextField(22); idField.setFont(Theme.F_BODY);
        ((AbstractDocument) idField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text != null && text.matches("\\d+")) super.insertString(fb, offset, text, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text != null && text.matches("\\d*")) super.replace(fb, offset, length, text, attrs);
            }
        });

        JTextField nameField = new JTextField(22); nameField.setFont(Theme.F_BODY);
        JComboBox<String> classField = new JComboBox<>(new String[]{
            "BSE 1","BSE 2","BSE 3","BSE 4","BSE 5","BSE 6","BSE 7","BSE 8"
        });
        classField.setFont(Theme.F_BODY);

        JComboBox<String> sectionCombo = new JComboBox<>(new String[]{"A", "B", "C"});
        sectionCombo.setFont(Theme.F_BODY);

        JComboBox<String> subjectCombo = new JComboBox<>();
        subjectCombo.setFont(Theme.F_BODY);
        // Populate subjects based on section
        Runnable updateSubjects = () -> {
            String sec = (String) sectionCombo.getSelectedItem();
            subjectCombo.removeAllItems();
            if (sec != null) {
                String[] subs = SECTION_SUBJECTS.get(sec);
                if (subs != null) for (String s : subs) subjectCombo.addItem(s);
            }
        };
        sectionCombo.addActionListener(e -> updateSubjects.run());
        updateSubjects.run();

        JCheckBox snCheck = new JCheckBox("Special Needs");
        snCheck.setFont(Theme.F_BODY); snCheck.setOpaque(false);

        // Pre-fill for edit
        if (editing) {
            idField.setText(existing.id); idField.setEditable(false);
            nameField.setText(existing.name);
            classField.setSelectedItem(existing.className);
            sectionCombo.setSelectedItem(existing.section);
            updateSubjects.run();
            subjectCombo.setSelectedItem(existing.subjectGroup);
            snCheck.setSelected(existing.specialNeeds);
        }

        String[] rowLabels = {"ID *", "Name", "Class", "Section", "Subject Group", "Special Needs"};
        Component[] rowFields = {idField, nameField, classField, sectionCombo, subjectCombo, snCheck};
        for (int i = 0; i < rowLabels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1; gbc.weightx = 0;
            JLabel lbl = new JLabel(rowLabels[i]); lbl.setFont(Theme.F_BODY);
            p.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            p.add(rowFields[i], gbc);
        }

        // ── Buttons ──────────────────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Theme.C_BG);
        CustomComponents.RoundedButton save = new CustomComponents.RoundedButton(editing ? "Update" : "Save",
                Theme.C_SUCCESS, new Color(55, 195, 110));
        CustomComponents.RoundedButton cancel = new CustomComponents.RoundedButton("Cancel", Theme.C_TEXT2, Theme.C_TEXT);
        save.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) { JOptionPane.showMessageDialog(dlg, "ID is required!"); return; }
            if (!editing) {
                for (Student s : DataManager.students)
                    if (s.id.equals(id)) { JOptionPane.showMessageDialog(dlg, "Duplicate ID!"); return; }
            }
            String section = (String) sectionCombo.getSelectedItem();
            String subject = subjectCombo.getSelectedItem() != null
                    ? (String) subjectCombo.getSelectedItem() : "";
            String className = (String) classField.getSelectedItem();
            Student ns = new Student(id, nameField.getText().trim(), "",
                    className != null ? className : "", section, subject, snCheck.isSelected());
            if (editing) DataManager.students.replaceAll(s -> s.id.equals(id) ? ns : s);
            else DataManager.students.add(ns);
            DataManager.generated = false;
            DataManager.log((editing ? "Edited" : "Added") + " student: " + id + " " + nameField.getText().trim());
            refresh(); dlg.dispose();
            frame.showToast((editing ? "Student updated" : "Student added"), Theme.C_SUCCESS);
        });
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(save); btns.add(cancel);
        gbc.gridx = 0; gbc.gridy = rowLabels.length; gbc.gridwidth = 2; p.add(btns, gbc);
        dlg.setContentPane(new JScrollPane(p)); dlg.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a student first", Theme.C_WARN); return; }
        String id = (String) model.getValueAt(row, 0);
        Student s = DataManager.findStudentById(id);
        if (s != null) showAddDialog(s);
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { frame.showToast("Select a student first", Theme.C_WARN); return; }
        String id = (String) model.getValueAt(row, 0);
        int res = JOptionPane.showConfirmDialog(frame, "Remove student " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            DataManager.students.removeIf(s -> s.id.equals(id));
            DataManager.generated = false;
            DataManager.log("Removed student: " + id);
            refresh(); frame.showToast("Student removed", Theme.C_DANGER);
        }
    }

    // ── CSV Import ───────────────────────────────────────────────────────────
    private void showImportDialog() {
        JDialog dlg = new JDialog(frame, "Import Students from CSV", true);
        dlg.setSize(520, 420); dlg.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new BorderLayout(8, 8)); p.setBackground(Theme.C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel info = new JLabel("<html><b>Paste CSV rows</b> (one per line):<br>" +
                "<i>ID(numeric), Name, Class, Section(A/B/C), SubjectGroup, SpecialNeeds(true/false)</i></html>");
        info.setFont(Theme.F_SMALL); info.setForeground(Theme.C_TEXT2);
        JTextArea ta = new JTextArea("1001,Alice Smith,BSE 1,A,Programming & OOP,false\n");
        ta.setFont(Theme.F_MONO); ta.setRows(10);
        JScrollPane sp = new JScrollPane(ta);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Theme.C_BG);
        CustomComponents.RoundedButton imp    = new CustomComponents.RoundedButton("Import", Theme.C_SUCCESS, new Color(55, 195, 110));
        CustomComponents.RoundedButton cancel = new CustomComponents.RoundedButton("Cancel", Theme.C_TEXT2, Theme.C_TEXT);
        imp.addActionListener(e -> {
            int[] result = importCsvLines(ta.getText().split("\n"));
            DataManager.generated = false;
            DataManager.log("CSV import: " + result[0] + " added, " + result[1] + " skipped");
            refresh(); dlg.dispose();
            frame.showToast(result[0] + " students imported" + (result[1] > 0 ? ", " + result[1] + " skipped" : ""), Theme.C_SUCCESS);
        });
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(imp); btns.add(cancel);
        p.add(info, BorderLayout.NORTH); p.add(sp, BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
        dlg.setContentPane(p); dlg.setVisible(true);
    }

    /** Parse CSV lines; returns {added, skipped}. */
    private int[] importCsvLines(String[] lines) {
        int count = 0, skip = 0;
        for (String line : lines) {
            line = line.trim(); if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 6) { skip++; continue; }
            String id = parts[0].trim();
            if (!id.matches("\\d+")) { skip++; continue; }
            boolean dup = false;
            for (Student s : DataManager.students) if (s.id.equals(id)) { dup = true; break; }
            if (dup) { skip++; continue; }
            try {
                DataManager.students.add(new Student(id, parts[1].trim(), "",
                        parts[2].trim(), parts[3].trim(), parts[4].trim(),
                        Boolean.parseBoolean(parts[5].trim())));
                count++;
            } catch (Exception ex) { skip++; }
        }
        return new int[]{count, skip};
    }

    // ── Excel (.xlsx) Import ─────────────────────────────────────────────────
    private void showExcelImportDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Excel File (.xlsx)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File xlsxFile = fc.getSelectedFile();

        // Preview dialog
        JDialog dlg = new JDialog(frame, "Import from Excel: " + xlsxFile.getName(), true);
        dlg.setSize(680, 520); dlg.setLocationRelativeTo(frame);
        JPanel outer = new JPanel(new BorderLayout(10, 10)); outer.setBackground(Theme.C_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(0, 6)); infoPanel.setOpaque(false);
        JLabel infoLbl = new JLabel(
            "<html><b>Excel Import</b> — file: <i>" + xlsxFile.getName() + "</i><br>" +
            "Expected columns: <b>A</b>=ID, <b>B</b>=Name, <b>C</b>=Class, " +
            "<b>D</b>=Section, <b>E</b>=Subject Group, <b>F</b>=Special Needs (true/false)<br>" +
            "Row 1 is treated as a header and skipped automatically.</html>");
        infoLbl.setFont(Theme.F_SMALL); infoLbl.setForeground(Theme.C_TEXT2);
        infoPanel.add(infoLbl, BorderLayout.CENTER);
        outer.add(infoPanel, BorderLayout.NORTH);

        // Preview table
        DefaultTableModel previewModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Class", "Section", "Subject Group", "Special", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable previewTable = new CustomComponents.StyledTable(previewModel);
        JScrollPane previewScroll = new JScrollPane(previewTable);
        outer.add(previewScroll, BorderLayout.CENTER);

        // Load preview
        JLabel statusLbl = new JLabel("Loading…"); statusLbl.setFont(Theme.F_SMALL); statusLbl.setForeground(Theme.C_TEXT2);
        outer.add(statusLbl, BorderLayout.SOUTH);

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btnPanel.setBackground(Theme.C_BG);
        CustomComponents.RoundedButton importAllBtn = new CustomComponents.RoundedButton("Import All Valid", Theme.C_SUCCESS, new Color(55, 195, 110));
        CustomComponents.RoundedButton cancelBtn    = new CustomComponents.RoundedButton("Cancel",           Theme.C_TEXT2,  Theme.C_TEXT);
        importAllBtn.setEnabled(false);
        btnPanel.add(importAllBtn); btnPanel.add(cancelBtn);

        JPanel southPanel = new JPanel(new BorderLayout(0, 6)); southPanel.setOpaque(false);
        southPanel.add(statusLbl, BorderLayout.NORTH);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        outer.add(southPanel, BorderLayout.SOUTH);

        dlg.setContentPane(outer);

        // Parse in background
        final java.util.List<String[]> parsed = new ArrayList<>();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errorMsg = null;
            protected Void doInBackground() {
                try {
                    java.util.List<String[]> rows = ExcelImporter.readXlsx(xlsxFile);
                    boolean firstRow = true;
                    for (String[] row : rows) {
                        if (firstRow) { firstRow = false; continue; } // skip header
                        if (row.length < 6) continue;
                        String id   = row[0].trim();
                        String name = row[1].trim();
                        String cls  = row[2].trim();
                        String sec  = row[3].trim();
                        String subj = row[4].trim();
                        String sn   = row[5].trim();
                        if (id.isEmpty() && name.isEmpty()) continue; // blank row

                        String status;
                        if (!id.matches("\\d+"))         status = "✕ ID must be numeric";
                        else if (name.isEmpty())          status = "✕ Name is empty";
                        else if (!sec.matches("[ABC]"))   status = "✕ Section must be A, B or C";
                        else {
                            boolean dup = false;
                            for (Student s : DataManager.students) if (s.id.equals(id)) { dup = true; break; }
                            status = dup ? "⚠ Duplicate ID (skip)" : "✓ OK";
                        }
                        parsed.add(new String[]{id, name, cls, sec, subj, sn, status});
                    }
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                }
                return null;
            }
            protected void done() {
                if (errorMsg != null) {
                    statusLbl.setText("Error reading file: " + errorMsg);
                    statusLbl.setForeground(Theme.C_DANGER);
                    return;
                }
                long valid = parsed.stream().filter(r -> r[6].startsWith("✓")).count();
                for (String[] row : parsed)
                    previewModel.addRow(row);
                statusLbl.setText("  Found " + parsed.size() + " data row(s) — " + valid + " valid, ready to import.");
                statusLbl.setForeground(valid > 0 ? Theme.C_SUCCESS : Theme.C_WARN);
                importAllBtn.setEnabled(valid > 0);
            }
        };
        worker.execute();

        importAllBtn.addActionListener(e -> {
            int count = 0;
            for (String[] row : parsed) {
                if (!row[6].startsWith("✓")) continue;
                try {
                    boolean sn = row[5].equalsIgnoreCase("true") || row[5].equalsIgnoreCase("yes") || row[5].equals("1");
                    DataManager.students.add(new Student(row[0], row[1], "", row[2], row[3], row[4], sn));
                    count++;
                } catch (Exception ex) { /* skip */ }
            }
            DataManager.generated = false;
            DataManager.log("Excel import: " + count + " students added from " + xlsxFile.getName());
            refresh(); dlg.dispose();
            frame.showToast(count + " students imported from Excel", Theme.C_SUCCESS);
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.setVisible(true);
    }
}
