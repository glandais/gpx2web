package io.github.glandais.gpx.data.values.unit;

public interface Unit<S> {

    S interpolate(S v, S vp1, double coef);

    String formatHuman(S value);
}
