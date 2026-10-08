import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Pure-Java reader for .xlsx files.
 * No external libraries required — uses java.util.zip + javax.xml.parsers.
 *
 * Expected sheet layout:
 *   Col A = ID, Col B = Name, Col C = Class,
 *   Col D = Section, Col E = Subject Group, Col F = Special Needs
 */
public class ExcelImporter {

    /**
     * Read all rows from the first sheet of an xlsx workbook.
     * Returns a list of String arrays (one per row); missing cells are empty strings.
     * Row 0 is typically the header row — callers must skip it themselves.
     */
    public static List<String[]> readXlsx(File xlsxFile) throws Exception {
        try (ZipFile zip = new ZipFile(xlsxFile)) {

            // 1. Load shared strings (if present)
            List<String> sharedStrings = loadSharedStrings(zip);

            // 2. Find the first sheet entry
            ZipEntry sheetEntry = findSheetEntry(zip);
            if (sheetEntry == null)
                throw new IOException("No worksheet found inside the Excel file.");

            // 3. Parse the sheet XML
            Document doc = parseXml(zip.getInputStream(sheetEntry));
            NodeList rowNodes = doc.getElementsByTagName("row");

            List<String[]> result = new ArrayList<>();
            int maxCol = 6; // A–F

            for (int ri = 0; ri < rowNodes.getLength(); ri++) {
                Element rowEl = (Element) rowNodes.item(ri);
                String[] rowData = new String[maxCol];
                Arrays.fill(rowData, "");

                NodeList cellNodes = rowEl.getElementsByTagName("c");
                for (int ci = 0; ci < cellNodes.getLength(); ci++) {
                    Element cell = (Element) cellNodes.item(ci);
                    int colIdx = cellRefToColIndex(cell.getAttribute("r"));
                    if (colIdx < 0 || colIdx >= maxCol) continue;

                    String type  = cell.getAttribute("t"); // "s" = shared string, "" = number/inline
                    String rawV  = getChildText(cell, "v");
                    String rawIs = getChildText(cell, "is"); // inline string

                    String value = "";
                    if ("s".equals(type)) {
                        // Shared string index
                        try {
                            int idx = Integer.parseInt(rawV.trim());
                            value = (idx >= 0 && idx < sharedStrings.size()) ? sharedStrings.get(idx) : "";
                        } catch (NumberFormatException ignored) {}
                    } else if ("inlineStr".equals(type) || "str".equals(type)) {
                        // Inline or formula string
                        value = rawIs.isEmpty() ? rawV : rawIs;
                    } else {
                        // Number / boolean / date — keep as raw text
                        value = rawV;
                    }
                    rowData[colIdx] = value.trim();
                }
                result.add(rowData);
            }
            return result;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Load xl/sharedStrings.xml into a List<String>. */
    private static List<String> loadSharedStrings(ZipFile zip) throws Exception {
        List<String> list = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return list; // workbook may have no shared strings
        Document doc = parseXml(zip.getInputStream(entry));
        // Each <si> element holds a string; text may be inside <t> or <r><t>
        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            StringBuilder sb = new StringBuilder();
            NodeList tNodes = si.getElementsByTagName("t");
            for (int j = 0; j < tNodes.getLength(); j++) {
                Node t = tNodes.item(j);
                sb.append(t.getTextContent());
            }
            list.add(sb.toString());
        }
        return list;
    }

    /** Find the first worksheet entry (xl/worksheets/sheet1.xml or similar). */
    private static ZipEntry findSheetEntry(ZipFile zip) {
        // Try the canonical name first
        ZipEntry e = zip.getEntry("xl/worksheets/sheet1.xml");
        if (e != null) return e;
        // Fall back: any entry whose name ends in .xml inside worksheets/
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith("xl/worksheets/") && name.endsWith(".xml"))
                return entry;
        }
        return null;
    }

    /** Parse an XML InputStream into a DOM Document. */
    private static Document parseXml(InputStream in) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Disable external entity processing for safety
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    /**
     * Convert a cell reference like "A1", "B3", "AA5" to a 0-based column index.
     * Returns -1 if the reference is malformed.
     */
    private static int cellRefToColIndex(String ref) {
        if (ref == null || ref.isEmpty()) return -1;
        int col = 0;
        for (int i = 0; i < ref.length(); i++) {
            char c = ref.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                col = col * 26 + (c - 'A' + 1);
            } else {
                break; // reached the row number part
            }
        }
        return col - 1; // convert to 0-based
    }

    /**
     * Get the text content of the first direct child element with the given tag.
     * Returns "" if no such child exists.
     */
    private static String getChildText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        // For inline strings, collect all text from nested <t> tags
        Node node = nl.item(0);
        return node == null ? "" : node.getTextContent();
    }
}
