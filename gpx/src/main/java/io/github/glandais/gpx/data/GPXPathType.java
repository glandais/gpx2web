package io.github.glandais.gpx.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GPXPathType {
    ROUTE("rte"),
    TRACK("trk");

    private final String gpxTag;

    public static GPXPathType getByTagName(String tagName) {
        for (GPXPathType value : values()) {
            if (value.gpxTag.equals(tagName)) {
                return value;
            }
        }
        return TRACK;
    }
}
