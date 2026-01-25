package text_editor.command;

public interface Command {
    void execute();
    void undo();
}
