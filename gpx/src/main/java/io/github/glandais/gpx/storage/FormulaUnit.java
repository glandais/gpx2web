package io.github.glandais.gpx.storage;

import io.github.glandais.gpx.storage.unit.StorageUnit;

public class FormulaUnit extends StorageUnit<Formul> {

    private final StorageUnit<?> realUnit;

    public FormulaUnit(StorageUnit<?> unit) {
        super();
        this.realUnit = unit;
    }

    @Override
    public Formul interpolate(Formul v, Formul vp1, double coef) {
        return v;
    }

    @Override
    public String formatData(Formul s) {
        return s.getFormula();
    }

    @Override
    public String formatHuman(Formul s) {
        return s.getFormula();
    }

    @Override
    public String getFormat() {
        return realUnit.getFormat();
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return realUnit.getFormulaPartHumanToSI();
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return realUnit.getFormulaPartSIToHuman();
    }
}
