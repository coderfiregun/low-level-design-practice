package text_editor.command;

import text_editor.TextEditor;

public class DeleteTextCommand implements Command {

    private final TextEditor textEditor;
    private final int row;
    private final int startColumn;
    private final String deletedText;

    public DeleteTextCommand(TextEditor textEditor,
                             int row,
                             int startColumn,
                             String deletedText) {
        this.textEditor = textEditor;
        this.row = row;
        this.startColumn = startColumn;
        this.deletedText = deletedText;
    }

    @Override
    public void execute() {
        textEditor.executeDeleteText(row, startColumn, deletedText.length());
    }

    @Override
    public void undo() {
        textEditor.executeAddText(row, startColumn, deletedText);
    }
}
