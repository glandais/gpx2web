package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.unit.StorageUnit;
import lombok.Value;

import java.util.List;

@Value
public class Formul {

    String formula;

    ValueKeyKind[] inputs;

    StorageUnit<?> unit;

    public Formul(String formula, StorageUnit<?> unit, ValueKeyKind... inputs) {
        this.formula = formula;
        this.unit = unit;
        this.inputs = inputs;
    }

    public Formul(String formula, StorageUnit<?> unit, List<ValueKeyKind> inputs) {
        this.formula = formula;
        this.unit = unit;
        this.inputs = inputs.toArray(new ValueKeyKind[0]);
    }
}
