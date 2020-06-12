package io.github.glandais.map;

import lombok.Value;

@Value
public class Vector {

    private double x;

    private double y;

    public Vector normalize() {

        double l = x * x + y * y;
        if (l > 0.0) {
            l = Math.sqrt(l);
            return new Vector(x / l, y / l);
        } else {
            return this;
        }
    }

    public Vector add(Vector other) {

        return new Vector(this.x + other.x, this.y + other.y);
    }

    public Vector mul(double c) {

        return new Vector(this.x * c, this.y * c);
    }
}
