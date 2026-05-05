package io.github.glandais.gpx.srtm.mapterhorn;

import java.awt.image.BufferedImage;

public final class TerrainTile {

    private final int width;
    private final int height;
    private final int[] argb;

    public TerrainTile(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.argb = image.getRGB(0, 0, width, height, null, 0, width);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double getElevation(int x, int y) {
        int p = argb[y * width + x];
        int r = (p >> 16) & 0xFF;
        int g = (p >> 8) & 0xFF;
        int b = p & 0xFF;
        return TerrariumDecoder.decode(r, g, b);
    }
}
