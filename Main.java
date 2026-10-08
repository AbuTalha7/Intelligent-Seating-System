import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Splash
        JWindow splash = new JWindow();
        JPanel splashPanel = new CustomComponents.GradientPanel(Theme.C_PRI_DARK, Theme.C_PRI, true) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                g2.drawString("ExamSeating Pro", 50, 110);
                g2.setFont(Theme.F_BODY); g2.setColor(new Color(180, 200, 230));
                g2.drawString("Intelligent Exam Seating Arrangement System", 50, 138);
                g2.setFont(Theme.F_SMALL); g2.setColor(new Color(100, 130, 170));
                g2.drawString("Version 2.0  ·  © 2025 University Tools", 50, 200);
            }
        };
        splashPanel.setPreferredSize(new Dimension(500, 240));
        splash.setContentPane(splashPanel);
        splash.pack();
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        Timer t = new Timer(1600, e -> {
            splash.dispose();
            SwingUtilities.invokeLater(() -> {
                AppFrame app = new AppFrame();
                app.setVisible(true);
            });
        });
        t.setRepeats(false); t.start();
    }
}
