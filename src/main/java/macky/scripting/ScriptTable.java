package macky.scripting;

import java.util.*;

public class ScriptTable {
    private static final class Entry {
        ScriptObject key;
        ScriptObject value;
    }

    private final Set<Entry> entries;

    public ScriptTable() {
        entries = new HashSet<>();
    }

    public ScriptTable(List<ScriptObject> values) {
        this();
        for (int i = 0; i < values.size(); i++) {
            put(ScriptObjects.integer(i), values.get(i));
        }
    }

    public ScriptTable(Map<ScriptObject, ScriptObject> values) {
        this();
        values.forEach(this::put);
    }

    public void put(ScriptObject key, ScriptObject value) {
        for (Entry e : entries) {
            if (e.key.equivalent(key)) {
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
        for (Entry e : entries) {
            if (e.key.equivalent(key)) {
                return Optional.of(e.value);
            }
        }
        return Optional.empty();
    }
}
