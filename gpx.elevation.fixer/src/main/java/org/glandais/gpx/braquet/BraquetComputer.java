package org.glandais.gpx.braquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.glandais.srtm.loader.Point;

public class BraquetComputer {

	private List<Braquet> braquets;

	public BraquetComputer() {
		super();
		braquets = new ArrayList<Braquet>();

		List<Pedalier> pedaliersSansDoublon = new ArrayList<Pedalier>();
		Pedalier[] pedaliers = Pedalier.values();
		for (Pedalier pedalier : pedaliers) {
			boolean add = true;
			for (Pedalier pedalierTest : pedaliersSansDoublon) {
				if (add) {
					if (Arrays.equals(pedalier.plateaux, pedalierTest.plateaux)) {
						add = false;
					}
				}
			}
			if (add) {
				pedaliersSansDoublon.add(pedalier);
			}
		}

		List<Cassette> cassettesSansDoublon = new ArrayList<Cassette>();
		Cassette[] cassettes = Cassette.values();
		for (Cassette cassette : cassettes) {
			boolean add = true;
			for (Cassette cassetteTest : cassettesSansDoublon) {
				if (add) {
					if (Arrays.equals(cassette.pignons, cassetteTest.pignons)) {
						add = false;
					}
				}
			}
			if (add) {
				cassettesSansDoublon.add(cassette);
			}
		}

		for (Pedalier pedalier : pedaliersSansDoublon) {
			for (Cassette cassette : cassettesSansDoublon) {
				Braquet braquet = new Braquet(pedalier, cassette);
				braquets.add(braquet);
			}
		}

	}

	public List<Braquet> getBraquets() {
		return braquets;
	}

	public void parseGPX(List<GPXPath> providedPaths, BraquetProgress progress) throws Exception {
		List<GPXPath> paths = new ArrayList<GPXPath>();
		for (GPXPath gpxPath : providedPaths) {
			paths.addAll(splitWithStops(gpxPath));
		}

		int k = 0;
		for (GPXPath gpxPath : paths) {
			//			gpxPath.filterPoints();
			//			gpxPath.computeArrays();
		}
		for (Braquet braquetDisp : braquets) {
			progress.progress(k, braquets.size());
			braquetDisp.reset();
			for (GPXPath gpxPath : paths) {
				tryBraquets(gpxPath, braquetDisp);
			}
			k++;
		}

		double[][] braquetRatios = new double[braquets.size()][];
		double[] minRatio = new double[10];
		double[] maxRatio = new double[10];
		int b = 0;
		for (Braquet braquetDisp : braquets) {
			braquetDisp.computeRpmStandardDeviation();
			braquetRatios[b] = braquetDisp.getRatios();
			int i = 0;
			for (double d : braquetRatios[b]) {
				if (d < minRatio[i] || b == 0) {
					minRatio[i] = d;
				}
				if (d > maxRatio[i] || b == 0) {
					maxRatio[i] = d;
				}
				i++;
			}
			b++;
		}
		b = 0;
		for (Braquet braquetDisp : braquets) {
			for (int i = 0; i < braquetRatios[b].length; i++) {
				if (maxRatio[i] == minRatio[i]) {
					braquetRatios[b][i] = 1.0;
				} else {
					braquetRatios[b][i] = (braquetRatios[b][i] - minRatio[i]) / (maxRatio[i] - minRatio[i]);
				}
			}
			braquetDisp.setNormRatios(braquetRatios[b]);
			b++;
		}
	}

	public List<GPXPath> splitWithStops(GPXPath gpxPath) {
		List<GPXPath> result = new ArrayList<GPXPath>();
		int ipath = 1;

		List<Point> curPoints = new ArrayList<Point>();
		Point lastPoint = null;
		for (int i = 0; i < gpxPath.getPoints().size(); i++) {
			Point p = gpxPath.getPoints().get(i);
			if (lastPoint != null) {
				double dist = lastPoint.distanceTo(p);
				long time = p.getTime() - lastPoint.getTime();
				// a stop is not moving for a long time (2m in 10s)
				if (dist < 0.01 && time >= 5000) {
					ipath = addPath(gpxPath, result, ipath, curPoints);
					curPoints = new ArrayList<Point>();
				} else {
					if (curPoints.size() == 0) {
						curPoints.add(lastPoint);
					}
					curPoints.add(p);
				}
			}
			lastPoint = p;
		}
		addPath(gpxPath, result, ipath, curPoints);
		// for (GPXPath path : result) {
		// System.out.println(path.points.get(0).distanceTo(path.points.get(path.points.size()
		// - 1)));
		// List<Point> pathPoints = path.points;
		// for (int i = 1; i < pathPoints.size(); i++) {
		// Point p1 = pathPoints.get(i - 1);
		// Point p2 = pathPoints.get(i);
		// long dt = p2.getTime() - p1.getTime();
		// double speed = p1.distanceTo(p2) / (dt / 3600000.0);
		// System.out.println("- " + p1.distanceTo(p2) + " (" + dt +
		// ") " + speed);
		// }
		// }
		return result;
	}

	private int addPath(GPXPath sourcePath, List<GPXPath> result, int ipath, List<Point> points) {
		if (points.size() > 0) {
			/*
			List<Point> filteredPoints = GPXPath.filterPoints(points);
			if (filteredPoints.size() > 0) {
			
				long startTime = filteredPoints.get(0).getTime() + 15000;
				long endTime = filteredPoints.get(filteredPoints.size() - 1).getTime() - 15000;
			
				List<Point> realPoints = new ArrayList<Point>();
				for (Point point : filteredPoints) {
					long time = point.getTime();
					if (time >= startTime && time <= endTime) {
						realPoints.add(point);
					}
				}
			
				if (realPoints.size() > 0) {
					GPXPath curPath = new GPXPath(sourcePath.getName() + " - " + ipath);
					curPath.getPoints().addAll(realPoints);
					curPath.computeArrays();
			
					double dist = curPath.getPoints().get(curPath.getPoints().size() - 1).getDist();
					long time = curPath.getPoints().get(curPath.getPoints().size() - 1).getTime()
							- curPath.getPoints().get(0).getTime();
			
					double speed = dist / (time / 3600000.0);
					if (dist > 0.1 && speed > 3) {
						result.add(curPath);
						ipath++;
					}
				}
			}
			*/
		}
		return ipath;
	}

	public void tryBraquets(GPXPath gpxPath, Braquet braquet) throws IOException {
		Point lastPoint = null;
		double[] speed = new double[gpxPath.getPoints().size() - 1];
		for (int i = 0; i < gpxPath.getPoints().size(); i++) {
			Point p = gpxPath.getPoints().get(i);
			if (i == 0) {
				lastPoint = p;
			} else {
				if (i > 1) {
					speed[i - 1] = speed[i - 2];
				} else {
					speed[i - 1] = 1.0;
				}
				double dist = lastPoint.distanceTo(p);
				long dt = p.getTime() - lastPoint.getTime();
				if (dt > 0 && dist > 0.002) {
					double invSpeed = (dt / 3600000.0) / dist;
					if (invSpeed > 0.0) {
						speed[i - 1] = invSpeed;
					}
				}
			}
			lastPoint = p;
		}
		lastPoint = gpxPath.getPoints().get(0);
		for (int i = 0; i < gpxPath.getPoints().size() - 1; i++) {
			Point p = gpxPath.getPoints().get(i + 1);
			long dt = p.getTime() - lastPoint.getTime();
			/*
			double invSpeed = gpxPath.computeNewValueTime(i, 15000, 15000, speed);
			double curSpeed = 1 / invSpeed;
			double dist = lastPoint.distanceTo(p);
			braquet.applySpeed(p, curSpeed, dt, dist);
			lastPoint = p;
			*/
		}

	}

}
