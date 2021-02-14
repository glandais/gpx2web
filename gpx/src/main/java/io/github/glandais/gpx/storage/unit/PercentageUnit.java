package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PercentageUnit extends DoubleUnit implements HumanUnit {

    public static final String PATTERN = "0.##";
    private static final ThreadLocal<DecimalFormat> PERC_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat(PATTERN, new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return PERC_FORMATTER.get().format(getHumanValue(aDouble));
    }

    @Override
    public double getHumanValue(double doubleValue) {
        return 100.0 * doubleValue;
    }

    @Override
    public String getFormat() {
        return PATTERN;
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return "/100";
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return "*100";
    }
}
