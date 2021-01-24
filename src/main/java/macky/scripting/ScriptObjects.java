package macky.scripting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScriptObjects {

    public static final ScriptFunction STRING_LEN = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 1);
            return new BigDecimal(getString(params.get(0)).length());
        }
    };

    public static final ScriptFunction STRING_SUBSTR = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 3);
            String v = getString(params.get(0));
            int a = getNumber(params.get(1)).intValue();
            int b = getNumber(params.get(2)).intValue();
            try {
                return v.substring(a, b);
            } catch (StringIndexOutOfBoundsException e) {
                throw new ScriptException(e.getMessage());
            }
        }
    };

    public static final ScriptFunction STRING_FIRST = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 2);
            String v = getString(params.get(0));
            int a = getNumber(params.get(1)).intValue();
            try {
                return v.substring(0, a);
            } catch (StringIndexOutOfBoundsException e) {
                throw new ScriptException(e.getMessage());
            }
        }
    };

    public static final ScriptFunction STRING_LAST = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 2);
            String v = getString(params.get(0));
            int a = getNumber(params.get(1)).intValue();
            try {
                return v.substring(v.length() - a);
            } catch (StringIndexOutOfBoundsException e) {
                throw new ScriptException(e.getMessage());
            }
        }
    };

    public static final ScriptFunction STRING_AT = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 2);
            String v = getString(params.get(0));
            int a = getNumber(params.get(1)).intValue();
            try {
                return "" + v.charAt(a);
            } catch (StringIndexOutOfBoundsException e) {
                throw new ScriptException(e.getMessage());
            }
        }
    };

    public static final ScriptFunction MAP_KEYS = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 1);
            return new ArrayList<>(getMap(params.get(0)).mapKeys());
        }
    };

    public static final ScriptFunction MAP_LEN = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 1);
            return new BigDecimal(getMap(params.get(0)).mapSize());
        }
    };

    public static final ScriptFunction LIST_LEN = new ScriptFunction() {
        @Override
        public Object call(List<Object> params) {
            argCount(params, 1);
            return new BigDecimal(getList(params.get(0)).listSize());
        }
    };

    private ScriptObjects() {

    }

    public static Set<Object> getKeys(Object object) {
        if(object instanceof Map) return ((Map<Object, Object>) object).keySet();
        else if (object instanceof ScriptingMap) return ((ScriptingMap) object).mapKeys();
        else throw new ScriptException("expected a map, got " + object);
    }

    public static int getLength(Object object) {
        if(object instanceof Map) return ((Map<Object, Object>) object).size();
        else if (object instanceof List) return ((List<Object>) object).size();
        else if (object instanceof ScriptingMap) return ((ScriptingMap) object).mapSize();
        else if (object instanceof ScriptingList) return ((ScriptingList) object).listSize();
        else throw new ScriptException("expected a map or list, got " + object);
    }

    public static boolean asBoolean(Object object) {
        if (object instanceof BigDecimal) return object.equals(BigDecimal.ZERO);
        else if (object instanceof String) return ((String) object).length() > 0;
        else if (object instanceof Boolean) return (boolean) object;
        else if (object == null) return false;
        else if (object instanceof Map) return ((Map<?, ?>) object).size() > 0;
        else if (object instanceof List) return ((List<?>) object).size() > 0;
        else if (object instanceof ScriptFunction) return true;
        else return true;
    }

    public static BigDecimal getNumber(Object object) {
        if (object instanceof BigDecimal) return (BigDecimal) object;
        else throw new ScriptException("expected a number, got " + object);
    }

    public static String getString(Object object) {
        if (object instanceof String) return (String) object;
        else throw new ScriptException("expected a string, got " + object);
    }

    public static boolean getBoolean(Object object) {
        if (object instanceof Boolean) return ((Boolean) object);
        else throw new ScriptException("expected a boolean, got " + object);
    }

    public static void getNull(Object object) {
        if (object != null) throw new ScriptException("expected null");
    }

    public static ScriptingMap getMap(Object object) {
        if (object instanceof Map) return ScriptingMap.wrap((Map<Object, Object>) object);
        else throw new ScriptException("expected a map, got " + object);
    }

    public static ScriptingList getList(Object object) {
        if (object instanceof List) return ScriptingList.wrap((List<Object>) object);
        else throw new ScriptException("expected a list, got " + object);
    }

    public static ScriptFunction getFunction(Object object) {
        if (object instanceof ScriptFunction) return (ScriptFunction) object;
        else throw new ScriptException("expected a function, got " + object);
    }

    public static Object getUserData(Object object) {
        if (object instanceof BigDecimal ||
                object instanceof String ||
                object instanceof Boolean ||
                object == null ||
                object instanceof Map ||
                object instanceof List ||
                object instanceof ScriptFunction) throw new ScriptException("expected userdata, got " + object);
        else return object;
    }

    public static ObjectType getObjectType(Object object) {
        if (object instanceof BigDecimal) return ObjectType.NUMBER;
        else if (object instanceof String) return ObjectType.STRING;
        else if (object instanceof Boolean) return ObjectType.BOOLEAN;
        else if (object == null) return ObjectType.NULL;
        else if (object instanceof Map) return ObjectType.MAP;
        else if (object instanceof List) return ObjectType.LIST;
        else if (object instanceof ScriptFunction) return ObjectType.FUNCTION;
        else return ObjectType.USERDATA;
    }

    public static Object access(Object owner, Object key, boolean method) {
        Object result;
        if(owner instanceof Map) {
            result = ((Map<Object, Object>) owner).get(key);
        } else if (owner instanceof List) {
            int index = getNumber(key).intValue();
            if (index < 0 || index >= ((List<?>) owner).size()) {
                throw new ScriptException("list index out of bounds");
            }
            result = ((List<?>) owner).get(index);
        } else if (owner instanceof ScriptingMap) {
            result = ((ScriptingMap) owner).mapGet(key);
        } else if (owner instanceof ScriptingList) {
            int index = getNumber(key).intValue();
            if (index < 0 || index >= ((List<?>) owner).size()) {
                throw new ScriptException("list index out of bounds");
            }
            result = ((ScriptingList) owner).listGet(index);
        } else {
            throw new ScriptException("expected a map or list, got " + owner);
        }
        if(method) {
            ScriptFunction function = getFunction(result);
            return new ScriptFunction() {
                @Override
                public Object call(List<Object> params) {
                    params.add(0, owner);
                    return function.call(params);
                }
            };
        } else {
            return result;
        }
    }

    public static void assign(Object owner, Object key, Object value) {
        if(owner instanceof Map) {
            ((Map<Object, Object>) owner).put(key, value);
        } else if (owner instanceof List) {
            int index = getNumber(key).intValue();
            if (index < 0 || index >= ((List<?>) owner).size()) {
                throw new ScriptException("list index out of bounds");
            }
            ((List<Object>) owner).set(index, value);
        } else if (owner instanceof ScriptingMap) {
            ((ScriptingMap) owner).mapSet(key, value);
        } else if (owner instanceof ScriptingList) {
            int index = getNumber(key).intValue();
            if (index < 0 || index >= ((ScriptingList) owner).listSize()) {
                throw new ScriptException("list index out of bounds");
            }
            ((ScriptingList) owner).listSet(index, value);
        } else {
            throw new ScriptException("expected a map or list, got " + owner);
        }
    }

}
