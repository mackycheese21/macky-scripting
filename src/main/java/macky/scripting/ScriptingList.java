package macky.scripting;

import java.util.List;

public interface ScriptingList {

    int listSize();

    Object listGet(int index);

    void listSet(int index, Object value);

    void listAdd(int index, Object value);

    static ScriptingList wrap(List<Object> list) {
        return new ScriptingList() {
            @Override
            public int listSize() {
                return list.size();
            }

            @Override
            public Object listGet(int index) {
                return list.get(index);
            }

            @Override
            public void listSet(int index, Object value) {
                list.set(index, value);
            }

            @Override
            public void listAdd(int index, Object value) {
                list.add(index, value);
            }
        };
    }

}
