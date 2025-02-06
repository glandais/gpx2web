package io.github.glandais.gpx.data.values;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ValuesWithKind implements Values {

    private Map<String, Map<ValueKind, Value<?, ?>>> map = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    @Override
    public <J> void put(String key, J value, Unit<J> unit, ValueKind kind) {
        if (kind == ValueKind.current) {
            throw new IllegalArgumentException("current kind must not be used");
        }
        putInternal(key, value, unit, kind);
    }

    private <J> void putInternal(String key, J value, Unit<J> unit, ValueKind kind) {
        Value valueObject = getValue(value, unit, kind);
        Map<ValueKind, Value<?, ?>> byKind = getByKind(key);
        byKind.put(kind, valueObject);
        byKind.put(ValueKind.current, valueObject);
    }

    @Override
    public Value<?, ?> getCurrent(String key) {
        return getByKind(key).get(ValueKind.current);
    }

    @Override
    public Value<?, ?> get(String key, ValueKind kind) {
        Map<ValueKind, Value<?, ?>> valueKindValueMap = map.get(key);
        if (valueKindValueMap == null) {
            return null;
        }
        return valueKindValueMap.get(kind);
    }

    @Override
    public Map<ValueKind, Value<?, ?>> getAll(String key) {
        return map.get(key);
    }

    @Override
    public Set<String> getKeySet() {
        return map.keySet();
    }

    @Override
    public <J> J get(String key, Unit<J> unit) {

        Value v = getByKind(key).get(ValueKind.current);
        return getConvertedValue(v, unit);
    }

    private Map<ValueKind, Value<?, ?>> getByKind(String key) {
        return map.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    @Override
    public ValuesWithKind interpolate(Values to, double coef) {
        if (to instanceof ValuesWithKind toOk) {
            ValuesWithKind data = new ValuesWithKind();
            for (String key : this.map.keySet()) {

                Map<ValueKind, Value<?, ?>> fromByKind = this.getByKind(key);
                Map<ValueKind, Value<?, ?>> toByKind = toOk.getByKind(key);

                for (ValueKind valueKind : ValueKind.values()) {
                    Value v = fromByKind.get(valueKind);
                    Value vp1 = toByKind.get(valueKind);

                    Object nv = interpolateValue(v, vp1, coef);
                    if (nv != null) {
                        data.putInternal(key, nv, v.getUnit(), valueKind);
                    }
                }
            }
            return data;
        } else {
            throw new IllegalStateException("Not ValuesWithKind");
        }
    }

}
