package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class LengthUnit extends DoubleUnit {
    private static final ThreadLocal<DecimalFormat> LENGTH_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return LENGTH_FORMATTER.get().format(aDouble);
    }
}
