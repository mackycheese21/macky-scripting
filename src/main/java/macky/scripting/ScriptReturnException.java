package macky.scripting;

public class ScriptReturnException extends ScriptException {

    private final Object returnValue;

    public ScriptReturnException(Object returnValue) {
        super("return not caught by function");
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

}
