package io.github.glandais.gpx.storage;

import io.github.glandais.gpx.storage.convert.ConvertableUnit;
import io.github.glandais.gpx.storage.unit.StorageUnit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Values {

    private Map<String, Map<ValueKind, Value<?, ?>>> map = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    public <J> void put(String key, J value, Unit<J> unit, ValueKind kind) {
        if (kind == ValueKind.current) {
            throw new IllegalArgumentException("current kind must not be used");
        }
        putInternal(key, value, unit, kind);
    }

    private <J> void putInternal(String key, J value, Unit<J> unit, ValueKind kind) {
        Value valueObject;
        if (unit instanceof StorageUnit) {
            valueObject = new Value(value, (StorageUnit) unit, kind);
        } else {
            ConvertableUnit convertableUnit = (ConvertableUnit) unit;
            J j = null;
            if (value != null) {
                j = (J) convertableUnit.convertToStorage(value);
            }
            valueObject = new Value(j, convertableUnit.getStorageUnit(), kind);
        }
        Map<ValueKind, Value<?, ?>> byKind = getByKind(key);
        byKind.put(kind, valueObject);
        byKind.put(ValueKind.current, valueObject);
    }

    public Value<?, ?> getCurrent(String key) {
        return getByKind(key).get(ValueKind.current);
    }

    public Value<?, ?> get(String key, ValueKind kind) {
        Map<ValueKind, Value<?, ?>> valueKindValueMap = map.get(key);
        if (valueKindValueMap == null) {
            return null;
        }
        return valueKindValueMap.get(kind);
    }

    public Map<ValueKind, Value<?, ?>> getAll(String key) {
        return map.get(key);
    }

    public Set<String> getKeySet() {
        return map.keySet();
    }

    public <J> J get(String key, Unit<J> unit) {

        Value v = getByKind(key).get(ValueKind.current);
        if (v == null) {
            return null;
        }
        Object value = v.getValue();
        if (unit instanceof StorageUnit) {
            return (J) value;
        } else {
            ConvertableUnit convertableUnit = (ConvertableUnit) unit;
            J j = null;
            if (value != null) {
                j = (J) convertableUnit.convertFromStorage(value);
            }
            return j;
        }
    }

    private Map<ValueKind, Value<?, ?>> getByKind(String key) {
        return map.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    public static Values interpolate(Values from, Values to, double coef) {
        Values data = new Values();
        for (String key : from.map.keySet()) {

            Map<ValueKind, Value<?, ?>> fromByKind = from.getByKind(key);
            Map<ValueKind, Value<?, ?>> toByKind = to.getByKind(key);

            for (ValueKind valueKind : ValueKind.values()) {
                Value v = fromByKind.get(valueKind);
                if (v != null) {
                    Value vp1 = toByKind.get(valueKind);

                    Object ov = v == null ? null : v.getValue();
                    Object ovp1 = vp1 == null ? null : vp1.getValue();
                    Object nv;
                    if (ov != null && ovp1 != null) {
                        nv = v.getUnit().interpolate(ov, ovp1, coef);
                    } else if (ov != null) {
                        nv = ov;
                    } else {
                        nv = ovp1;
                    }
                    if (nv != null) {
                        data.putInternal(key, nv, v.getUnit(), valueKind);
                    }
                }
            }
        }
        return data;
    }

}
