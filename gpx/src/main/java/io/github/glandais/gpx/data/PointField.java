package io.github.glandais.gpx.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PointField {

    lat(),

    lon(),

    bearing(),

    speed(),

    cadence("gpxtpx:cad", "cadence"),

    dist(),

    speed_max(),

    time(),

    power("power"),

    grade(),

    ele(),

    aeroCoef(),

    elapsed(),

    originalSpeed(),

    simulatedSpeed(),

    speedDifference(),

    mKg(),

    temperature("gpxx:Temperature");

    private final List<String> gpxTags;

    PointField(String... gpxTag) {
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
