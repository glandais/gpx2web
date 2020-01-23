package io.github.glandais.gpx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GPXFilter {

	public void filterPointsDouglasPeucker(GPXPath path) {
		simplify(path, false, true);
		path.computeArrays();
	}

	public void filterPointsDistance(GPXPath path) {
		simplify(path, true, false);
		path.computeArrays();
	}

	private void simplify(GPXPath path, boolean radial, boolean douglasPeucker) {
		List<Point> points = path.getPoints();

		log.info("Filtering {} ({})", path.getName(), points.size());
		List<Point> newPoints = simplify(points, 0.0001, radial, douglasPeucker);
		path.setPoints(newPoints);
		log.info("Filtered {} ({} -> {})", path.getName(), points.size(), newPoints.size());
	}

	/**
	 * Reduces the number of points in a polyline while retaining its shape, giving
	 * a performance boost when processing it and also reducing visual noise.
	 *
	 * @param points         an array of points
	 * @param tolerance      affects the amount of simplification (in the same
	 *                       metric as the point coordinates)
	 * @param highestQuality excludes distance-based preprocessing step which leads
	 *                       to highest quality simplification
	 * @return an array of simplified points
	 * @see <a href="http://mourner.github.io/simplify-js/">JavaScript
	 *      implementation</a>
	 * @since 1.2.0
	 */
	protected List<Point> simplify(List<Point> points, double tolerance, boolean radial, boolean douglasPeucker) {
		if (points.size() <= 2) {
			return points;
		}

		double sqTolerance = tolerance * tolerance;

		points = radial ? simplifyRadialDist(points, sqTolerance) : points;
		points = douglasPeucker ? simplifyDouglasPeucker(points, sqTolerance) : points;

		return points;
	}

	/**
	 * Basic distance-based simplification.
	 *
	 * @param points      a list of points to be simplified
	 * @param sqTolerance square of amount of simplification
	 * @return a list of simplified points
	 */
	private static List<Point> simplifyRadialDist(List<Point> points, double sqTolerance) {
		Point prevPoint = points.get(0);
		ArrayList<Point> newPoints = new ArrayList<>();
		newPoints.add(prevPoint);
		Point point = null;

		for (int i = 1, len = points.size(); i < len; i++) {
			point = points.get(i);

			if (getSqDist(point, prevPoint) > sqTolerance) {
				newPoints.add(point);
				prevPoint = point;
			}
		}

		if (!prevPoint.equals(point)) {
			newPoints.add(point);
		}
		return newPoints;
	}

	/**
	 * Square distance between 2 points.
	 *
	 * @param p1 first {@link Point}
	 * @param p2 second Point
	 * @return square of the distance between two input points
	 */
	private static double getSqDist(Point p1, Point p2) {
		double dx = p1.getLon() - p2.getLon();
		double dy = p1.getLat() - p2.getLat();
		return dx * dx + dy * dy;
	}

	/**
	 * Square distance from a point to a segment.
	 *
	 * @param point {@link Point} whose distance from segment needs to be determined
	 * @param       p1,p2 points defining the segment
	 * @return square of the distance between first input point and segment defined
	 *         by other two input points
	 */
	private static double getSqSegDist(Point point, Point p1, Point p2) {
		double horizontal = p1.getLon();
		double vertical = p1.getLat();
		double diffHorizontal = p2.getLon() - horizontal;
		double diffVertical = p2.getLat() - vertical;

		if (diffHorizontal != 0 || diffVertical != 0) {
			double total = ((point.getLon() - horizontal) * diffHorizontal + (point.getLat() - vertical) * diffVertical)
					/ (diffHorizontal * diffHorizontal + diffVertical * diffVertical);
			if (total > 1) {
				horizontal = p2.getLon();
				vertical = p2.getLat();

			} else if (total > 0) {
				horizontal += diffHorizontal * total;
				vertical += diffVertical * total;
			}
		}

		diffHorizontal = point.getLon() - horizontal;
		diffVertical = point.getLat() - vertical;

		return diffHorizontal * diffHorizontal + diffVertical * diffVertical;
	}

	private static List<Point> simplifyDpStep(List<Point> points, int first, int last, double sqTolerance,
			List<Point> simplified) {
		double maxSqDist = sqTolerance;
		int index = 0;

		ArrayList<Point> stepList = new ArrayList<>();

		for (int i = first + 1; i < last; i++) {
			double sqDist = getSqSegDist(points.get(i), points.get(first), points.get(last));
			if (sqDist > maxSqDist) {
				index = i;
				maxSqDist = sqDist;
			}
		}

		if (maxSqDist > sqTolerance) {
			if (index - first > 1) {
				stepList.addAll(simplifyDpStep(points, first, index, sqTolerance, simplified));
			}

			stepList.add(points.get(index));

			if (last - index > 1) {
				stepList.addAll(simplifyDpStep(points, index, last, sqTolerance, simplified));
			}
		}

		return stepList;
	}

	/**
	 * Simplification using Ramer-Douglas-Peucker algorithm.
	 *
	 * @param points      a list of points to be simplified
	 * @param sqTolerance square of amount of simplification
	 * @return a list of simplified points
	 */
	private static List<Point> simplifyDouglasPeucker(List<Point> points, double sqTolerance) {
		int last = points.size() - 1;
		ArrayList<Point> simplified = new ArrayList<>();
		simplified.add(points.get(0));
		simplified.addAll(simplifyDpStep(points, 0, last, sqTolerance, simplified));
		simplified.add(points.get(last));
		return simplified;
	}
}
