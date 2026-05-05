# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Run a Single Test
```bash
mvn test -Dtest=ClimbDetectorTest
```

### Code Formatting
```bash
mvn spotless:apply
```

### Build CLI Application
```bash
cd gpxtools-cli
mvn clean package
```

### Build Native CLI
```bash
cd gpxtools-cli
mvn clean package -Pnative
```

### Run CLI
```bash
java -jar gpxtools-cli/target/quarkus-app/quarkus-run.jar
```

## Architecture

This is a multi-module Maven project for GPX cycling data processing and virtual cycling simulation:

### Core Modules

**gpx** - Core library containing:
- GPX file I/O (read/write GPX, FIT, CSV, JSON formats)
- Elevation fixing using SRTM data via GraphHopper
- Virtual cyclist simulation with physics-based power calculations
- Climb detection and analysis
- Map generation (static maps with elevation/tiles)
- Data filtering and simplification

**gpxtools-cli** - Quarkus-based CLI application with three main commands:
- `process` - Virtual power/speed calculations
- `export` - Multi-format export with map generation
- `virtualize` - Complete virtual ride simulation

### Key Architecture Components

**Virtual Cyclist Physics Engine** (`virtual/` package):
- Multi-pass speed calculation (forward for cornering limits, backward for braking)
- Physics-based power modeling: aerodynamic drag, rolling resistance, gravity, wheel bearings
- Cyclist and bike parameter modeling with realistic constraints

**MaxSpeedComputer** (`virtual/maxspeed/`):
- Two-pass algorithm: forward pass for cornering limits, backward pass for braking constraints
- Cornering physics: `v_max = √(g × r × tan(θ_max))` with 3-point curvature analysis
- Braking physics: kinematic equations with 0.6g deceleration limit
- GPS coordinate transformation for geometric calculations
- Safety margins: +2m radius buffer, conservative braking coefficients

**PowerComputer Core Engine** (`virtual/power/`):
- Implements fundamental physics equations for power-speed relationships
- Equivalent mass calculation: `M_eq = m + I_total/r²` (includes rotational inertia)
- Kinetic energy power equation: `P = 0.5 * M_eq * (v₂² - v₁²) / Δt`
- Energy conservation: `P_cyclist = P_resistances + dE/dt`
- Numerical integration with physical constraints (min speed, positive power)
- Validated against Martin et al. (1998) cycling power research (R² = 0.97)

**Power Resistance Components** (`virtual/power/` package):
- **Rolling Resistance** (`rolling/`): `RollingResistancePowerProvider` - tire deformation resistance adjusted for grade using `-cos(atan(grade)) * mass * g * speed * crr`
- **Wheel Bearings** (`rolling/`): `WheelBearingsPowerProvider` - bearing friction losses using quadratic speed model `-speed * (91 + 8.7 * speed) / 1000`
- **Gravity** (`grav/`): `GravPowerProvider` - gravitational power for climbs/descents using `-sin(atan(grade)) * mass * g * speed`
- **Aerodynamic Drag** (`aero/`): `AeroPowerProvider` - air resistance with cubic speed relationship
  - No wind: `-aeroCoef * speed³` using standard `P = 0.5 × ρ × Cd × A × v³` formula
  - With wind: Implements Isvan (2011) wind model with apparent wind calculation and aerodynamic aspect ratio corrections
- All components return negative power values representing energy losses (positive for gravity on descents)

**Elevation Processing** (`srtm/` package):
- SRTM data integration via GraphHopper for elevation fixing
- Smooth elevation computation and total elevation calculation

**Climb Detection** (`climb/` package):
- Algorithmic climb detection with configurable parameters
- Climb analysis and segmentation

**Data Model** (`data/` package):
- Flexible property system with unit conversion
- Type-safe value handling with converters (degrees, duration, semicircles)
- Point-based GPS track representation

The virtual cyclist simulation uses realistic physics calculations including bike leaning dynamics for cornering, kinematic braking models, and comprehensive power modeling based on academic cycling research.

**GPXEnhancer Pipeline Orchestrator** (`virtual/`):
- 6-step virtualization pipeline: distance resampling → SRTM elevation → speed limits → physics simulation → temporal resampling → Douglas-Peucker filtering
- Data preprocessing: 10m distance intervals, ±1m SRTM elevation accuracy vs ±10-15m GPS
- Multi-track support with temporal consistency across multi-day routes
- Quality assurance with validation and error handling at each pipeline step

**VirtualizeService** (`virtual/`):
- Complete course simulation orchestrator using time-stepping numerical integration
- Modified trapezoidal rule for 2nd-order accuracy and A-stability
- Adaptive step size handling: GPS waypoint alignment + regular time steps
- Power-speed integration with physical constraints from MaxSpeedComputer
- Binary search O(log n) distance lookup for efficient GPS point indexing
- Post-processing cyclist power calculation accounting for all resistances

## Default Parameters

All default values for virtual cyclist simulation have been extracted to `Constants.java` and validated against academic sources:

**Bicycle defaults:** Rolling resistance (0.004), wheel inertia (0.05/0.07 kg⋅m²), wheel radius (0.7m), drivetrain efficiency (97.6%), air density (1.225 kg/m³)

**Cyclist defaults:** Mass (80kg), power (280W = 3.5 W/kg), braking (0.6g), aerodynamics (Cd=0.7, A=0.5m²), lean angle (35°), max speed (100km/h)

Sources include bicyclerollingresistance.com, SAE braking research, aerodynamics studies, ISO Standard Atmosphere, and cycling physics literature.