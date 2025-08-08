package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.AngleUnit;

public class DegreesConverter implements Converter<Double, AngleUnit, Double> {

    @Override
    public Double convertFromStorage(Double storageValue) {
        return storageValue == null ? null : Math.toDegrees(storageValue);
    }

    @Override
    public Double convertToStorage(Double value) {
        return value == null ? null : Math.toRadians(value);
    }
}
