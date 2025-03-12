package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.Unit;

public interface Converter<S, U extends Unit<S>, T> {
    T convertFromStorage(S storageValue);

    S convertToStorage(T value);
}
