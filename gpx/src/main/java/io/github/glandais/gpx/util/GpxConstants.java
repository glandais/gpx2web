package io.github.glandais.gpx.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GpxConstants {

    @UtilityClass
    public static class Physical {
        // g, cyclist from earth
        public static final double G = 9.8;

        // WGS-84 semi-major axis (m)
        public static final double SEMI_MAJOR_AXIS = 6378137.0;

        // WGS-84 first eccentricity squared
        public static final double FIRST_ECCENTRICITY_SQUARED = 6.6943799901377997e-3;

        // Earth perimeter (m)
        public static final double CIRC = SEMI_MAJOR_AXIS * 2 * Math.PI;
    }

    @UtilityClass
    public static class Virtual {
        // m.s-2, minimal speed = 2km/h
        public static final double MINIMAL_SPEED = 2.0 / 3.6;
        // s
        public static final double DT = 1.0;
    }

    public static boolean DEBUG = false;
}
