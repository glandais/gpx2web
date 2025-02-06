package io.github.glandais.gpx.data.values.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CxUnit extends DoubleUnit {

    public static final String PATTERN = "0.000";
    private static final ThreadLocal<DecimalFormat> CX_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat(PATTERN, new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return CX_FORMATTER.get().format(aDouble);
    }

    @Override
    public String getFormat() {
        return PATTERN;
    }
}
