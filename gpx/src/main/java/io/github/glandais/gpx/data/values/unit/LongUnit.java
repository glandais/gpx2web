package io.github.glandais.gpx.data.values.unit;

public class LongUnit extends NumberUnit<Long> {

    @Override
    public Long interpolate(Long v, Long vp1, double coef) {
        return (long) (v + coef * (vp1 - v));
    }

    @Override
    public String formatData(Long l) {
        return String.valueOf(l);
    }

    @Override
    public String formatHuman(Long l) {
        return String.valueOf(l);
    }

    @Override
    public String getFormat() {
        return "0";
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
