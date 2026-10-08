import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DashboardPanel extends JPanel {
    private AppFrame frame;
    private JPanel statsRow;
    private JPanel utilBarsPanel;
    private JList<String> logList;
    private DefaultListModel<String> logModel;
    private JLabel examInfoLbl;

    public DashboardPanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        // ── Top: title + exam info ────────────────────────────────────────
        JPanel topArea = new JPanel(new BorderLayout(0, 4));
        topArea.setOpaque(false);
        topArea.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.F_TITLE);
        title.setForeground(Theme.C_TEXT);
        examInfoLbl = new JLabel();
        examInfoLbl.setFont(Theme.F_BODY);
        examInfoLbl.setForeground(Theme.C_TEXT2);
        topArea.add(title, BorderLayout.NORTH);
        topArea.add(examInfoLbl, BorderLayout.CENTER);
        add(topArea, BorderLayout.NORTH);

        // ── Center: stat cards + room bars + log ─────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        // Stat cards row
        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        center.add(statsRow, BorderLayout.NORTH);

        // Middle: room utilisation + log
        JPanel mid = new JPanel(new GridLayout(1, 2, 16, 0));
        mid.setOpaque(false);

        // Room utilisation card
        CustomComponents.CardPanel utilCard = new CustomComponents.CardPanel();
        utilCard.setLayout(new BorderLayout(0, 8));
        utilCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel utilTitle = new JLabel("Room Utilisation");
        utilTitle.setFont(Theme.F_SUB);
        utilTitle.setForeground(Theme.C_PRI);
        utilCard.add(utilTitle, BorderLayout.NORTH);
        utilBarsPanel = new JPanel();
        utilBarsPanel.setOpaque(false);
        utilBarsPanel.setLayout(new BoxLayout(utilBarsPanel, BoxLayout.Y_AXIS));
        JScrollPane utilScroll = new JScrollPane(utilBarsPanel);
        utilScroll.setBorder(BorderFactory.createEmptyBorder());
        utilScroll.getViewport().setBackground(Theme.C_CARD);
        utilCard.add(utilScroll, BorderLayout.CENTER);
        mid.add(utilCard);

        // Activity log card
        CustomComponents.CardPanel logCard = new CustomComponents.CardPanel();
        logCard.setLayout(new BorderLayout(0, 8));
        logCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel logTitle = new JLabel("Activity Log");
        logTitle.setFont(Theme.F_SUB);
        logTitle.setForeground(Theme.C_PRI);
        logCard.add(logTitle, BorderLayout.NORTH);
        logModel = new DefaultListModel<>();
        logList = new JList<>(logModel);
        logList.setFont(Theme.F_SMALL);
        logList.setForeground(Theme.C_TEXT2);
        logList.setBackground(Theme.C_CARD);
        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.getViewport().setBackground(Theme.C_CARD);
        logCard.add(logScroll, BorderLayout.CENTER);
        mid.add(logCard);

        center.add(mid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ── Bottom: action buttons ────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 28, 20, 28));

        CustomComponents.RoundedButton loadBtn = new CustomComponents.RoundedButton(
                "⤓ Load Sample Data", Theme.C_PRI, Theme.C_PRI_LIGHT);
        loadBtn.setPreferredSize(new Dimension(180, 38));
        loadBtn.addActionListener(e -> {
            DataManager.loadSampleData();
            refresh();
            frame.showToast("Sample data loaded: " + DataManager.students.size() + " students", Theme.C_SUCCESS);
        });

        CustomComponents.RoundedButton editExamBtn = new CustomComponents.RoundedButton(
                "✎ Edit Exam Info", Theme.C_ACCENT2, Theme.C_ACCENT2.darker());
        editExamBtn.setPreferredSize(new Dimension(160, 38));
        editExamBtn.addActionListener(e -> editExamInfo());

        CustomComponents.RoundedButton genBtn = new CustomComponents.RoundedButton(
                "▶ Generate Seating", Theme.C_ACCENT, new Color(245, 110, 50));
        genBtn.setPreferredSize(new Dimension(175, 38));
        genBtn.addActionListener(e -> frame.navigate(3));

        bottom.add(loadBtn);
        bottom.add(editExamBtn);
        bottom.add(genBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        ExamInfo ei = DataManager.examInfo;
        examInfoLbl.setText("<html>" + ei.examName + "  ·  " + ei.date + "  " + ei.time
                + "  ·  " + ei.venue + "  |  Supervisor: " + ei.supervisor + "</html>");

        // Stat cards
        statsRow.removeAll();
        int cap = DataManager.totalCapacity();
        int assigned = DataManager.arrangement.size();
        statsRow.add(new CustomComponents.StatCard("Total Students", DataManager.students.size(), "", Theme.C_PRI));
        statsRow.add(new CustomComponents.StatCard("Total Rooms", DataManager.rooms.size(), "", Theme.C_ACCENT2));
        statsRow.add(new CustomComponents.StatCard("Total Capacity", cap, " seats", Theme.C_SUCCESS));
        statsRow.add(new CustomComponents.StatCard("Assigned Seats",
                DataManager.generated ? assigned : 0,
                DataManager.generated ? "/" + DataManager.students.size() : "",
                DataManager.generated ? Theme.C_ACCENT : Theme.C_WARN));
        statsRow.revalidate();
        statsRow.repaint();

        // Room utilisation bars
        utilBarsPanel.removeAll();
        if (DataManager.rooms.isEmpty()) {
            JLabel nl = new JLabel("No rooms yet");
            nl.setFont(Theme.F_SMALL);
            nl.setForeground(Theme.C_TEXT3);
            utilBarsPanel.add(nl);
        }
        for (Room rm : DataManager.rooms) {
            int used = 0;
            if (DataManager.generated) {
                for (SeatAllocation sa : DataManager.arrangement.values())
                    if (sa.roomId.equals(rm.id))
                        used++;
            }
            utilBarsPanel.add(buildUtilBar(rm.name, used, rm.total(), rm.reserved.size()));
            utilBarsPanel.add(Box.createVerticalStrut(6));
        }
        utilBarsPanel.revalidate();
        utilBarsPanel.repaint();

        // Activity log
        logModel.clear();
        for (String entry : DataManager.activityLog)
            logModel.addElement(entry);

        revalidate();
        repaint();
    }

    private JPanel buildUtilBar(String label, int used, int total, int reserved) {
        JPanel p = new JPanel(new BorderLayout(6, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel lbl = new JLabel(label.length() > 22 ? label.substring(0, 22) + "…" : label);
        lbl.setFont(Theme.F_SMALL);
        lbl.setForeground(Theme.C_TEXT2);
        lbl.setPreferredSize(new Dimension(170, 16));
        int avail = total - reserved;
        JLabel cnt = new JLabel(used + "/" + avail);
        cnt.setFont(new Font("SansSerif", Font.BOLD, 11));
        cnt.setForeground(used > 0 ? Theme.C_SUCCESS : Theme.C_TEXT3);
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(lbl, BorderLayout.WEST);
        topRow.add(cnt, BorderLayout.EAST);
        p.add(topRow, BorderLayout.NORTH);

        double pct = avail > 0 ? (double) used / avail : 0;
        JPanel bar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(210, 222, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int filled = (int) (getWidth() * pct);
                Color barColor = pct > 0.85 ? Theme.C_DANGER : pct > 0.5 ? Theme.C_ACCENT : Theme.C_SUCCESS;
                if (filled > 0) {
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 0, filled, getHeight(), 6, 6);
                }
            }
        };
        bar.setPreferredSize(new Dimension(0, 10));
        bar.setOpaque(false);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    private void editExamInfo() {
        ExamInfo ei = DataManager.examInfo;
        JDialog dlg = new JDialog(frame, "Edit Exam Information", true);
        dlg.setSize(460, 340);
        dlg.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.C_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] lbs = { "Exam Name:", "Date:", "Time:", "Venue:", "Supervisor:", "Institution:" };
        JTextField[] flds = {
                new JTextField(ei.examName, 22), new JTextField(ei.date, 22),
                new JTextField(ei.time, 22), new JTextField(ei.venue, 22),
                new JTextField(ei.supervisor, 22), new JTextField(ei.institution, 22)
        };
        for (JTextField f : flds)
            f.setFont(Theme.F_BODY);
        for (int i = 0; i < lbs.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            JLabel l = new JLabel(lbs[i]);
            l.setFont(Theme.F_BODY);
            p.add(l, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            p.add(flds[i], gbc);
        }
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.C_BG);
        CustomComponents.RoundedButton save = new CustomComponents.RoundedButton("Save", Theme.C_SUCCESS,
                new Color(55, 195, 110));
        CustomComponents.RoundedButton cancel = new CustomComponents.RoundedButton("Cancel", Theme.C_TEXT2,
                Theme.C_TEXT);
        save.addActionListener(e -> {
            ei.examName = flds[0].getText().trim();
            ei.date = flds[1].getText().trim();
            ei.time = flds[2].getText().trim();
            ei.venue = flds[3].getText().trim();
            ei.supervisor = flds[4].getText().trim();
            ei.institution = flds[5].getText().trim();
            refresh();
            dlg.dispose();
        });
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(save);
        btns.add(cancel);
        gbc.gridx = 0;
        gbc.gridy = lbs.length;
        gbc.gridwidth = 2;
        p.add(btns, gbc);
        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
