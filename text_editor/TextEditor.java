package text_editor;

import text_editor.command.AddTextCommand;
import text_editor.command.Command;
import text_editor.command.DeleteTextCommand;

public class TextEditor implements DocumentEditor {

    private final Document document;
    private final CommandManager commandManager;

    public TextEditor() {
        this.document = new Document();
        this.commandManager = new CommandManager();
    }

    public void addText(int row, int col, String text) {
        document.ensureRowExists(row);
        Command command = new AddTextCommand(this, row, col, text);
        commandManager.executeCommand(command);
    }

    public void deleteText(int row, int startColumn, int length) {
        String textToDelete = document.substring(row, startColumn, startColumn+length);
        Command command = new DeleteTextCommand(this, row, startColumn, textToDelete);
        commandManager.executeCommand(command);
    }

    // CMD + Z
    public void undo() {
        commandManager.undo();
    }


    // CMD + SHIFT + Z
    public void redo() {
        commandManager.redo();
    }

    public String readLine(int row) {
        return document.getLine(row);
    }

    @Override
    public void executeAddText(int row, int col, String text) {
        document.insertText(row, col, text);
    }

    @Override
    public void executeDeleteText(int row, int startColumn, int length) {
        document.deleteText(row, startColumn, length);
    }

    @Override
    public String getDeletedText(int row, int startColumn, int length) {
        return document.substring(row, startColumn, startColumn + length);
    }

}
