package io.github.glandais.gpx.storage;

import io.github.glandais.gpx.storage.convert.ConvertableUnit;
import io.github.glandais.gpx.storage.unit.StorageUnit;
import lombok.experimental.Delegate;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Values {

    @Delegate
    private Map<String, Value<?, ?>> values = new TreeMap<>();

    @Override
    public String toString() {
        return String.valueOf(values);
    }

    public <J> void put(String key, J value, Unit<J> unit) {
        if (unit instanceof StorageUnit) {
            values.put(key, new Value(value, (StorageUnit) unit));
        } else {
            ConvertableUnit convertableUnit = (ConvertableUnit) unit;
            J j = null;
            if (value != null) {
                j = (J) convertableUnit.convertToStorage(value);
            }
            values.put(key, new Value(j, convertableUnit.getStorageUnit()));
        }
    }

    public <J> J get(String key, Unit<J> unit) {

        Value v = values.get(key);
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

    public static Values interpolate(Values from, Values to, double coef) {
        Values data = new Values();
        for (String key : from.values.keySet()) {

            Value v = from.values.get(key);
            Value vp1 = to.values.get(key);
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
                data.put(key, nv, v.getUnit());
            }
        }
        return data;
    }

    public void putAllSuffixed(Values data, String s) {
        values.putAll(
                data.values.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey() + ".orig",
                        Map.Entry::getValue
                ))
        );
    }
}