package io.github.glandais.guesser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConstantRange {

    protected double min;

    protected double max;

    public double getValue(int i, int nSteps) {
        return min + i * getStep(nSteps);
    }

    public double getStep(int nSteps) {
        return (max - min) / nSteps;
    }

}
