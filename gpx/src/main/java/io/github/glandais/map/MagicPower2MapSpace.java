/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.github.glandais.map;

/**
 * Mercator projection with a world width and height of 256 * 2<sup>zoom</sup>
 * pixel. This is the common projecton used by Openstreetmap and Google. It
 * provides methods to translate coordinates from 'map space' into latitude and
 * longitude (on the WGS84 ellipsoid) and vice versa. Map space is measured in
 * pixels. The origin of the map space is the top left corner. The map space
 * origin (0,0) has latitude ~85 and longitude -180
 * 
 * <p>
 * This is the only implementation that is currently supported by Mobile Atlas
 * Creator.
 * </p>
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 * 
 * @see MapSpace
 */
public class MagicPower2MapSpace {

	protected static final double MAX_LAT = 85.05112877980659;
	protected static final double MIN_LAT = -85.05112877980659;

	protected final int tileSize;

	/**
	 * Pre-computed values for the world size (height respectively width) in the
	 * different zoom levels.
	 */
	protected final int[] worldSize;
	public static final MagicPower2MapSpace INSTANCE_256 = new MagicPower2MapSpace(
			256);

	protected MagicPower2MapSpace(int tileSize) {
		this.tileSize = tileSize;
		worldSize = new int[22 + 1];
		for (int zoom = 0; zoom < worldSize.length; zoom++)
			worldSize[zoom] = 256 * (1 << zoom);
	}

	protected double radius(int zoom) {
		return getMaxPixels(zoom) / (2.0 * Math.PI);
	}

	/**
	 * Returns the absolute number of pixels in y or x, defined as:
	 * 2<sup>zoom</sup> * TILE_WIDTH where TILE_WIDTH is the width respectively
	 * height of a tile in pixels
	 * 
	 * @param zoom
	 *            [0..22]
	 * @return
	 */
	public int getMaxPixels(int zoom) {
		return worldSize[zoom];
	}

	protected int falseNorthing(int aZoomlevel) {
		return (-1 * getMaxPixels(aZoomlevel) / 2);
	}

	/**
	 * Transforms latitude to pixelspace
	 * 
	 * @param lat
	 *            [-90...90] qparam zoom [0..22]
	 * @return [0..2^zoom*TILE_SIZE[
	 * @author Jan Peter Stotz
	 */
	public double cLatToY(double lat, int zoom) {
		lat = Math.max(MIN_LAT, Math.min(MAX_LAT, lat));
		double sinLat = Math.sin(Math.toRadians(lat));
		double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
		int mp = getMaxPixels(zoom);
		double y = (mp * (0.5 - (log / (4.0 * Math.PI))));
		y = Math.min(y, mp - 1);
		return y;
	}

	/**
	 * Transform longitude to pixelspace
	 * 
	 * @param lon
	 *            [-180..180]
	 * @param zoom
	 *            [0..22]
	 * @return [0..2^zoom*TILE_SIZE[
	 * @author Jan Peter Stotz
	 */
	public double cLonToX(double lon, int zoom) {
		int mp = getMaxPixels(zoom);
		double x = (((mp * 1.0) * (lon + 180.0)) / 360.0);
		x = Math.min(x, mp - 1);
		return x;
	}

	/**
	 * Transforms pixel coordinate X to longitude
	 * 
	 * @param x
	 *            [0..2^zoom*TILE_WIDTH[
	 * @param zoom
	 *            [0..22]
	 * @return ]-180..180[
	 * @author Jan Peter Stotz
	 */
	public double cXToLon(int x, int zoom) {
		return ((360.0 * x) / getMaxPixels(zoom)) - 180.0;
	}

	/**
	 * Transforms pixel coordinate Y to latitude
	 * 
	 * @param y
	 *            [0..2^zoom*TILE_WIDTH[
	 * @param zoom
	 *            [0..22]
	 * @return [MIN_LAT..MAX_LAT] is about [-85..85]
	 */
	public double cYToLat(int y, int zoom) {
		y += falseNorthing(zoom);
		double latitude = (Math.PI / 2)
				- (2 * Math.atan(Math.exp(-1.0 * y / radius(zoom))));
		return -1 * Math.toDegrees(latitude);
	}

	public int getTileSize() {
		return tileSize;
	}

	public double[] calculateLatLon(int zoom, int tilex, int tiley) {
		int tileSize = this.getTileSize();
		double[] result = new double[4];
		tilex *= tileSize;
		tiley *= tileSize;
		result[0] = this.cXToLon(tilex, zoom); // lon_min
		result[1] = this.cYToLat(tiley + tileSize, zoom); // lat_max
		result[2] = this.cXToLon(tilex + tileSize, zoom); // lon_min
		result[3] = this.cYToLat(tiley, zoom); // lat_max
		return result;
	}

}
