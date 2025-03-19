package io.github.glandais.gpx.virtual.power.aero.wind;

/**
 * @param windSpeed m.s-2
 * @param windDirection rad (0 = N, Pi/2 = E)
 */
public record Wind(double windSpeed, double windDirection) {}
