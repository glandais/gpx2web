package io.github.glandais.gpx.storage.unit;

public class IntegerUnit extends StorageUnit<Integer> {

    @Override
    public Integer interpolate(Integer v, Integer vp1, double coef) {
        return (int) (v + coef * (vp1 - v));
    }

    @Override
    public String formatData(Integer integer) {
        return String.valueOf(integer);
    }

    @Override
    public String formatHuman(Integer integer) {
        return String.valueOf(integer);
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
