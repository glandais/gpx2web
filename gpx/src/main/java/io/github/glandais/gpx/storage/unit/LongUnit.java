package io.github.glandais.gpx.storage.unit;

public class LongUnit extends StorageUnit<Long> {

    @Override
    public Long interpolate(Long v, Long vp1, double coef) {
        return (long) (v + coef * (vp1 - v));
    }

    @Override
    public String formatData(Long l) {
        return String.valueOf(l);
    }

    @Override
    public String formatHuman(Long l) {
        return String.valueOf(l);
    }

}
