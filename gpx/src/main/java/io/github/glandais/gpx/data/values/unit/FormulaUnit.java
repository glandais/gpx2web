package io.github.glandais.gpx.data.values.unit;

public class FormulaUnit implements Unit<Formul> {
    public static final FormulaUnit INSTANCE = new FormulaUnit();

    @Override
    public Formul interpolate(Formul v, Formul vp1, double coef) {
        return v;
    }

    @Override
    public String formatHuman(Formul value) {
        if (value == null) {
            return null;
        }
        return value.getFormula();
    }

}
