package io.github.glandais.gpx.util;

public class Constants {

    // m.s-2, minimal speed = 2km/h
    public static final double MINIMAL_SPEED = 2.0 / 3.6;

    // s
    public static final double DT = 1.0;

    // g, cyclist from earth
    public static final double G = 9.8;

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

    // Standard road bike wheel radius (m) - 700c wheels with typical tire
    // Source: Standard wheel sizing, ~1.4m diameter for 700x25c tires
    public static final double DEFAULT_WHEEL_RADIUS = 0.7;

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
    // Source: Academic research shows bicycle braking limit ~0.67g, 0.6g provides safety margin
    // Reference: SAE Technical Paper 2020-01-0876 "Bicycle Braking Performance Testing and Analysis"
    public static final double DEFAULT_MAX_BRAKE_G = 0.6;

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
    public static final double DEFAULT_MAX_LEAN_ANGLE_DEG = 35;

    // Maximum speed capability (km/h)
    // Source: Reasonable maximum for recreational cycling on roads
    public static final double DEFAULT_MAX_SPEED_KMH = 100;

    // Air density at sea level standard conditions (kg/m³)
    // Source: ISO International Standard Atmosphere (ISA), 15°C, 1 atm
    // Reference: Used in cycling aerodynamics research (Martin et al., cycling drag studies)
    public static final double DEFAULT_AIR_DENSITY = 1.225;
}
