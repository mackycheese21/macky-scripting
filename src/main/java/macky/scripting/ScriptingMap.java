package macky.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface ScriptingMap {

    int mapSize();

    Object mapGet(Object key);

    void mapSet(Object key, Object value);

    Set<Object> mapKeys();

    final class Builder {
        private final Map<Object, Object> data;

        public Builder() {
            data = new HashMap<>();
        }

        public Builder put(Object key, Object value) {
            data.put(key, value);
            return this;
        }

        public Object getData() {
            return data;
        }
    }

    static ScriptingMap wrap(Map<Object, Object> map) {
        return new ScriptingMap() {
            @Override
            public int mapSize() {
                return map.size();
            }

            @Override
            public Object mapGet(Object key) {
                return map.getOrDefault(key, null);
            }

            @Override
            public void mapSet(Object key, Object value) {
                map.put(key, value);
            }

            @Override
            public Set<Object> mapKeys() {
                return map.keySet();
            }
        };
    }

}
