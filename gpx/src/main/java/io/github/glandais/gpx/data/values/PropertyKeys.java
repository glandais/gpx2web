package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.unit.*;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyKeys {

    static final Set<PropertyKey<?, ?>> SET = new LinkedHashSet<>();

    public static final PropertyKey<Double, DoubleUnit> aeroCoef =
            getPropertyKey(PropertyKeyEnum.aeroCoef, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> bearing =
            getPropertyKey(PropertyKeyEnum.bearing, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> cadence =
            getPropertyKey(PropertyKeyEnum.cadence, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> dist =
            getPropertyKey(PropertyKeyEnum.dist, DoubleUnit.INSTANCE);
    public static final PropertyKey<Duration, DurationUnit> elapsed =
            getPropertyKey(PropertyKeyEnum.elapsed, DurationUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> ele = getPropertyKey(PropertyKeyEnum.ele, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> grade =
            getPropertyKey(PropertyKeyEnum.grade, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> lat = getPropertyKey(PropertyKeyEnum.lat, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> lon = getPropertyKey(PropertyKeyEnum.lon, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> p_cyclist_current_speed =
            getPropertyKey(PropertyKeyEnum.p_cyclist_current_speed, SpeedUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> p_cyclist_optimal_power =
            getPropertyKey(PropertyKeyEnum.p_cyclist_optimal_power, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> p_cyclist_optimal_speed =
            getPropertyKey(PropertyKeyEnum.p_cyclist_optimal_speed, SpeedUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> p_cyclist_raw =
            getPropertyKey(PropertyKeyEnum.p_cyclist_raw, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> p_cyclist_wheel =
            getPropertyKey(PropertyKeyEnum.p_cyclist_wheel, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> p_power_from_acc =
            getPropertyKey(PropertyKeyEnum.p_power_from_acc, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> p_power_wheel_from_acc =
            getPropertyKey(PropertyKeyEnum.p_power_wheel_from_acc, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> power =
            getPropertyKey(PropertyKeyEnum.power, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> radius =
            getPropertyKey(PropertyKeyEnum.radius, DoubleUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> speed =
            getPropertyKey(PropertyKeyEnum.speed, SpeedUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> speed_max =
            getPropertyKey(PropertyKeyEnum.speed_max, SpeedUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> speed_max_incline =
            getPropertyKey(PropertyKeyEnum.speed_max_incline, SpeedUnit.INSTANCE);
    public static final PropertyKey<Double, DoubleUnit> temperature =
            getPropertyKey(PropertyKeyEnum.temperature, DoubleUnit.INSTANCE);
    public static final PropertyKey<Instant, InstantUnit> time =
            getPropertyKey(PropertyKeyEnum.time, InstantUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> wind_alpha =
            getPropertyKey(PropertyKeyEnum.wind_alpha, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> wind_bearing =
            getPropertyKey(PropertyKeyEnum.wind_bearing, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, AngleUnit> wind_direction =
            getPropertyKey(PropertyKeyEnum.wind_direction, AngleUnit.INSTANCE);
    public static final PropertyKey<Double, SpeedUnit> wind_speed =
            getPropertyKey(PropertyKeyEnum.wind_speed, SpeedUnit.INSTANCE);

    public static final PropertyKey<Double, SpeedUnit> virt_speed_current =
            getPropertyKey(PropertyKeyEnum.virt_speed_current, SpeedUnit.INSTANCE);

    private static <S, U extends Unit<S>> PropertyKey<S, U> getPropertyKey(PropertyKeyEnum propertyKeyEnum, U unit) {
        PropertyKey<S, U> propertyKey = new PropertyKey<>(propertyKeyEnum, unit);
        if (!SET.add(propertyKey)) {
            throw new IllegalArgumentException(propertyKey + " already exists");
        }
        return propertyKey;
    }

    public static Set<PropertyKey<?, ?>> getList() {
        return SET;
    }
}
