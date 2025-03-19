package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.unit.Unit;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PropertyKey<S, U extends Unit<S>> {

    @EqualsAndHashCode.Include
    final PropertyKeyEnum propertyKeyEnum;

    final U unit;

    @Getter
    final int ordinal;

    PropertyKey(PropertyKeyEnum propertyKeyEnum, U unit) {
        this.propertyKeyEnum = propertyKeyEnum;
        this.ordinal = propertyKeyEnum.ordinal();
        this.unit = unit;
    }

    public String formatHuman(S value) {
        return unit.formatHuman(value);
    }

    public String getPropertyKeyName() {
        return propertyKeyEnum.name();
    }

    public S interpolate(S s1, S s2, double coef) {
        return unit.interpolate(s1, s2, coef);
    }
}
