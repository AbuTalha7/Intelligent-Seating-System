import java.awt.*;

public class Theme {
    // Sidebar / Primary
    public static final Color C_PRI_DARK  = new Color(18, 26, 45);
    public static final Color C_PRI       = new Color(34, 54, 96);
    public static final Color C_PRI_LIGHT = new Color(62, 108, 182);
    // Accent
    public static final Color C_ACCENT    = new Color(235, 89, 41);
    public static final Color C_ACCENT2   = new Color(52, 181, 172);  // teal accent
    // Status
    public static final Color C_SUCCESS   = new Color(39, 174, 96);
    public static final Color C_WARN      = new Color(241, 196, 15);
    public static final Color C_DANGER    = new Color(211, 65, 60);
    public static final Color C_INFO      = new Color(52, 152, 219);
    // Background & surfaces
    public static final Color C_BG        = new Color(234, 239, 246);
    public static final Color C_BG2       = new Color(244, 247, 251);
    public static final Color C_CARD      = Color.WHITE;
    // Text
    public static final Color C_TEXT      = new Color(28, 40, 58);
    public static final Color C_TEXT2     = new Color(115, 130, 148);
    public static final Color C_TEXT3     = new Color(175, 188, 200);
    // Borders / sidebar
    public static final Color C_BORDER    = new Color(210, 220, 232);
    public static final Color C_SB_HOV    = new Color(32, 46, 68);
    public static final Color C_SB_ACT    = new Color(46, 70, 118);
    // Table
    public static final Color C_TBL_HDR  = new Color(34, 54, 96);
    public static final Color C_TBL_ALT  = new Color(245, 248, 253);

    // Fonts
    public static final Font F_TITLE = new Font("SansSerif", Font.BOLD,  26);
    public static final Font F_HEAD  = new Font("SansSerif", Font.BOLD,  18);
    public static final Font F_SUB   = new Font("SansSerif", Font.BOLD,  14);
    public static final Font F_BODY  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font F_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font F_BTN   = new Font("SansSerif", Font.BOLD,  13);
    public static final Font F_MONO  = new Font("Monospaced",Font.PLAIN, 12);

    // Chart palette (10 distinct colours)
    public static final Color[] CHART_PALETTE = {
        new Color(62,108,182), new Color(39,174,96),  new Color(235,89,41),
        new Color(142,68,173), new Color(22,160,133), new Color(192,57,43),
        new Color(44,62,80),   new Color(241,196,15), new Color(52,73,94),
        new Color(230,126,34)
    };
}
