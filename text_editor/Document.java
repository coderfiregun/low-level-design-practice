package text_editor;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private final List<StringBuilder> rows;

    public Document() {
        rows = new ArrayList<>();
    }

    public void ensureRowExists(int row) {
        if (row == rows.size()) {
            rows.add(new StringBuilder());
        }
    }

    public void insertText(int row, int col, String text) {
        rows.get(row).insert(col, text);
    }

    public void deleteText(int row, int startCol, int length) {
        rows.get(row).delete(startCol, startCol + length);
    }

    public String getLine(int row) {
        return rows.get(row).toString();
    }

    public String substring(int row, int start, int end) {
        return rows.get(row).substring(start, end);
    }

    public int getRowCount() {
        return rows.size();
    }

}
