package io.github.glandais.gpx.data.values.unit;

/** m.s-2 */
public class SpeedUnit extends DoubleUnit {
    public static final SpeedUnit INSTANCE = new SpeedUnit();

    @Override
    public String formatHuman(Double value) {
        if (value == null) {
            return "";
        }
        return super.formatHuman(value * 3.6);
    }
}
