package io.github.glandais.gpx.io;

import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum GPXField {
    cadence(PropertyKeys.cadence, "gpxtpx:cad", "cadence"),

    heartRate(PropertyKeys.heartRate, "gpxtpx:hr", "hr", "heartrate", "tpx1:hr"),

    power(PropertyKeys.power, "power"),

    temperature(PropertyKeys.temperature, "gpxtpx:atemp", "gpxx:Temperature");

    @Getter
    private final PropertyKey<Double, ?> propertyKey;

    private final List<String> gpxTags;

    GPXField(PropertyKey<Double, DoubleUnit> propertyKey, String... gpxTag) {
        this.propertyKey = propertyKey;
        this.gpxTags = Arrays.asList(gpxTag);
    }

    public static GPXField fromGpxTag(String tagName) {
        for (GPXField value : values()) {
            for (String gpxTag : value.gpxTags) {
                if (tagName.equals(gpxTag)) {
                    return value;
                }
            }
        }
        return null;
    }

    public boolean isExportGpx() {
        return !gpxTags.isEmpty();
    }

    public String getGpxTag() {
        return !gpxTags.isEmpty() ? gpxTags.get(0) : "unknown";
    }
}
