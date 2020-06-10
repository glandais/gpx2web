/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Grum Ltd (Romain Gallet)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Geocalc nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.github.glandais.gpx.geocalc;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Earth related calculations.
 *
 * @author rgallet
 */
public class EarthCalc {

    private static final double EARTH_RADIUS = 6371.01 * 1000; //meters

    /**
     * This is the half-way point along a great circle path between the two points.
     *
     * @param standPoint standPoint
     * @param forePoint  standPoint
     * @return mid point
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html"></a>
     */
    public static GeocalcPoint midPoint(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        double λ1 = toRadians(standPoint.longitude);
        double λ2 = toRadians(forePoint.longitude);

        double φ1 = toRadians(standPoint.latitude);
        double φ2 = toRadians(forePoint.latitude);

        double Bx = cos(φ2) * cos(λ2 - λ1);
        double By = cos(φ2) * sin(λ2 - λ1);

        double φ3 = atan2(sin(φ1) + sin(φ2), sqrt((cos(φ1) + Bx) * (cos(φ1) + Bx) + By * By));
        double λ3 = λ1 + atan2(By, cos(φ1) + Bx);

        return GeocalcPoint.at(Coordinate.fromRadians(φ3), Coordinate.fromRadians(λ3));
    }

    /**
     * Returns the distance between two points at spherical law of cosines.
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return The distance, in meters
     */
    public static double gcdDistance(GeocalcPoint standPoint, GeocalcPoint forePoint) {

        double diffLongitudes = toRadians(abs(forePoint.longitude - standPoint.longitude));
        double slat = toRadians(standPoint.latitude);
        double flat = toRadians(forePoint.latitude);

        //spherical law of cosines

        double sphereCos = (sin(slat) * sin(flat)) + (cos(slat) * cos(flat) * cos(diffLongitudes));
        double c = acos(max(min(sphereCos, 1d), -1d));

        return EARTH_RADIUS * c;
    }

    /**
     * Returns the distance between two points at Harvesine formula.
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return The distance, in meters
     */
    public static double harvesineDistance(GeocalcPoint standPoint, GeocalcPoint forePoint) {

        double diffLongitudes = toRadians(abs(forePoint.longitude - standPoint.longitude));
        double slat = toRadians(standPoint.latitude);
        double flat = toRadians(forePoint.latitude);

        // haversine formula
        double diffLatitudes = toRadians(abs(forePoint.latitude - standPoint.latitude));
        double a = sin(diffLatitudes / 2) * sin(diffLatitudes / 2) + cos(slat) * cos(flat) * sin(diffLongitudes / 2) * sin(diffLongitudes / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a)); //angular distance in radians

        return EARTH_RADIUS * c;
    }

    /**
     * Calculate distance, (azimuth) bearing and final bearing between standPoint and forePoint.
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return Vincenty object which holds all 3 values
     */
    private static Vincenty vincenty(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        double λ1 = toRadians(standPoint.longitude);
        double λ2 = toRadians(forePoint.longitude);

        double φ1 = toRadians(standPoint.latitude);
        double φ2 = toRadians(forePoint.latitude);

        double a = 6_378_137;
        double b = 6_356_752.314245;
        double f = 1 / 298.257223563;

        double L = λ2 - λ1;
        double tanU1 = (1 - f) * tan(φ1), cosU1 = 1 / sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1;
        double tanU2 = (1 - f) * tan(φ2), cosU2 = 1 / sqrt((1 + tanU2 * tanU2)), sinU2 = tanU2 * cosU2;

        double λ = L, λʹ, iterationLimit = 100, cosSqα, σ, cos2σM, cosσ, sinσ, sinλ, cosλ;
        do {
            sinλ = sin(λ);
            cosλ = cos(λ);
            double sinSqσ = (cosU2 * sinλ) * (cosU2 * sinλ) + (cosU1 * sinU2 - sinU1 * cosU2 * cosλ) * (cosU1 * sinU2 - sinU1 * cosU2 * cosλ);
            sinσ = sqrt(sinSqσ);
            if (sinσ == 0) return new Vincenty(0, 0, 0);  // co-incident points
            cosσ = sinU1 * sinU2 + cosU1 * cosU2 * cosλ;
            σ = atan2(sinσ, cosσ);
            double sinα = cosU1 * cosU2 * sinλ / sinσ;
            cosSqα = 1 - sinα * sinα;
            cos2σM = cosσ - 2 * sinU1 * sinU2 / cosSqα;

            if (Double.isNaN(cos2σM)) cos2σM = 0;  // equatorial line: cosSqα=0 (§6)
            double C = f / 16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
            λʹ = λ;
            λ = L + (1 - C) * f * sinα * (σ + C * sinσ * (cos2σM + C * cosσ * (-1 + 2 * cos2σM * cos2σM)));
        } while (abs(λ - λʹ) > 1e-12 && --iterationLimit > 0);

        if (iterationLimit == 0) throw new IllegalStateException("Formula failed to converge");

        double uSq = cosSqα * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double Δσ = B * sinσ * (cos2σM + B / 4 * (cosσ * (-1 + 2 * cos2σM * cos2σM) -
                B / 6 * cos2σM * (-3 + 4 * sinσ * sinσ) * (-3 + 4 * cos2σM * cos2σM)));

        double distance = b * A * (σ - Δσ);

        double initialBearing = atan2(cosU2 * sinλ, cosU1 * sinU2 - sinU1 * cosU2 * cosλ);
        initialBearing = (initialBearing + 2 * PI) % (2 * PI); //turning value to trigonometric direction

        double finalBearing = atan2(cosU1 * sinλ, -sinU1 * cosU2 + cosU1 * sinU2 * cosλ);
        finalBearing = (finalBearing + 2 * PI) % (2 * PI);  //turning value to trigonometric direction

        return new Vincenty(distance, toDegrees(initialBearing), toDegrees(finalBearing));
    }

    public static double vincentyDistance(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        return vincenty(standPoint, forePoint).distance;
    }

    /**
     * Returns (azimuth) bearing at Vincenty formula.
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return (azimuth) bearing in degrees to the North
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html"></a>
     */
    public static double vincentyBearing(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        return vincenty(standPoint, forePoint).initialBearing;
    }

    /**
     * Returns final bearing in direction of standPoint→forePoint at Vincenty formula.
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return (azimuth) bearing in degrees to the North
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html"></a>
     */
    public static double vincentyFinalBearing(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        return vincenty(standPoint, forePoint).finalBearing;
    }

    /**
     * Returns the coordinates of a point which is "distance" away
     * from standPoint in the direction of "bearing"
     * <p>
     * Note: North is equal to 0 for bearing value
     *
     * @param standPoint Origin
     * @param bearing    Direction in degrees, clockwise from north
     * @param distance   distance in meters
     * @return forePoint coordinates
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html"></a>
     */
    public static GeocalcPoint pointAt(GeocalcPoint standPoint, double bearing, double distance) {
        /*
         φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
         λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )

         where
         φ is latitude,
         λ is longitude,
         θ is the bearing (clockwise from north),
         δ is the angular distance d/R;
         d being the distance travelled, R the earth’s radius
         */

        double φ1 = toRadians(standPoint.latitude);
        double λ1 = toRadians(standPoint.longitude);
        double θ = toRadians(bearing);
        double δ = distance / EARTH_RADIUS; // normalize linear distance to radian angle

        double φ2 = asin(sin(φ1) * cos(δ) + cos(φ1) * sin(δ) * cos(θ));
        double λ2 = λ1 + atan2(sin(θ) * sin(δ) * cos(φ1), cos(δ) - sin(φ1) * sin(φ2));

        double λ2_harmonised = (λ2 + 3 * PI) % (2 * PI) - PI; // normalise to −180..+180°

        return GeocalcPoint.at(Coordinate.fromRadians(φ2), Coordinate.fromRadians(λ2_harmonised));
    }

    /**
     * Returns the (azimuth) bearing, in decimal degrees, from standPoint to forePoint
     *
     * @param standPoint Origin point
     * @param forePoint  Destination point
     * @return (azimuth) bearing, in decimal degrees
     */
    public static double bearing(GeocalcPoint standPoint, GeocalcPoint forePoint) {
        /*
         * Formula: θ = atan2( 	sin(Δlong).cos(lat2), cos(lat1).sin(lat2) − sin(lat1).cos(lat2).cos(Δlong) )
         */

        double Δlong = toRadians(forePoint.longitude - standPoint.longitude);
        double y = sin(Δlong) * cos(toRadians(forePoint.latitude));
        double x = cos(toRadians(standPoint.latitude)) * sin(toRadians(forePoint.latitude))
                - sin(toRadians(standPoint.latitude)) * cos(toRadians(forePoint.latitude)) * cos(Δlong);

        double bearing = (atan2(y, x) + 2 * PI) % (2 * PI);

        return toDegrees(bearing);
    }

    /**
     * Returns an area around standPoint
     *
     * @param standPoint The centre of the area
     * @param distance   Distance around standPoint, im meters
     * @return The area
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html"></a>
     */
    public static BoundingArea around(GeocalcPoint standPoint, double distance) {

        //45 degrees going north-east
        GeocalcPoint northEast = pointAt(standPoint, 45, distance);

        //225 degrees going south-west
        GeocalcPoint southWest = pointAt(standPoint, 225, distance);

        return BoundingArea.at(northEast, southWest);
    }

    private static class Vincenty {
        /**
         * distance is the distance in meter
         * initialBearing is the initial bearing, or forward azimuth (in reference to North point), in degrees
         * finalBearing is the final bearing (in direction p1→p2), in degrees
         */
        final double distance, initialBearing, finalBearing;

        Vincenty(double distance, double initialBearing, double finalBearing) {
            this.distance = distance;
            this.initialBearing = initialBearing;
            this.finalBearing = finalBearing;
        }
    }
}
