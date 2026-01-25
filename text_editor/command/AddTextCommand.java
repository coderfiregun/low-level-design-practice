package text_editor.command;

import text_editor.TextEditor;

public class AddTextCommand implements Command {

    private final TextEditor textEditor;
    private final int row;
    private final int col;
    private final String text;


    public AddTextCommand(TextEditor textEditor,
                          int row,
                          int col,
                          String text) {
        this.textEditor = textEditor;
        this.row = row;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        textEditor.executeAddText(row, col, text);
    }

    @Override
    public void undo() {
        textEditor.executeDeleteText(row, col, text.length());
    }
}
