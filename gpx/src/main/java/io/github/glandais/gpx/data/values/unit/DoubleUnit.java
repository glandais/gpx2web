package io.github.glandais.gpx.data.values.unit;

public class DoubleUnit implements Unit<Double> {
    public static final DoubleUnit INSTANCE = new DoubleUnit();

    @Override
    public Double interpolate(Double v, Double vp1, double coef) {
        return v + coef * (vp1 - v);
    }

    @Override
    public String formatHuman(Double value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
