package io.github.glandais.gpx.data.values;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ValuesWithKind implements Values {

    private final Map<ValueKey, Map<ValueKind, Value<?, ?>>> map = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    @Override
    public <J> void put(ValueKey key, J value, Unit<J> unit, ValueKind kind) {
        if (kind == ValueKind.current) {
            throw new IllegalArgumentException("current kind must not be used");
        }
        putInternal(key, value, unit, kind);
    }

    private <J> void putInternal(ValueKey key, J value, Unit<J> unit, ValueKind kind) {
        Value valueObject = getStorageValue(value, unit, kind);
        Map<ValueKind, Value<?, ?>> byKind = getByKind(key);
        byKind.put(kind, valueObject);
        byKind.put(ValueKind.current, valueObject);
    }

    @Override
    public Value<?, ?> getCurrent(ValueKey key) {
        return getByKind(key).get(ValueKind.current);
    }

    @Override
    public Value<?, ?> get(ValueKey key, ValueKind kind) {
        Map<ValueKind, Value<?, ?>> valueKindValueMap = map.get(key);
        if (valueKindValueMap == null) {
            return null;
        }
        return valueKindValueMap.get(kind);
    }

    @Override
    public Map<ValueKind, Value<?, ?>> getAll(ValueKey key) {
        return map.get(key);
    }

    @Override
    public Set<ValueKey> getKeySet() {
        return map.keySet();
    }

    @Override
    public <J> J get(ValueKey key, Unit<J> unit) {

        Value v = getByKind(key).get(ValueKind.current);
        return getConvertedValue(v, unit);
    }

    private Map<ValueKind, Value<?, ?>> getByKind(ValueKey key) {
        return map.computeIfAbsent(key, k -> new EnumMap<>(ValueKind.class));
    }

    @Override
    public Values interpolate(Values to, double coef) {
        if (to instanceof ValuesWithKind toOk) {
            ValuesWithKind data = new ValuesWithKind();
            for (ValueKey key : this.map.keySet()) {

                Map<ValueKind, Value<?, ?>> fromByKind = this.getByKind(key);
                Map<ValueKind, Value<?, ?>> toByKind = toOk.getByKind(key);

                for (ValueKind valueKind : ValueKind.values()) {
                    Value v = fromByKind.get(valueKind);
                    Value vp1 = toByKind.get(valueKind);

                    Object nv = interpolateValue(v, vp1, coef);
                    if (nv != null) {
                        data.putInternal(key, nv, v.unit(), valueKind);
                    }
                }
            }
            return data;
        } else {
            throw new IllegalStateException("Not ValuesWithKind");
        }
    }

    public Values copy() {
        ValuesWithKind data = new ValuesWithKind();
        for (Map.Entry<ValueKey, Map<ValueKind, Value<?, ?>>> entry : this.map.entrySet()) {
            Map<ValueKind, Value<?, ?>> valuesCopy = new LinkedHashMap<>();
            for (Map.Entry<ValueKind, Value<?, ?>> valueKindValueEntry : entry.getValue().entrySet()) {
                valuesCopy.put(
                        valueKindValueEntry.getKey(),
                        valueKindValueEntry.getValue().copy()
                );
            }
            data.map.put(entry.getKey(), valuesCopy);
        }
        return data;
    }

}
