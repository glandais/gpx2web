package io.github.glandais.gpx;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public enum PointField {

    lat(),

    lon(),

    bearing(),

    speed(),

    cadence("gpxtpx:cad", "cadence"),

    dist(),

    max_speed(),

    time(),

    power("power"),

    grade(),

    ele(),

    cx(),

    ellapsed(),

    originalSpeed(),

    simulatedSpeed(),

    speedDifference(),

    mKg(),

    temperature("gpxx:Temperature");

    @Getter
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
        return gpxTags.size() > 0;
    }

    public String getGpxTag() {
        return gpxTags.size() > 0 ? gpxTags.get(0) : "unknown";
    }
}
