package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.AngleUnit;

public class SemiCirclesConverter implements Converter<Double, AngleUnit, Integer> {

    public static int toSemiCircles(double rad) {
        return (int) (rad * 2147483648.0 / Math.PI);
    }

    @Override
    public Integer convertFromStorage(Double storageValue) {
        return storageValue == null ? null : toSemiCircles(storageValue);
    }

    @Override
    public Double convertToStorage(Integer value) {
        return value == null ? null : value * Math.PI / 2147483648.0;
    }
}
