package io.github.glandais.gpx.storage.unit;

public abstract class DoubleUnit extends StorageUnit<Double> {

    @Override
    public Double interpolate(Double v, Double vp1, double coef) {
        return v + coef * (vp1 - v);
    }

    @Override
    public String formatData(Double aDouble) {
        return Double.toString(aDouble);
    }

}
