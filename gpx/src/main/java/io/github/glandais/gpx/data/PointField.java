package io.github.glandais.gpx.data;

import io.github.glandais.gpx.data.values.ValueKey;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PointField {

    lat(ValueKey.lat),

    lon(ValueKey.lon),

    bearing(ValueKey.bearing),

    speed(ValueKey.speed),

    cadence(ValueKey.cadence, "gpxtpx:cad", "cadence"),

    dist(ValueKey.dist),

    speed_max(ValueKey.speed_max),

    time(ValueKey.time),

    power(ValueKey.power, "power"),

    grade(ValueKey.grade),

    ele(ValueKey.ele),

    aeroCoef(ValueKey.aeroCoef),

    elapsed(ValueKey.elapsed),

    originalSpeed(ValueKey.originalSpeed),

    simulatedSpeed(ValueKey.simulatedSpeed),

    speedDifference(ValueKey.speedDifference),

    mKg(ValueKey.mKg),

    temperature(ValueKey.temperature, "gpxx:Temperature");

    @Getter
    private final ValueKey valueKey;

    private final List<String> gpxTags;

    PointField(ValueKey valueKey, String... gpxTag) {
        this.valueKey = valueKey;
        this.gpxTags = Arrays.asList(gpxTag);
    }

    public static PointField fromGpxTag(String tagName) {
        for (PointField value : values()) {
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
