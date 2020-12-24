package macky.scripting;

import org.derive4j.Data;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
public abstract class ScriptObject {
    public interface Cases<X> {
        X nil();
        X integer(int integer);
        X number(double number);
        X string(String string);
        X table(ScriptTable table);
        X function(ScriptFunction function);
    }

    public abstract <X> X match(Cases<X> cases);

    public boolean equivalent(ScriptObject b) {
        return ScriptObjects.cases()
                .nil(() -> b == ScriptObjects.nil())
                .integer(a -> ScriptObjects.getInteger(b).equals(Optional.of(a)) || ScriptObjects.getNumber(b).equals(Optional.of((double)a)))
                .number(a -> ScriptObjects.getInteger(b).map(Double::valueOf).equals(Optional.of(a)) || ScriptObjects.getNumber(b).equals(Optional.of(a)))
                .string(a -> ScriptObjects.getString(b).equals(Optional.of(a)))
                .table(a -> ScriptObjects.getTable(b).equals(Optional.of(a)))
                .function(a -> ScriptObjects.getFunction(b).equals(Optional.of(a)))
                .apply(this);
    }

    public String printFormat() {
        return ScriptObjects.cases()
                .nil(() -> "nil")
                .integer(String::valueOf)
                .number(String::valueOf)
                .string(String::valueOf)
                .table(a -> "table: " + a.toString())
                .function(a -> "function: " + a.toString())
                .apply(this);
    }

    public void nil() {
        if(this != ScriptObjects.nil()) {
            throw new ScriptException("expected nil");
        }
    }

    public int integer() {
        return ScriptObjects.getInteger(this).orElseThrow(() -> new ScriptException("expected integer"));
    }

    public double number() {
        return ScriptObjects.getNumber(this).orElseThrow(() -> new ScriptException("expected double"));
    }

    public String string() {
        return ScriptObjects.getString(this).orElseThrow(() -> new ScriptException("expected string"));
    }

    public ScriptTable table() {
        return ScriptObjects.getTable(this).orElseThrow(() -> new ScriptException("expected table"));
    }

    public ScriptFunction function() {
        return ScriptObjects.getFunction(this).orElseThrow(() -> new ScriptException("expected function"));
    }
}
