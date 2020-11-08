package io.github.glandais.gpx.storage.convert;

import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.unit.StorageUnit;

public abstract class ConvertableUnit<F, T> implements Unit<T> {

    private final StorageUnit<F> storageUnit;

    public ConvertableUnit(StorageUnit<F> storageUnit) {
        this.storageUnit = storageUnit;
    }

    public StorageUnit<F> getStorageUnit() {
        return storageUnit;
    }

    public abstract T convertFromStorage(F from);

    public abstract F convertToStorage(T value);

}
