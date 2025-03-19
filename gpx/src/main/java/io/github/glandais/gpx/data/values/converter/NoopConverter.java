package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.Unit;

public class NoopConverter<S, U extends Unit<S>> implements Converter<S, U, S> {
    @Override
    public S convertFromStorage(S storageValue) {
        return storageValue;
    }

    @Override
    public S convertToStorage(S value) {
        return value;
    }
}
