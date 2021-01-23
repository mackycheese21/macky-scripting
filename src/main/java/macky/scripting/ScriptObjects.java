package macky.scripting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ScriptObjects {

    private ScriptObjects() {

    }

    public static boolean asBoolean(Object object) {
        if(object instanceof BigDecimal) return object.equals(BigDecimal.ZERO);
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

    public static Map<Object, Object> getMap(Object object) {
        if (object instanceof Map) return (Map<Object, Object>) object;
        else throw new ScriptException("expected a map, got " + object);
    }

    public static List<Object> getList(Object object) {
        if (object instanceof List) return (List<Object>) object;
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

    public static Object simpleAccess(Object owner, Object key) {
        if(owner instanceof Map) {
            return ((Map<?, ?>) owner).get(key);
        } else if (owner instanceof List) {
            int index = getNumber(key).intValue();
            if(index < 0 || index >= ((List<?>) owner).size()) {
                return null;
            }
            return ((List<?>) owner).get(index);
        } else {
            throw new ScriptException("cannot get key on " + owner);
        }
    }

    public static Object augmentMethod(Object owner, Object property) {
        ScriptFunction function = getFunction(property);
        return new ScriptFunction() {
            @Override
            public Object call(List<Object> params) {
                params.add(0, owner);
                return function.call(params);
            }
        };
    }

    public static Object access(Object owner, Object key, boolean method) {
        if(method) {
            return augmentMethod(owner, simpleAccess(owner, key));
        } else {
            return simpleAccess(owner, key);
        }
    }

    public static void assign(Object owner, Object key, Object value, boolean method) {
        if(method) value = augmentMethod(owner, value);
        if(owner instanceof Map) {
            ((Map<Object, Object>) owner).put(key, value);
        } else if (owner instanceof List) {
            int index = getNumber(key).intValue();
            if(index < 0 || index >= ((List<?>) owner).size()) {
                throw new ScriptException("attempted to assign to out of bounds position in list");
            }
            ((List<Object>) owner).set(index, value);
        } else {
            throw new ScriptException("cannot assign key on " + owner);
        }
    }

}
