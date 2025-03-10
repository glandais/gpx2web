package io.github.glandais.gpx.data.values;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ValuesSimple implements Values {

    private final Map<ValueKey, Value<?, ?>> map = new EnumMap<>(ValueKey.class);

    @Override
    public <J> void put(ValueKey key, J value, Unit<J> unit, ValueKind kind) {
        map.put(key, getStorageValue(value, unit, ValueKind.current));
    }

    @Override
    public Value<?, ?> getCurrent(ValueKey key) {
        return map.get(key);
    }

    @Override
    public Value<?, ?> get(ValueKey key, ValueKind kind) {
        return map.get(key);
    }

    @Override
    public Map<ValueKind, Value<?, ?>> getAll(ValueKey key) {
        LinkedHashMap<ValueKind, Value<?, ?>> result = new LinkedHashMap<>();
        result.put(ValueKind.current, getCurrent(key));
        return result;
    }

    @Override
    public Set<ValueKey> getKeySet() {
        return map.keySet();
    }

    @Override
    public <J> J get(ValueKey key, Unit<J> unit) {
        return getConvertedValue(getCurrent(key), unit);
    }

    @Override
    public Values interpolate(Values to, double coef) {
        if (to instanceof ValuesSimple toOk) {
            ValuesSimple data = new ValuesSimple();
            for (Map.Entry<ValueKey, Value<?, ?>> entry : this.map.entrySet()) {
                ValueKey key = entry.getKey();
                Value v = entry.getValue();
                Value vp1 = toOk.getCurrent(key);

                Object nv = interpolateValue(v, vp1, coef);

                if (nv != null) {
                    data.put(key, nv, v.unit(), ValueKind.current);
                }
            }
            return data;
        } else {
            throw new IllegalStateException("Not ValuesWithKind");
        }
    }

    @Override
    public Values copy() {
        ValuesSimple data = new ValuesSimple();
        for (Map.Entry<ValueKey, Value<?, ?>> entry : this.map.entrySet()) {
            data.map.put(entry.getKey(), entry.getValue().copy());
        }
        return data;
    }
}
