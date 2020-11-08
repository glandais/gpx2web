package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DoubleAnyUnit extends DoubleUnit {
    private static final ThreadLocal<DecimalFormat> DOUBLE_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.#####", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return DOUBLE_FORMATTER.get().format(aDouble);
    }
}
