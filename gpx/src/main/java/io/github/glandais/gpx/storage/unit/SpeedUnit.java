package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SpeedUnit extends DoubleUnit implements HumanUnit {

    public static final String PATTERN = "0.#";
    private static final ThreadLocal<DecimalFormat> SPEED_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat(PATTERN, new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return SPEED_FORMATTER.get().format(getHumanValue(aDouble));
    }

    @Override
    public double getHumanValue(double aDouble) {
        return 3.6 * aDouble;
    }

    @Override
    public String getFormat() {
        return PATTERN;
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return "/3.6";
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return "*3.6";
    }
}
