package macky.scripting;

public class ScriptReturnException extends ScriptException {
    private final ScriptObject value;

    public ScriptReturnException(ScriptObject value) {
        super("return statement outside of function");
        this.value = value;
    }

    public ScriptObject getValue() {
        return value;
    }
}
