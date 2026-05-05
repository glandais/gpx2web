package io.github.glandais.gpx.srtm.mapterhorn;

public final class TerrariumDecoder {

    private TerrariumDecoder() {}

    public static double decode(int r, int g, int b) {
        return r * 256.0 + g + b / 256.0 - 32768.0;
    }
}
