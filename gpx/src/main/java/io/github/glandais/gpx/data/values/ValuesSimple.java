package io.github.glandais.gpx.data.values;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ValuesSimple implements Values {

    private final Map<String, Value<?, ?>> map = new LinkedHashMap<>();

    @Override
    public <J> void put(String key, J value, Unit<J> unit, ValueKind kind) {
        map.put(key, getValue(value, unit, ValueKind.current));
    }

    @Override
    public Value<?, ?> getCurrent(String key) {
        return map.get(key);
    }

    @Override
    public Value<?, ?> get(String key, ValueKind kind) {
        return map.get(key);
    }

    @Override
    public Map<ValueKind, Value<?, ?>> getAll(String key) {
        LinkedHashMap<ValueKind, Value<?, ?>> result = new LinkedHashMap<>();
        result.put(ValueKind.current, getCurrent(key));
        return result;
    }

    @Override
    public Set<String> getKeySet() {
        return map.keySet();
    }

    @Override
    public <J> J get(String key, Unit<J> unit) {
        return getConvertedValue(getCurrent(key), unit);
    }

    @Override
    public Values interpolate(Values to, double coef) {
        if (to instanceof ValuesSimple toOk) {
            ValuesSimple data = new ValuesSimple();
            for (Map.Entry<String, Value<?, ?>> entry : this.map.entrySet()) {
                String key = entry.getKey();
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
        for (Map.Entry<String, Value<?, ?>> entry : this.map.entrySet()) {
            data.map.put(entry.getKey(), entry.getValue().copy());
        }
        return data;
    }
}
