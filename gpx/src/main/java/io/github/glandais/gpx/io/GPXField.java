package io.github.glandais.gpx.io;

import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum GPXField {

    cadence(PropertyKeys.cadence, "gpxtpx:cad", "cadence"),

    power(PropertyKeys.power, "power"),

    temperature(PropertyKeys.temperature, "gpxx:Temperature");

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
