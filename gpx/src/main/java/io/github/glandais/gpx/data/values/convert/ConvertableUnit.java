package io.github.glandais.gpx.data.values.convert;

import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class ConvertableUnit<F, T> implements Unit<T> {

    private final StorageUnit<F> storageUnit;

    public abstract T convertFromStorage(F from);

    public abstract F convertToStorage(T value);

}
