package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AngleUnit extends DoubleUnit implements HumanUnit {

    public static final String PATTERN = "0.00#####";
    private static final ThreadLocal<DecimalFormat> LAT_LON_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat(PATTERN, new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return LAT_LON_FORMATTER.get().format(getHumanValue(aDouble));
    }

    @Override
    public String getFormat() {
        return PATTERN;
    }

    @Override
    public double getHumanValue(double doubleValue) {
        return Math.toDegrees(doubleValue);
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return "*PI()/180";
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return "*180/PI()";
    }
}
