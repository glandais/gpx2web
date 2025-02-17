package io.github.glandais.guesser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.util.SmoothService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.function.Predicate;

@Singleton
@Slf4j
public class ConstantsGuesser {

    protected final ObjectMapper objectMapper;

    protected final PowerComputer powerComputer;

    protected final MaxSpeedComputer maxSpeedComputer;

    protected final SmoothService smoothService;

    public ConstantsGuesser(final ObjectMapper objectMapper,
                            final PowerComputer powerComputer,
                            final MaxSpeedComputer maxSpeedComputer,
                            final SmoothService smoothService) {

        this.objectMapper = objectMapper;
        this.powerComputer = powerComputer;
        this.maxSpeedComputer = maxSpeedComputer;
        this.smoothService = smoothService;
    }

    public Course guessWithPathWithPower(GPXPath original, Cyclist cyclist) throws IOException {
        smoothService.smoothEle(original, 100);
//        gradeService.computeGrade(original, ValueKind.computed);
//        speedService.computeSpeed(original, PointField.originalSpeed, ValueKind.computed);

        CourseWithScore course = new CourseWithScore(original, Instant.now(), cyclist, new PowerProviderFromData(), new WindProviderNone(), new CxProviderConstant());

        String originalJson = objectMapper.writeValueAsString(original);
        GPXPath simulated = objectMapper.readValue(originalJson, GPXPath.class);

        setScore(original, simulated, course);
        log.info("{}", cyclist);

        ConstantRange cxRange = new ConstantRange(0.05, 0.35);
        ConstantRange crrRange = new ConstantRange(0.0002, 0.015);
        ConstantRange mRange = new ConstantRange(50.0, 100.0);

        printRanges(cxRange, crrRange, mRange);

        CourseWithScore minCourse = null;
        int nSteps = 5;

        for (int l = 0; l < 4; l++) {
            minCourse = null;
            log.info("******************** {}", l);
            for (int i = 0; i < nSteps; i++) {
                double cx = cxRange.getValue(i, nSteps);
                for (int j = 0; j < nSteps; j++) {
                    double crr = crrRange.getValue(j, nSteps);
                    for (int k = 0; k < nSteps; k++) {
                        double m = mRange.getValue(k, nSteps);

                        Cyclist curCyclist = new Cyclist(m, 15, 90, 0.3, crr);
                        CourseWithScore current = new CourseWithScore(original, Instant.now(), curCyclist,
                                new PowerProviderFromData(),
                                new WindProviderNone(),
                                new CxProviderConstant(cx));
                        simulated = objectMapper.readValue(originalJson, GPXPath.class);

                        setScore(original, simulated, current);

                        log.debug("{}", current);
                        if (minCourse == null || current.getScore() < minCourse.getScore()) {
                            minCourse = current;
                        }
                    }
                }
            }
            double cx = ((CxProviderConstant) minCourse.getCxProvider()).getCx();
            double crr = minCourse.getCyclist().getCrr();
            double mKg = minCourse.getCyclist().getMKg();
            cxRange = new ConstantRange(cx - cxRange.getStep(nSteps),
                    cx + cxRange.getStep(nSteps));
            crrRange = new ConstantRange(crr - crrRange.getStep(nSteps),
                    crr + crrRange.getStep(nSteps));
            mRange = new ConstantRange(mKg - mRange.getStep(nSteps),
                    mKg + mRange.getStep(nSteps));
            printRanges(cxRange, crrRange, mRange);
        }

        return minCourse;
    }

    protected void printRanges(ConstantRange cxRange, ConstantRange crrRange, ConstantRange mRange) {
        log.info("cx {}", cxRange);
        log.info("crr {}", crrRange);
        log.info("m {}", mRange);
    }

    protected void setScore(GPXPath original, GPXPath simulated, CourseWithScore course) {
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
//        speedService.computeSpeed(simulated, ValueKind.computed);
        double scoreCx = getScore(original, simulated, g -> Math.abs(g) < 0.5);
        double scoreM = getScore(original, simulated, g -> Math.abs(g) > 2.5);
        double scoreCrr = getScore(original, simulated, g -> true);
        course.setScore(scoreCx * scoreM * scoreCrr);
    }

    protected double getScore(GPXPath original, GPXPath simulated, Predicate<Double> predicate) {
        double s = 0;
        for (int i = 0; i < original.size(); i++) {
            Point originalPoint = original.getPoints().get(i);
            if (i > 0 && predicate.test(originalPoint.getGrade())) {
                Point simulatedPoint = simulated.getPoints().get(i);
                double dv = Math
                        .abs(originalPoint.getSpeed() - simulatedPoint.getSpeed());
                double dt = original.getTime()[i] - original.getTime()[i - 1];
                s = s + (dv * dt / 1000.0);
            }
        }
        return s;
    }
}
