package io.github.glandais.gpx.web.service;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.virtual.Bike;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.Cyclist;
import io.github.glandais.gpx.virtual.GPXEnhancer;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.Wind;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderConstant;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import io.github.glandais.gpx.web.model.VirtualizationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class VirtualizationService {

    private final GPXFileReader gpxFileReader;
    private final GPXEnhancer gpxEnhancer;

    public GPX virtualizeGpx(byte[] gpxData, VirtualizationRequest request) throws IOException {
        GPX gpx = gpxFileReader.readGPX(new ByteArrayInputStream(gpxData));

        for (GPXPath gpxPath : gpx.paths()) {
            Cyclist cyclist = createCyclist(request.cyclist());
            Bike bike = createBike(request.bike());
            Wind wind = createWind(request.wind());

            Course course = new Course(
                    gpxPath,
                    request.startTime(),
                    cyclist,
                    bike,
                    new PowerProviderConstant(),
                    new WindProviderConstant(wind),
                    new AeroProviderConstant());

            gpxEnhancer.virtualize(course, false);
        }

        return gpx;
    }

    private Cyclist createCyclist(VirtualizationRequest.CyclistParameters params) {
        return new Cyclist(
                params.weightKg(),
                params.powerWatts(),
                params.harmonics(),
                params.maxBrakeG(),
                params.dragCoefficient(),
                params.frontalAreaM2(),
                params.maxAngleDeg(),
                params.maxSpeedKmH());
    }

    private Bike createBike(VirtualizationRequest.BikeParameters params) {
        return new Bike(
                params.rollingResistance(),
                params.frontWheelInertia(),
                params.rearWheelInertia(),
                params.wheelRadiusM(),
                params.efficiency());
    }

    private Wind createWind(VirtualizationRequest.WindParameters params) {
        double windDirectionRad = Math.toRadians(params.directionDeg());
        return new Wind(params.speedMs(), windDirectionRad);
    }
}