package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.convert.*;
import io.github.glandais.gpx.data.values.unit.*;

import java.time.Instant;
import java.util.Date;

public interface Unit<J> {
    // unscalable unit
    StorageUnit<Double> DOUBLE_ANY = new DoubleAnyUnit();

    // instant
    StorageUnit<Instant> INSTANT = new InstantUnit();
    Unit<Long> EPOCH_MILLIS = new EpochMillisUnit();
    Unit<Double> EPOCH_SECONDS = new EpochSecondsUnit();
    Unit<Date> DATE = new DateUnit();

    // duration
    StorageUnit<Double> SECONDS = new DurationUnit();

    // length
    StorageUnit<Double> METERS = new LengthUnit();

    // speed
    StorageUnit<Double> SPEED_S_M = new SpeedUnit();

    // angle
    StorageUnit<Double> RADIANS = new AngleUnit();
    Unit<Double> DEGREES = new DegreesUnit();
    Unit<Integer> SEMI_CIRCLE = new SemiCirclesUnit();

    // grade
    StorageUnit<Double> PERCENTAGE = new PercentageUnit();

    // power
    StorageUnit<Double> WATTS = new WattsUnit();

    // aeroCoef
    StorageUnit<Double> AERO_COEF = new CxUnit();

    // formula
    StorageUnit<Formul> FORMULA_WATTS = new FormulaUnit(WATTS);
    StorageUnit<Formul> FORMULA_SPEED_S_M = new FormulaUnit(SPEED_S_M);
    StorageUnit<Formul> FORMULA_METERS = new FormulaUnit(METERS);

}
