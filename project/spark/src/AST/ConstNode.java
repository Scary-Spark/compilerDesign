package AST;

public class ConstNode extends Node {
    public String varName;
    public String value;

    public ConstNode(String varName, String value, int line) {
        this.varName = varName;
        this.value = value;
        super.line = line;
    }
}