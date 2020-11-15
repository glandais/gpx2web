package io.github.glandais.util;

public class Constants {

    // g, cyclist from earth
    public static final double G = 9.8;

    // WGS-84 semi-major axis
    public static final double SEMI_MAJOR_AXIS = 6378137.0;

    // WGS-84 first eccentricity squared
    public static final double FIRST_ECCENTRICITY_SQUARED = 6.6943799901377997e-3;

    // Earth perimeter
    public static final double CIRC = SEMI_MAJOR_AXIS * 2 * Math.PI;

    public static final boolean VERIFIED = false;

}
