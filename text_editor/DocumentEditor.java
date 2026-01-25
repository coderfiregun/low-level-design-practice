package text_editor;

public interface DocumentEditor {

    void executeAddText(int row, int col, String text);
    void executeDeleteText(int row, int startColumn, int length);
    String getDeletedText(int row, int startColumn, int length);

}
