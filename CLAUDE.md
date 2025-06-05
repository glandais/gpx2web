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
java -jar gpxtools-cli/target/gpxtools-cli-runner.jar
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