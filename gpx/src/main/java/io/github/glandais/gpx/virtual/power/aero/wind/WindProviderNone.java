package io.github.glandais.gpx.virtual.power.aero.wind;

public class WindProviderNone extends WindProviderConstant {

    public WindProviderNone() {
        super(new Wind(0, 0));
    }
}
