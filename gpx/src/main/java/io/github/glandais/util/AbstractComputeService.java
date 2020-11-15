package io.github.glandais.util;

public class AbstractComputeService {

    protected double delta(double[] values, int i, int j) {
        if (i < 0 || j < 0 || i >= values.length || j >= values.length) {
            return 0.0;
        } else {
            return values[j] - values[i];
        }
    }

    protected long delta(long[] values, int i, int j) {
        if (i < 0 || j < 0 || i >= values.length || j >= values.length) {
            return 0;
        } else {
            return values[j] - values[i];
        }
    }
}
