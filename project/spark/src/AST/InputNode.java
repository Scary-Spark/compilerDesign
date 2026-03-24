package AST;

public class InputNode extends Node {
    public String varName;
    public String value;

    public InputNode(String varName, String value, int line) {
        this.varName = varName;
        this.value = value;
        super.line = line;
    }
}