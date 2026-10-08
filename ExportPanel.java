import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ExportPanel extends JPanel {
    private AppFrame frame;
    private JTextArea preview;
    private JComboBox<String> formatCombo;

    public ExportPanel(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Export Arrangement");
        title.setFont(Theme.F_TITLE);
        title.setForeground(Theme.C_TEXT);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Options card
        CustomComponents.CardPanel optCard = new CustomComponents.CardPanel();
        optCard.setLayout(new BorderLayout(12, 10));
        optCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        optCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        optCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel optTitle = new JLabel("Export Options");
        optTitle.setFont(Theme.F_HEAD);
        optTitle.setForeground(Theme.C_PRI);
        optCard.add(optTitle, BorderLayout.NORTH);

        JPanel optRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        optRow.setOpaque(false);
        JLabel fmtLbl = new JLabel("Format:");
        fmtLbl.setFont(Theme.F_BODY);
        formatCombo = new JComboBox<>(new String[] { "Text Report (.txt)", "CSV (.csv)", "HTML (.html)" });
        formatCombo.setFont(Theme.F_BODY);
        formatCombo.setPreferredSize(new Dimension(200, 32));
        formatCombo.addActionListener(e -> refreshPreview());

        CustomComponents.RoundedButton exportBtn = new CustomComponents.RoundedButton("⤓ Save File", Theme.C_PRI,
                Theme.C_PRI_LIGHT);
        exportBtn.setPreferredSize(new Dimension(130, 36));
        exportBtn.addActionListener(e -> exportFile());

        CustomComponents.RoundedButton copyBtn = new CustomComponents.RoundedButton("⎘ Copy", Theme.C_ACCENT2,
                Theme.C_ACCENT2.darker());
        copyBtn.setPreferredSize(new Dimension(100, 36));
        copyBtn.addActionListener(e -> {
            if (!DataManager.generated) {
                frame.showToast("Generate arrangement first!", Theme.C_WARN);
                return;
            }
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(preview.getText()), null);
            frame.showToast("Copied to clipboard", Theme.C_SUCCESS);
        });

        optRow.add(fmtLbl);
        optRow.add(formatCombo);
        optRow.add(Box.createHorizontalStrut(16));
        optRow.add(exportBtn);
        optRow.add(copyBtn);
        optCard.add(optRow, BorderLayout.CENTER);
        center.add(optCard);
        center.add(Box.createVerticalStrut(14));

        // Preview card
        CustomComponents.CardPanel previewCard = new CustomComponents.CardPanel();
        previewCard.setLayout(new BorderLayout(0, 8));
        previewCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel prevTitle = new JLabel("Preview");
        prevTitle.setFont(Theme.F_SUB);
        prevTitle.setForeground(Theme.C_PRI);
        previewCard.add(prevTitle, BorderLayout.NORTH);

        preview = new JTextArea(20, 60);
        preview.setFont(Theme.F_MONO);
        preview.setEditable(false);
        preview.setBackground(new Color(248, 250, 253));
        preview.setForeground(Theme.C_TEXT);
        preview.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(preview);
        sp.getViewport().setBackground(new Color(248, 250, 253));
        sp.setBorder(new CustomComponents.RoundedLineBorder(Theme.C_BORDER, 1, 8));
        previewCard.add(sp, BorderLayout.CENTER);

        // Info label
        JLabel infoLbl = new JLabel("Switch format to update preview. HTML is best for printing.");
        infoLbl.setFont(Theme.F_SMALL);
        infoLbl.setForeground(Theme.C_TEXT2);
        previewCard.add(infoLbl, BorderLayout.SOUTH);
        center.add(previewCard);
        add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        refreshPreview();
    }

    private void refreshPreview() {
        if (!DataManager.generated) {
            preview.setText("No arrangement generated yet.\nGo to the Generate tab first.");
            return;
        }
        String fmt = getSelectedFormat();
        preview.setText(fmt.equals("html") ? buildHtml() : fmt.equals("csv") ? buildCsv() : buildTxt());
        preview.setCaretPosition(0);
    }

    private String getSelectedFormat() {
        int idx = formatCombo.getSelectedIndex();
        return idx == 1 ? "csv" : idx == 2 ? "html" : "txt";
    }

    private void exportFile() {
        if (!DataManager.generated) {
            frame.showToast("Generate arrangement first!", Theme.C_WARN);
            return;
        }
        String fmt = getSelectedFormat();
        String ext = "." + (fmt.equals("html") ? "html" : fmt);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("seating_arrangement" + ext));
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                pw.print(fmt.equals("html") ? buildHtml() : fmt.equals("csv") ? buildCsv() : buildTxt());
                DataManager.log("Exported as " + fmt.toUpperCase() + ": " + fc.getSelectedFile().getName());
                frame.showToast("Exported successfully", Theme.C_SUCCESS);
            } catch (Exception ex) {
                frame.showToast("Export failed: " + ex.getMessage(), Theme.C_DANGER);
            }
        }
    }

    // ── Text report ───────────────────────────────────────────────────────
    private String buildTxt() {
        ExamInfo ei = DataManager.examInfo;
        StringBuilder sb = new StringBuilder();
        String line = "═".repeat(60);
        sb.append(line).append("\n");
        sb.append("  EXAM SEATING ARRANGEMENT REPORT\n");
        sb.append(line).append("\n");
        sb.append("  Exam   : ").append(ei.examName).append("\n");
        sb.append("  Date   : ").append(ei.date).append("  ").append(ei.time).append("\n");
        sb.append("  Venue  : ").append(ei.venue).append("\n");
        sb.append("  Supvr  : ").append(ei.supervisor).append("\n");
        sb.append(line).append("\n\n");

        Map<String, Student> stuMap = new HashMap<>();
        for (Student s : DataManager.students)
            stuMap.put(s.id, s);

        for (Room rm : DataManager.rooms) {
            Map<String, SeatAllocation> rs = new HashMap<>();
            for (SeatAllocation sa : DataManager.arrangement.values())
                if (sa.roomId.equals(rm.id))
                    rs.put(sa.row + "-" + sa.col, sa);

            sb.append("┌─ ").append(rm.name)
                    .append("  (").append(rm.rows).append("×").append(rm.cols).append(")")
                    .append("  Invigilator: ").append(rm.invigilator.isEmpty() ? "—" : rm.invigilator)
                    .append("\n");

            for (int r = 0; r < rm.rows; r++) {
                sb.append("│  ");
                for (int c = 0; c < rm.cols; c++) {
                    String key = r + "-" + c;
                    SeatAllocation sa = rs.get(key);
                    if (rm.reserved.contains(key))
                        sb.append(String.format("[%-9s] ", "RESERVED"));
                    else if (sa != null) {
                        Student s = stuMap.get(sa.studentId);
                        String nm = s != null ? (s.id + " " + s.name) : sa.studentId;
                        if (nm.length() > 9)
                            nm = nm.substring(0, 9);
                        sb.append(String.format("[%-9s] ", nm));
                    } else
                        sb.append(String.format("[%-9s] ", "   --   "));
                }
                sb.append("\n");
            }
            sb.append("└").append("─".repeat(58)).append("\n\n");
        }

        sb.append("Total assigned: ").append(DataManager.arrangement.size())
                .append(" / ").append(DataManager.students.size()).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()))
                .append("\n");
        return sb.toString();
    }

    // ── CSV ───────────────────────────────────────────────────────────────
    private String buildCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student ID,Name,Email,Class,Section,Subject Group,Special Needs,Room ID,Room Name,Row,Col\n");
        Map<String, Student> stuMap = new HashMap<>();
        for (Student s : DataManager.students)
            stuMap.put(s.id, s);
        Map<String, Room> roomMap = new HashMap<>();
        for (Room r : DataManager.rooms)
            roomMap.put(r.id, r);

        for (SeatAllocation sa : DataManager.arrangement.values()) {
            Student s = stuMap.get(sa.studentId);
            Room rm = roomMap.get(sa.roomId);
            if (s != null)
                sb.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d\n",
                        esc(s.id), esc(s.name), esc(s.email), esc(s.className),
                        esc(s.section), esc(s.subjectGroup), s.specialNeeds,
                        esc(sa.roomId), rm != null ? esc(rm.name) : "",
                        sa.row + 1, sa.col + 1));
        }
        return sb.toString();
    }

    private String esc(String v) {
        return v.contains(",") ? "\"" + v + "\"" : v;
    }

    // ── HTML ──────────────────────────────────────────────────────────────
    private String buildHtml() {
        ExamInfo ei = DataManager.examInfo;
        Map<String, Student> stuMap = new HashMap<>();
        for (Student s : DataManager.students)
            stuMap.put(s.id, s);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>Exam Seating Arrangement</title>");
        sb.append("<style>");
        sb.append("body{font-family:Arial,sans-serif;background:#f0f4f8;margin:0;padding:20px;color:#1c2d3a;}");
        sb.append(
                ".header{background:linear-gradient(135deg,#1b263b,#22395f);color:#fff;padding:24px 32px;border-radius:10px;margin-bottom:20px;}");
        sb.append(".header h1{margin:0 0 6px;font-size:22px;} .header p{margin:3px 0;font-size:13px;color:#9bb5d8;}");
        sb.append(
                ".room{background:#fff;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08);margin-bottom:20px;overflow:hidden;}");
        sb.append(".room-head{background:#22395f;color:#fff;padding:12px 18px;font-size:14px;font-weight:bold;}");
        sb.append(".room-head span{font-weight:normal;color:#9bb5d8;margin-left:10px;font-size:12px;}");
        sb.append(
                "table{width:100%;border-collapse:collapse;} td{padding:9px 12px;font-size:12px;border:1px solid #e2e8f0;}");
        sb.append("thead td{background:#3e6cb6;color:#fff;font-weight:bold;font-size:11px;}");
        sb.append(
                "tr:nth-child(even) td{background:#f5f8fd;} .res{background:#fff8e1!important;color:#b8860b;font-style:italic;}");
        sb.append(".sn{background:#fff3e0!important;} .empty{color:#aaa;}");
        sb.append(".stats{background:#fff;border-radius:8px;padding:16px 20px;box-shadow:0 2px 8px rgba(0,0,0,.08);}");
        sb.append(
                ".stat-row{display:flex;gap:16px;} .stat{flex:1;text-align:center;padding:12px;border-radius:6px;background:#f0f4f8;}");
        sb.append(
                ".stat .val{font-size:26px;font-weight:bold;color:#22395f;} .stat .lbl{font-size:11px;color:#7f8c8d;}");
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class='header'>");
        sb.append("<h1>").append(ei.examName).append("</h1>");
        sb.append("<p>Date: <b>").append(ei.date).append("  ").append(ei.time).append("</b>  &nbsp;|&nbsp;  Venue: <b>")
                .append(ei.venue).append("</b></p>");
        sb.append("<p>Supervisor: <b>").append(ei.supervisor).append("</b>  &nbsp;|&nbsp;  Institution: <b>")
                .append(ei.institution).append("</b></p>");
        sb.append("</div>");

        // Summary stats
        sb.append("<div class='stats'><div class='stat-row'>");
        statHtml(sb, String.valueOf(DataManager.students.size()), "Total Students");
        statHtml(sb, String.valueOf(DataManager.rooms.size()), "Exam Rooms");
        statHtml(sb, String.valueOf(DataManager.arrangement.size()), "Assigned");
        int unassigned = DataManager.students.size() - DataManager.arrangement.size();
        statHtml(sb, String.valueOf(unassigned), unassigned > 0 ? "Unassigned ⚠" : "Unassigned");
        sb.append("</div></div><br>");

        // Per room tables
        for (Room rm : DataManager.rooms) {
            Map<String, SeatAllocation> rs = new HashMap<>();
            for (SeatAllocation sa : DataManager.arrangement.values())
                if (sa.roomId.equals(rm.id))
                    rs.put(sa.row + "-" + sa.col, sa);

            int usedCount = rs.size();
            sb.append("<div class='room'><div class='room-head'>").append(rm.name);
            sb.append("<span>").append(rm.rows).append("×").append(rm.cols)
                    .append("  |  Capacity: ").append(rm.capacity())
                    .append("  |  Used: ").append(usedCount)
                    .append("  |  Invigilator: ").append(rm.invigilator.isEmpty() ? "—" : rm.invigilator)
                    .append("</span></div>");
            sb.append(
                    "<table><thead><tr><td>Seat</td><td>Student ID</td><td>Name</td><td>Class</td><td>Section</td><td>Subject Group</td><td>Special</td></tr></thead><tbody>");

            for (int r = 0; r < rm.rows; r++) {
                for (int c = 0; c < rm.cols; c++) {
                    String key = r + "-" + c;
                    SeatAllocation sa = rs.get(key);
                    boolean res = rm.reserved.contains(key);
                    String pos = "R" + (r + 1) + "C" + (c + 1);
                    if (res) {
                        sb.append("<tr><td>").append(pos)
                                .append("</td><td class='res' colspan='6'>— RESERVED —</td></tr>");
                    } else if (sa != null) {
                        Student s = stuMap.get(sa.studentId);
                        String cls = s != null && s.specialNeeds ? " class='sn'" : "";
                        sb.append("<tr").append(cls).append("><td>").append(pos).append("</td>");
                        if (s != null) {
                            sb.append("<td>").append(s.id).append("</td><td>").append(s.name).append("</td>")
                                    .append("<td>").append(s.className).append("</td><td>").append(s.section)
                                    .append("</td>")
                                    .append("<td>").append(s.subjectGroup).append("</td>")
                                    .append("<td>").append(s.specialNeeds ? "★ Yes" : "No").append("</td></tr>");
                        }
                    } else {
                        sb.append("<tr><td>").append(pos)
                                .append("</td><td colspan='6' class='empty'>— Empty —</td></tr>");
                    }
                }
            }
            sb.append("</tbody></table></div>");
        }

        sb.append("<p style='color:#aaa;font-size:11px;text-align:right;'>Generated: ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                .append("</p></body></html>");
        return sb.toString();
    }

    private void statHtml(StringBuilder sb, String val, String lbl) {
        sb.append("<div class='stat'><div class='val'>").append(val).append("</div><div class='lbl'>").append(lbl)
                .append("</div></div>");
    }
}
