import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GeneratePanel extends JPanel {
    private AppFrame frame;
    private JLabel genStatus;
    private JProgressBar progBar;
    private JLabel unassignedLbl;
    private JSpinner separationSpinner;
    private ConstraintManager cm = new ConstraintManager();

    public GeneratePanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Generate Seating Arrangement");
        title.setFont(Theme.F_TITLE);
        title.setForeground(Theme.C_TEXT);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // ── Algorithm info card ────────────────────────────────────────────
        CustomComponents.CardPanel algoCard = new CustomComponents.CardPanel();
        algoCard.setLayout(new BorderLayout(12, 12));
        algoCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        algoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        algoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel algoTitle = new JLabel("Algorithm Pipeline");
        algoTitle.setFont(Theme.F_HEAD);
        algoTitle.setForeground(Theme.C_PRI);
        algoCard.add(algoTitle, BorderLayout.NORTH);

        JPanel steps = new JPanel(new GridLayout(1, 3, 16, 0));
        steps.setOpaque(false);
        steps.add(stepPanel("1. Conflict Graph",
                "Build adjacency map: students from the same class-section are in conflict", Theme.C_PRI_LIGHT));
        steps.add(stepPanel("2. Greedy Assignment",
                "Most-constrained students placed first; special needs in front rows", Theme.C_SUCCESS));
        steps.add(stepPanel("3. Backtracking", "Displaces seated students when no direct placement is found",
                Theme.C_ACCENT));
        algoCard.add(steps, BorderLayout.CENTER);
        center.add(algoCard);
        center.add(Box.createVerticalStrut(14));

        // ── Settings card ──────────────────────────────────────────────────
        CustomComponents.CardPanel settCard = new CustomComponents.CardPanel();
        settCard.setLayout(new GridBagLayout());
        settCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        settCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        settCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);

        JLabel settTitle = new JLabel("Settings");
        settTitle.setFont(Theme.F_HEAD);
        settTitle.setForeground(Theme.C_PRI);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        settCard.add(settTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        settCard.add(label("Separation distance (seats):"), gbc);
        separationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
        separationSpinner.setFont(Theme.F_BODY);
        separationSpinner.setPreferredSize(new Dimension(60, 28));
        gbc.gridx = 1;
        settCard.add(separationSpinner, gbc);
        gbc.gridx = 2;
        settCard.add(label("   (min distance between conflicting students)"), gbc);
        center.add(settCard);
        center.add(Box.createVerticalStrut(14));

        // ── Status card ────────────────────────────────────────────────────
        CustomComponents.CardPanel statCard = new CustomComponents.CardPanel();
        statCard.setLayout(new BorderLayout(8, 10));
        statCard.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        statCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        genStatus = new JLabel("Ready — configure settings and click Generate");
        genStatus.setFont(Theme.F_BODY);
        genStatus.setForeground(Theme.C_TEXT);
        statCard.add(genStatus, BorderLayout.NORTH);

        progBar = new JProgressBar(0, 100);
        progBar.setStringPainted(true);
        progBar.setFont(Theme.F_SMALL);
        progBar.setForeground(Theme.C_SUCCESS);
        progBar.setBackground(new Color(235, 240, 245));
        progBar.setPreferredSize(new Dimension(400, 22));
        statCard.add(progBar, BorderLayout.CENTER);

        unassignedLbl = new JLabel(" ");
        unassignedLbl.setFont(Theme.F_SMALL);
        unassignedLbl.setForeground(Theme.C_DANGER);
        statCard.add(unassignedLbl, BorderLayout.SOUTH);
        center.add(statCard);
        center.add(Box.createVerticalStrut(18));

        // ── Generate button ────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        CustomComponents.RoundedButton genBtn = new CustomComponents.RoundedButton(
                "▶  Generate Arrangement", Theme.C_ACCENT, new Color(245, 115, 55));
        genBtn.setPreferredSize(new Dimension(240, 46));
        genBtn.addActionListener(e -> generateArrangement(genBtn));

        CustomComponents.RoundedButton clearBtn = new CustomComponents.RoundedButton(
                "↺  Clear", Theme.C_TEXT2, Theme.C_TEXT);
        clearBtn.setPreferredSize(new Dimension(100, 46));
        clearBtn.addActionListener(e -> {
            DataManager.arrangement.clear();
            DataManager.generated = false;
            genStatus.setText("Arrangement cleared. Ready to generate.");
            progBar.setValue(0);
            unassignedLbl.setText(" ");
            frame.showToast("Arrangement cleared", Theme.C_WARN);
        });
        btnRow.add(genBtn);
        btnRow.add(Box.createHorizontalStrut(12));
        btnRow.add(clearBtn);
        center.add(btnRow);

        add(center, BorderLayout.CENTER);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.F_BODY);
        return l;
    }

    private JPanel stepPanel(String title, String desc, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new CustomComponents.RoundedLineBorder(
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80), 1, 8),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(Theme.F_SUB);
        t.setForeground(accent);
        JLabel d = new JLabel("<html><small>" + desc + "</small></html>");
        d.setFont(Theme.F_SMALL);
        d.setForeground(Theme.C_TEXT2);
        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        return p;
    }

    private void generateArrangement(CustomComponents.RoundedButton genBtn) {
        if (DataManager.students.isEmpty()) {
            frame.showToast("Add students first!", Theme.C_WARN);
            return;
        }
        if (DataManager.rooms.isEmpty()) {
            frame.showToast("Add rooms first!", Theme.C_WARN);
            return;
        }
        int cap = DataManager.totalCapacity();
        if (DataManager.students.size() > cap) {
            frame.showToast("Not enough capacity! " + DataManager.students.size()
                    + " students, " + cap + " seats", Theme.C_DANGER);
            return;
        }

        genBtn.setEnabled(false);
        int sep = (Integer) separationSpinner.getValue();
        cm.setSeparationDistance(sep);

        String[] msgs = {
                "Building conflict graph…",
                "Sorting students by constraint degree…",
                "Placing special-needs students…",
                "Running greedy allocator…",
                "Applying backtracking…",
                "Finalising arrangement…"
        };
        int[] pcts = { 10, 25, 40, 60, 80, 95 };
        final int[] step = { 0 };

        javax.swing.Timer timer = new javax.swing.Timer(280, null);
        timer.addActionListener(ev -> {
            if (step[0] < msgs.length) {
                genStatus.setText(msgs[step[0]]);
                progBar.setValue(pcts[step[0]]);
                step[0]++;
            } else {
                GreedyAllocator ga = new GreedyAllocator(cm);
                DataManager.arrangement = ga.allocate(DataManager.students, DataManager.rooms);
                DataManager.generated = true;
                int assigned = DataManager.arrangement.size();
                int total = DataManager.students.size();
                genStatus.setText("✓ Complete!  " + assigned + " / " + total + " students assigned");
                progBar.setValue(100);

                java.util.List<String> unassigned = ga.getUnassigned();
                if (unassigned.isEmpty()) {
                    unassignedLbl.setText("All students successfully assigned.");
                    unassignedLbl.setForeground(Theme.C_SUCCESS);
                } else {
                    unassignedLbl.setText("⚠ Unassigned (" + unassigned.size() + "): " + String.join(", ", unassigned));
                    unassignedLbl.setForeground(Theme.C_DANGER);
                }
                DataManager.log("Generated: " + assigned + "/" + total + " assigned (sep=" + sep + ")");
                timer.stop();
                genBtn.setEnabled(true);
                frame.showToast("Arrangement generated: " + assigned + "/" + total + " assigned", Theme.C_SUCCESS);
            }
        });
        timer.start();
    }
}
