package io.github.glandais.gpx.data.values.unit;

public abstract class DoubleUnit extends NumberUnit<Double> {

    @Override
    public Double interpolate(Double v, Double vp1, double coef) {
        return v + coef * (vp1 - v);
    }

    @Override
    public String formatData(Double aDouble) {
        return Double.toString(aDouble);
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return null;
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return null;
    }
}
