package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SpeedUnit extends DoubleUnit {

    private static final ThreadLocal<DecimalFormat> SPEED_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return SPEED_FORMATTER.get().format(3.6 * aDouble);
    }
}
