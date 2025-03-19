package io.github.glandais.gpx.util;

public record Vector(double x, double y) {

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

    public Vector sub(Vector p) {
        return new Vector(x - p.x, y - p.y);
    }
}
