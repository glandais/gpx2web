package io.github.glandais.gpx.data.values.unit;

import io.github.glandais.gpx.data.values.PropertyKeyKind;
import lombok.Value;

import java.util.List;

@Value
public class Formul {

    String formula;

    PropertyKeyKind[] inputs;

    Unit<?> unit;

    public Formul(String formula, Unit<?> unit, PropertyKeyKind... inputs) {
        this.formula = formula;
        this.unit = unit;
        this.inputs = inputs;
    }

    public Formul(String formula, Unit<?> unit, List<PropertyKeyKind> inputs) {
        this.formula = formula;
        this.unit = unit;
        this.inputs = inputs.toArray(new PropertyKeyKind[0]);
    }
}
