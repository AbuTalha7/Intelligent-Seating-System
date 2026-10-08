`import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class AppFrame extends JFrame {
    private static final String[] NAV = { "Dashboard", "Students", "Rooms", "Generate", "Seating Chart", "Statistics",
            "Export" };
    private static final Color[] ACCENT = {
            Theme.C_PRI_LIGHT, Theme.C_SUCCESS, Theme.C_ACCENT2, Theme.C_ACCENT,
            Theme.C_WARN, Theme.C_INFO, Theme.C_SUCCESS
    };
    private static final String[] ICON = { "◈", "✦", "⬡", "▶", "▦", "◉", "⤓" };

    private CustomComponents.SidebarButton[] navBtns = new CustomComponents.SidebarButton[NAV.length];
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private RoomPanel roomPanel;
    private GeneratePanel generatePanel;
    private ChartPanel chartPanel;
    private StatisticsPanel statsPanel;
    private ExportPanel exportPanel;

    public AppFrame() {
        super("ExamSeating Pro — Intelligent Seating Arrangement System");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 660));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        buildMenuBar();
        buildSidebar();
        buildContent();
        navigate(0);
    }

    // ── Menu Bar ──────────────────────────────────────────────────────────
    private void buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(Theme.C_PRI_DARK);
        mb.setBorder(BorderFactory.createEmptyBorder());

        // ── File menu ─────────────────────────────────────────────────────
        JMenu file = styledMenu("File");

        JMenuItem newSession = styledItem("New Session", KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        JMenuItem saveSession = styledItem("Save Session", KeyEvent.VK_S, ActionEvent.CTRL_MASK);
        JMenuItem loadSession = styledItem("Open Session", KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        JMenuItem sampleData = styledItem("Load Sample Data", -1, 0);
        JMenuItem exit = styledItem("Exit", KeyEvent.VK_Q, ActionEvent.CTRL_MASK);

        newSession.addActionListener(e -> newSession());
        saveSession.addActionListener(e -> saveSession());
        loadSession.addActionListener(e -> loadSession());
        sampleData.addActionListener(e -> {
            DataManager.loadSampleData();
            refreshAll();
            showToast("Sample data loaded: " + DataManager.students.size() + " students", Theme.C_SUCCESS);
        });
        exit.addActionListener(e -> System.exit(0));

        // ── Clear sub-menu ────────────────────────────────────────────────
        JMenu clearMenu = new JMenu("Clear Data");
        clearMenu.setFont(Theme.F_BODY);

        JMenuItem clearArrangement = styledItem("Clear Seating Arrangement", -1, 0);
        JMenuItem clearStudents = styledItem("Clear All Students", -1, 0);
        JMenuItem clearRooms = styledItem("Clear All Rooms", -1, 0);
        JMenuItem clearAll = styledItem("Clear Everything (Students + Rooms + Arrangement)", -1, 0);

        clearArrangement.addActionListener(e -> doClearArrangement());
        clearStudents.addActionListener(e -> doClearStudents());
        clearRooms.addActionListener(e -> doClearRooms());
        clearAll.addActionListener(e -> doClearAll());

        clearMenu.add(clearArrangement);
        clearMenu.addSeparator();
        clearMenu.add(clearStudents);
        clearMenu.add(clearRooms);
        clearMenu.addSeparator();
        clearMenu.add(clearAll);

        file.add(newSession);
        file.add(saveSession);
        file.add(loadSession);
        file.addSeparator();
        file.add(sampleData);
        file.addSeparator();
        file.add(clearMenu);
        file.addSeparator();
        file.add(exit);

        // ── View menu ─────────────────────────────────────────────────────
        JMenu view = styledMenu("View");
        for (int i = 0; i < NAV.length; i++) {
            JMenuItem mi = styledItem(NAV[i], -1, 0);
            final int idx = i;
            mi.addActionListener(e -> navigate(idx));
            view.add(mi);
        }

        // ── Help menu ─────────────────────────────────────────────────────
        JMenu help = styledMenu("Help");
        JMenuItem about = styledItem("About", -1, 0);
        about.addActionListener(e -> showAbout());
        help.add(about);

        mb.add(file);
        mb.add(view);
        mb.add(help);
        setJMenuBar(mb);
    }

    private JMenu styledMenu(String title) {
        JMenu m = new JMenu(title);
        m.setForeground(new Color(190, 205, 225));
        m.setFont(Theme.F_BODY);
        return m;
    }

    private JMenuItem styledItem(String title, int key, int mod) {
        JMenuItem mi = new JMenuItem(title);
        mi.setFont(Theme.F_BODY);
        if (key > 0)
            mi.setAccelerator(KeyStroke.getKeyStroke(key, mod));
        return mi;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────
    private void buildSidebar() {
        JPanel sidebar = new CustomComponents.GradientPanel(Theme.C_PRI_DARK, new Color(16, 24, 42), true);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        logo.setOpaque(false);
        JLabel logoIcon = new JLabel("◈");
        logoIcon.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoIcon.setForeground(Theme.C_PRI_LIGHT);
        JPanel logoText = new JPanel(new GridLayout(2, 1, 0, 0));
        logoText.setOpaque(false);
        JLabel ln1 = new JLabel("ExamSeating Pro");
        ln1.setFont(Theme.F_SUB);
        ln1.setForeground(Color.WHITE);
        JLabel ln2 = new JLabel("Seating Arrangement System");
        ln2.setFont(Theme.F_SMALL);
        ln2.setForeground(new Color(100, 125, 165));
        logoText.add(ln1);
        logoText.add(ln2);
        logo.add(logoIcon);
        logo.add(logoText);
        JPanel divider = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 18));
                g.fillRect(12, 0, getWidth() - 24, 1);
            }
        };
        divider.setPreferredSize(new Dimension(230, 1));
        divider.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(logo, BorderLayout.CENTER);
        topBar.add(divider, BorderLayout.SOUTH);
        sidebar.add(topBar, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.add(Box.createVerticalStrut(10));
        for (int i = 0; i < NAV.length; i++) {
            navBtns[i] = new CustomComponents.SidebarButton(ICON[i] + "  " + NAV[i], ACCENT[i]);
            navBtns[i].setMaximumSize(new Dimension(230, 46));
            navBtns[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            final int idx = i;
            navBtns[i].addActionListener(e -> navigate(idx));
            nav.add(navBtns[i]);
            if (i == 2 || i == 4)
                nav.add(Box.createVerticalStrut(6));
        }
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
        JLabel footLbl = new JLabel("DSA Project © 2026");
        footLbl.setFont(Theme.F_SMALL);
        footLbl.setForeground(new Color(80, 100, 130));
        foot.add(footLbl, BorderLayout.SOUTH);
        sidebar.add(foot, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    // ── Content ───────────────────────────────────────────────────────────
    private void buildContent() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(Theme.C_BG);

        dashboardPanel = new DashboardPanel(this);
        studentPanel = new StudentPanel(this);
        roomPanel = new RoomPanel(this);
        generatePanel = new GeneratePanel(this);
        chartPanel = new ChartPanel(this);
        statsPanel = new StatisticsPanel();
        exportPanel = new ExportPanel(this);

        contentPanel.add(dashboardPanel, NAV[0]);
        contentPanel.add(studentPanel, NAV[1]);
        contentPanel.add(roomPanel, NAV[2]);
        contentPanel.add(generatePanel, NAV[3]);
        contentPanel.add(chartPanel, NAV[4]);
        contentPanel.add(statsPanel, NAV[5]);
        contentPanel.add(exportPanel, NAV[6]);
        add(contentPanel, BorderLayout.CENTER);
    }

    // ── Navigation ─────────────────────────────────────────────────────────
    public void navigate(int idx) {
        cardLayout.show(contentPanel, NAV[idx]);
        for (int i = 0; i < NAV.length; i++)
            navBtns[i].setActive(i == idx);
        switch (idx) {
            case 0:
                dashboardPanel.refresh();
                break;
            case 1:
                studentPanel.refresh();
                break;
            case 2:
                roomPanel.refresh();
                break;
            case 4:
                chartPanel.refresh();
                break;
            case 5:
                statsPanel.refresh();
                break;
            case 6:
                exportPanel.refresh();
                break;
        }
    }

    public void refreshAll() {
        dashboardPanel.refresh();
        studentPanel.refresh();
        roomPanel.refresh();
        chartPanel.refresh();
        statsPanel.refresh();
        exportPanel.refresh();
    }

    // ── Toast helper ───────────────────────────────────────────────────────
    public void showToast(String msg, Color color) {
        CustomComponents.Toast.show(this, msg, color);
    }

    // ── Clear data actions ─────────────────────────────────────────────────

    /** Clear only the seating arrangement; keep students and rooms. */
    public void doClearArrangement() {
        if (DataManager.arrangement.isEmpty()) {
            showToast("No seating arrangement to clear", Theme.C_WARN);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Clear the entire seating arrangement?\n"
                        + DataManager.arrangement.size() + " seat allocation(s) will be removed.\n\n"
                        + "Students and rooms will NOT be affected.",
                "Clear Seating Arrangement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("Seating arrangement cleared");
            refreshAll();
            showToast("Seating arrangement cleared", Theme.C_WARN);
        }
    }

    /** Remove all students (and the arrangement, since it would be invalid). */
    public void doClearStudents() {
        if (DataManager.students.isEmpty()) {
            showToast("No students to clear", Theme.C_WARN);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Remove all " + DataManager.students.size() + " students?\n\n"
                        + "The seating arrangement will also be cleared.\nRooms will NOT be affected.",
                "Clear All Students", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.students.clear();
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("All students cleared");
            refreshAll();
            showToast("All students cleared", Theme.C_DANGER);
        }
    }

    /** Remove all rooms (and the arrangement, since it would be invalid). */
    public void doClearRooms() {
        if (DataManager.rooms.isEmpty()) {
            showToast("No rooms to clear", Theme.C_WARN);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Remove all " + DataManager.rooms.size() + " rooms?\n\n"
                        + "The seating arrangement will also be cleared.\nStudents will NOT be affected.",
                "Clear All Rooms", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.rooms.clear();
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("All rooms cleared");
            refreshAll();
            showToast("All rooms cleared", Theme.C_DANGER);
        }
    }

    /** Wipe everything — students, rooms, and seating arrangement. */
    public void doClearAll() {
        if (DataManager.students.isEmpty() && DataManager.rooms.isEmpty()) {
            showToast("Nothing to clear", Theme.C_WARN);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Remove ALL data?\n\n"
                        + "  • " + DataManager.students.size() + " student(s)\n"
                        + "  • " + DataManager.rooms.size() + " room(s)\n"
                        + "  • " + DataManager.arrangement.size() + " seat allocation(s)\n\n"
                        + "This cannot be undone.",
                "Clear Everything", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.students.clear();
            DataManager.rooms.clear();
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("All data cleared");
            refreshAll();
            showToast("All data cleared", Theme.C_DANGER);
        }
    }

    // ── File session helpers ───────────────────────────────────────────────
    private void newSession() {
        int r = JOptionPane.showConfirmDialog(this, "Clear all data and start a new session?",
                "New Session", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.students.clear();
            DataManager.rooms.clear();
            DataManager.arrangement.clear();
            DataManager.generated = false;
            DataManager.log("New session started");
            refreshAll();
            showToast("New session started", Theme.C_INFO);
        }
    }

    private void saveSession() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("exam_session.ess"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.saveToFile(fc.getSelectedFile());
                showToast("Session saved", Theme.C_SUCCESS);
            } catch (Exception ex) {
                showToast("Save failed: " + ex.getMessage(), Theme.C_DANGER);
            }
        }
    }

    private void loadSession() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.loadFromFile(fc.getSelectedFile());
                refreshAll();
                showToast("Session loaded", Theme.C_SUCCESS);
            } catch (Exception ex) {
                showToast("Load failed: " + ex.getMessage(), Theme.C_DANGER);
            }
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "<html><b>ExamSeating Pro v2.0</b><br><br>" +
                        "An intelligent exam seating arrangement system.<br>" +
                        "Uses greedy graph-coloring with backtracking.<br><br>" +
                        "<i>DSA Course Project · 2025</i></html>",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
