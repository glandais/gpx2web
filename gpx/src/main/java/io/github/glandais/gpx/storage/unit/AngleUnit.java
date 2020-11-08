package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AngleUnit extends DoubleUnit {

    private static final ThreadLocal<DecimalFormat> LAT_LON_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.00#####", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return LAT_LON_FORMATTER.get().format(Math.toDegrees(aDouble));
    }
}
