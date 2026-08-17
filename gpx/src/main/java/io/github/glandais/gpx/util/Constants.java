package io.github.glandais.gpx.util;

public class Constants {

    // m.s-2, minimal speed = 2km/h
    public static final double MINIMAL_SPEED = 2.0 / 3.6;

    // s
    public static final double DT = 1.0;

    // g, cyclist from earth — standard gravity g0 (SI exact)
    // Was 9.8; the exact value removes a 0.07% systematic bias on the gravity and rolling terms.
    public static final double G = 9.80665;

    // WGS-84 semi-major axis (m)
    public static final double SEMI_MAJOR_AXIS = 6378137.0;

    // WGS-84 first eccentricity squared
    public static final double FIRST_ECCENTRICITY_SQUARED = 6.6943799901377997e-3;

    // Earth perimeter (m)
    public static final double CIRC = SEMI_MAJOR_AXIS * 2 * Math.PI;

    public static boolean DEBUG = false;

    // Default bicycle parameters (validated against academic sources)

    // Rolling resistance coefficient for road bike tires (dimensionless)
    // Source: bicyclerollingresistance.com research, typical range 0.003-0.005 for modern road tires
    public static final double DEFAULT_CRR = 0.004;

    // Wheel inertia moments (kg⋅m²) - typical values for lightweight racing wheels
    // Sources: Physics education materials, estimated for racing wheels with mass concentrated near rim
    public static final double DEFAULT_INERTIA_FRONT = 0.05; // Front wheel
    public static final double DEFAULT_INERTIA_REAR = 0.07; // Rear wheel (slightly heavier)

    // Standard road bike wheel radius (m) - 700c wheel with a 25mm tire
    // Source: Standard wheel sizing, ~0.7m diameter for 700x25c tires, so a 0.35m radius.
    // Martin et al. (1998) use r = 0.311m for a 20mm tire.
    // Was 0.7 until the research review: that is the *diameter*. The bug understated the
    // rotating mass in PowerComputer's equivalent mass (I/r^2) by ~0.73kg.
    public static final double DEFAULT_WHEEL_RADIUS = 0.35;

    // Drivetrain efficiency (dimensionless, 0-1)
    // Source: Typical modern road bike drivetrain efficiency
    public static final double DEFAULT_DRIVETRAIN_EFFICIENCY = 0.976;

    // Default cyclist parameters

    // Total system mass: cyclist + bike (kg)
    // Source: Typical recreational/competitive cyclist weight + ~8-10kg bike
    public static final double DEFAULT_CYCLIST_MASS_KG = 80;

    // Sustained power output (watts)
    // Source: Represents ~3.5 W/kg FTP for 80kg cyclist (intermediate/advanced recreational level)
    public static final double DEFAULT_CYCLIST_POWER_W = 280;

    // Maximum braking deceleration coefficient (g units)
    // Source: the pitch-over (stoppie) ceiling is 0.56-0.63g, but measured riders actually use
    // 0.41 +/- 0.07g in combined braking - about 60-65% of the limit.
    // Reference: SAE Technical Paper 2020-01-0876 "Bicycle Braking Performance Testing and Analysis"
    // 0.4 models a believable rider; 0.6 is the physical ceiling, i.e. an "expert descender".
    public static final double DEFAULT_MAX_BRAKE_G = 0.4;

    // Aerodynamic drag coefficient (dimensionless)
    // Source: Academic cycling aerodynamics research, typical range 0.6-0.8
    // Reference: "Aerodynamic drag in cycling: Methods of assessment" (ResearchGate)
    public static final double DEFAULT_DRAG_COEFFICIENT = 0.7;

    // Frontal area (m²)
    // Source: Cycling aerodynamics studies show range 0.394-0.531 m² for different positions
    // Reference: "Reference values and improvement of aerodynamic drag in professional cyclists"
    public static final double DEFAULT_FRONTAL_AREA = 0.5;

    // Maximum lean angle for cornering (degrees)
    // Source: Practical limit on crowned roads from cycling physics research
    // Reference: Brandt's analysis of bicycle cornering dynamics
    // Cornering uses v_max = sqrt(g * R * tan(theta)) = sqrt(mu * g * R) with mu == tan(theta),
    // so this parameter IS a tyre friction coefficient: 35 deg => mu = 0.70.
    // Zignoli (2020) measures mu = 0.90 dry (42.0 deg) and mu = 0.36 wet (19.8 deg) for road
    // tyres, so the default sits at 78% of dry grip - a confident rider leaving margin.
    public static final double DEFAULT_MAX_LEAN_ANGLE_DEG = 35;

    // Maximum speed capability (km/h)
    // Source: Reasonable maximum for recreational cycling on roads
    public static final double DEFAULT_MAX_SPEED_KMH = 100;

    // Air density at sea level standard conditions (kg/m³)
    // Source: ISO International Standard Atmosphere (ISA), 15°C, 1 atm
    // Reference: Used in cycling aerodynamics research (Martin et al., cycling drag studies)
    public static final double DEFAULT_AIR_DENSITY = 1.225;
}
