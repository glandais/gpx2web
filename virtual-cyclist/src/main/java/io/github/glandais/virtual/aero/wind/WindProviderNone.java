package io.github.glandais.virtual.aero.wind;

public class WindProviderNone extends WindProviderConstant {

    public WindProviderNone() {
        super(new Wind(0, 0));
    }
}
