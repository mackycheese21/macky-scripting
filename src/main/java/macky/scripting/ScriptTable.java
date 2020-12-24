package macky.scripting;

import java.util.*;

public class ScriptTable {
    private static final class Entry {
        public ScriptObject key, value;
    }
//    private Optional<ScriptTable> metatable;
    private final Set<Entry> entries;

    public ScriptTable() {
//        metatable = Optional.empty();
        entries = new HashSet<>();
    }

//    public Optional<ScriptTable> getMetatable() {
//        return metatable;
//    }
//
//    public void setMetatable(ScriptTable metatable) {
//        this.metatable = Optional.of(metatable);
//    }

    public void put(ScriptObject key, ScriptObject value) {
        for(Entry e : entries) {
            if(e.key.equivalent(key)) {
                e.value = value;
                return;
            }
        }
        Entry e = new Entry();
        e.key = key;
        e.value = value;
        entries.add(e);
    }

    public Optional<ScriptObject> get(ScriptObject key) {
        for(Entry e : entries) {
            if(e.key.equivalent(key)) {
                return Optional.of(e.value);
            }
        }
        return Optional.empty();
    }
}
