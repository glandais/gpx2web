package io.github.glandais.gpx.storage.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CxUnit extends DoubleUnit {

    private static final ThreadLocal<DecimalFormat> CX_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public String formatHuman(Double aDouble) {
        return CX_FORMATTER.get().format(aDouble);
    }

}
