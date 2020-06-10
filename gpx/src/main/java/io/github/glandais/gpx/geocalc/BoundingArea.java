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

import java.util.Objects;

/**
 * Represents an area (viewed rectangular shaped projected onto Earth), defined by its top left and bottom right
 * coordinates
 *
 * @author rgallet
 */
public class BoundingArea {
    public final GeocalcPoint northEast, southWest;
    public final GeocalcPoint southEast, northWest;

    private BoundingArea(GeocalcPoint northEast, GeocalcPoint southWest) {
        this.northEast = northEast;
        this.southWest = southWest;

        southEast = GeocalcPoint.at(Coordinate.fromDegrees(southWest.latitude), Coordinate.fromDegrees(northEast.longitude));
        northWest = GeocalcPoint.at(Coordinate.fromDegrees(northEast.latitude), Coordinate.fromDegrees(southWest.longitude));
    }

    public static BoundingArea at(GeocalcPoint northEast, GeocalcPoint southWest) {
        return new BoundingArea(northEast, southWest);
    }

    @Override
    public String toString() {
        return "BoundingArea{" + "northEast=" + northEast + ", southWest=" + southWest + '}';
    }

    /**
     * @return true if GeocalcPoint point is contained withing this bounding area
     * @deprecated use contains(GeocalcPoint point)
     */
    @Deprecated
    public boolean isContainedWithin(GeocalcPoint point) {
        return contains(point);
    }

    /**
     * @param point point to check
     * @return true if GeocalcPoint point is contained withing this bounding area
     */
    public boolean contains(GeocalcPoint point) {
        boolean predicate1 = point.latitude >= this.southWest.latitude && point.latitude <= this.northEast.latitude;

        if (!predicate1) {
            return false;
        }

        boolean predicate2;

        if (southWest.longitude > northEast.longitude) { //area is going across the max/min longitude boundaries (ie. sort of back of the Earth)
            //we "split" the area in 2, longitude-wise, point only needs to be in one or the other.
            boolean predicate3 = point.longitude <= northEast.longitude && point.longitude >= -180;
            boolean predicate4 = point.longitude >= southWest.longitude && point.longitude <= 180;

            predicate2 = predicate3 || predicate4;
        } else {
            predicate2 = point.longitude >= southWest.longitude && point.longitude <= northEast.longitude;
        }

        return predicate2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        BoundingArea other = (BoundingArea) obj;

        return Objects.equals(this.northEast, other.northEast) &&
                Objects.equals(this.southWest, other.southWest);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.northEast != null ? this.northEast.hashCode() : 0);
        hash = 13 * hash + (this.southWest != null ? this.southWest.hashCode() : 0);
        return hash;
    }
}
