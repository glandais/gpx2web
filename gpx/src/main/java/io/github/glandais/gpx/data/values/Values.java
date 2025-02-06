package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.convert.ConvertableUnit;
import io.github.glandais.gpx.data.values.unit.StorageUnit;

import java.util.Map;
import java.util.Set;

public interface Values {
    <J> void put(String key, J value, Unit<J> unit, ValueKind kind);

    Value<?, ?> getCurrent(String key);

    Value<?, ?> get(String key, ValueKind kind);

    Map<ValueKind, Value<?, ?>> getAll(String key);

    Set<String> getKeySet();

    <J> J get(String key, Unit<J> unit);

    Values interpolate(Values to, double coef);

    default <J> Value getValue(J value, Unit<J> unit, ValueKind kind) {
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
        return valueObject;
    }

    default <J> J getConvertedValue(Value v, Unit<J> unit) {
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

    default Object interpolateValue(Value v, Value vp1, double coef) {
        Object nv = null;
        if (v != null) {
            Object ov = v.getValue();
            Object ovp1 = vp1 == null ? null : vp1.getValue();
            if (ov != null && ovp1 != null) {
                nv = v.getUnit().interpolate(ov, ovp1, coef);
            } else if (ov != null) {
                nv = ov;
            } else {
                nv = ovp1;
            }
        }
        return nv;
    }
}
