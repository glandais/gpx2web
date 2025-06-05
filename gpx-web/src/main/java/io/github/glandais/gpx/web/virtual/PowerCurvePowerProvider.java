package io.github.glandais.gpx.web.virtual;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.cyclist.CyclistPowerProviderBase;
import io.github.glandais.gpx.web.model.PowerCurvePoint;
import java.util.Comparator;
import java.util.List;

public class PowerCurvePowerProvider extends CyclistPowerProviderBase {

    private final List<PowerCurvePoint> powerCurve;

    public PowerCurvePowerProvider(List<PowerCurvePoint> powerCurve) {
        super();
        this.powerCurve = powerCurve;
        // Ensure power curve is sorted by distance
        this.powerCurve.sort(Comparator.comparingDouble(PowerCurvePoint::distanceKm));
    }

    @Override
    protected double getOptimalPower(Course course, Point location) {
        double distanceKm = location.getDist() / 1000.0; // Convert meters to kilometers

        // Handle edge cases
        if (powerCurve.isEmpty()) {
            return 250.0; // Default power
        }

        if (powerCurve.size() == 1) {
            return powerCurve.get(0).powerW();
        }

        // If distance is before first point, use first point's power
        if (distanceKm <= powerCurve.get(0).distanceKm()) {
            return powerCurve.get(0).powerW();
        }

        // If distance is after last point, use last point's power
        if (distanceKm >= powerCurve.get(powerCurve.size() - 1).distanceKm()) {
            return powerCurve.get(powerCurve.size() - 1).powerW();
        }

        // Find the two points to interpolate between
        for (int i = 0; i < powerCurve.size() - 1; i++) {
            PowerCurvePoint point1 = powerCurve.get(i);
            PowerCurvePoint point2 = powerCurve.get(i + 1);

            if (distanceKm >= point1.distanceKm() && distanceKm <= point2.distanceKm()) {
                // Linear interpolation
                double ratio = (distanceKm - point1.distanceKm()) / (point2.distanceKm() - point1.distanceKm());
                return point1.powerW() + ratio * (point2.powerW() - point1.powerW());
            }
        }

        // Fallback (should not reach here)
        return powerCurve.get(powerCurve.size() - 1).powerW();
    }
}
