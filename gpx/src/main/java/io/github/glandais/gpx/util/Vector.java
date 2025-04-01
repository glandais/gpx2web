/*
 * R3.java
 *
 * Copyright (c) 2016 Karambola. All rights reserved.
 */

package io.github.glandais.gpx.util;

public record Vector(double x, double y, double z) {

    public static final Vector O = new Vector(0.0, 0.0, 0.0);
    public static final Vector I = new Vector(1.0, 0.0, 0.0);
    public static final Vector J = new Vector(0.0, 1.0, 0.0);
    public static final Vector K = new Vector(0.0, 0.0, 1.0);

    public static Vector add(final Vector a, final Vector b) {
        return new Vector(
                a.x + b.x // x
                ,
                a.y + b.y // y
                ,
                a.z + b.z // z
                );
    }

    public static Vector sub(final Vector a, final Vector b) {
        return new Vector(
                a.x - b.x // x
                ,
                a.y - b.y // y
                ,
                a.z - b.z // z
                );
    }

    public static Vector scalar(final double k, final Vector v) {
        return new Vector(
                k * v.x // x
                ,
                k * v.y // y
                ,
                k * v.z // z
                );
    }

    public static double dot(final Vector a, final Vector b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vector cross(final Vector a, final Vector b) {
        return new Vector(
                a.y * b.z - a.z * b.y // x
                ,
                a.z * b.x - a.x * b.z // y
                ,
                a.x * b.y - a.y * b.x // z
                );
    }

    public static double modulus(final Vector v) {
        return Math.sqrt(dot(v, v));
    }

    public static Vector normalize(final Vector v) {
        final double m = modulus(v);

        if (m == 0.0) return O;

        return scalar(1.0 / m, v);
    }

    /**
     * Calculates the euclidean distance between two points.
     *
     * @param a first point
     * @param b second point
     * @return distance between a and b
     * @author Afonso Santos
     */
    public static double distance(final Vector a, final Vector b) {
        return b.sub(a).modulus();
    }

    /**
     * Calculates the euclidean distance from a point to a line segment.
     *
     * @param v the point
     * @param a start of line segment
     * @param b end of line segment
     * @return distance from v to line segment [a,b]
     * @author Afonso Santos
     */
    public static double distanceToSegment(final Vector v, final Vector a, final Vector b) {
        final Vector ab = b.sub(a);
        final Vector av = v.sub(a);

        if (av.dot(ab) <= 0.0) // Point is lagging behind start of the segment, so perpendicular distance is not
            // viable.
            return av.modulus(); // Use distance to start of segment instead.

        final Vector bv = v.sub(b);

        if (bv.dot(ab) >= 0.0) // Point is advanced past the end of the segment, so perpendicular distance is not
            // viable.
            return bv.modulus(); // Use distance to end of the segment instead.

        return (ab.cross(av)).modulus() / ab.modulus(); // Perpendicular distance of point to segment.
    }

    /**
     * Calculates the euclidean distance from a point to a line segmented path.
     *
     * @param v the point from with the distance is measured
     * @param path the array of points wich, when sequentialy joined by line segments, form a path
     * @return distance from v to closest of the path forming line segments
     * @author Afonso Santos
     */
    public static double distanceToPath(final Vector v, final Vector[] path) {
        double minDistance = Double.MAX_VALUE;

        for (int pathPointIdx = 1; pathPointIdx < path.length; ++pathPointIdx) {
            final double d = distanceToSegment(v, path[pathPointIdx - 1], path[pathPointIdx]);

            if (d < minDistance) minDistance = d;
        }

        return minDistance;
    }

    public Vector add(final Vector v) {
        return add(this, v);
    }

    public Vector cross(final Vector v) {
        return cross(this, v);
    }

    /**
     * Calculates the euclidean distance to a point.
     *
     * @param v the point to witch the distance is calculated
     * @return the distance between this point and v
     * @author Afonso Santos
     */
    public double distance(final Vector v) {
        return distance(this, v);
    }

    /**
     * Calculates the euclidean distance to a line segmented path.
     *
     * @param path the array of points wich, when sequentialy joined by line segments, form a path
     * @return distance from this point to closest of the path forming line segments
     * @author Afonso Santos
     */
    public double distanceToPath(final Vector[] path) {
        return distanceToPath(this, path);
    }

    /**
     * Calculates the euclidean distance to a line segment.
     *
     * @param a start of line segment
     * @param b end of line segment
     * @return distance from this point to line segment [a,b]
     * @author Afonso Santos
     */
    public double distanceToSegment(final Vector a, final Vector b) {
        return distanceToSegment(this, a, b);
    }

    public double dot(final Vector v) {
        return dot(this, v);
    }

    public double modulus() {
        return modulus(this);
    }

    public Vector scalar(final double k) {
        return scalar(k, this);
    }

    public Vector sub(final Vector v) {
        return sub(this, v);
    }

    public Vector normalize() {
        return normalize(this);
    }
}
