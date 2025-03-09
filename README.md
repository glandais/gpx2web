# GPX cycling tools

Java libraries available here :

```xml

<repositories>
    <repository>
        <id>gpx2web</id>
        <url>https://repo.repsy.io/mvn/glandais/gpx2web</url>
    </repository>
</repositories>
```

# GPX Stuff (gpx module)

- Read GPX files
- Write GPX files
- Write FIT file (single path)


- Fix elevation
- Smooth elevation, compute total elevation
- Detect climbs


- Simplify GPX file (both on location and elevation)


- Produce static map with map tiles
- Produce static map with elevation

# Power

Reference :

- https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf
- "Validation of a mathematical model for road cycling power"

## Wheel bearings

`-status.getSpeed() * (91 + 8.7 * status.getSpeed()) / 1000.0`

## Rolling resistance

```java
        final double mKg = course.getCyclist().getMKg();
final double crr = course.getCyclist().getCrr();
final double grade = location.getGrade();

double coef = Math.cos(Math.atan(grade));
double p_rr = -coef * mKg * Constants.G * status.getSpeed() * crr;
```

## Gravity

```java
        final double mKg = course.getCyclist().getMKg();
double grade = location.getGrade();
double coef = Math.sin(Math.atan(grade));
        return-mKg *Constants.G *status.

getSpeed() *coef;
```

## Air drag

Given cx

No wind : `p_air = -cx * speed * speed * speed`

With wind : to be reviewed

## Cyclist

# Speed given power

`p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT`

- p_sum : total powered applied (cyclist - resistance)
- mKg : total weight
- I1+I2 : wheels inertial moment ((0.05 + 0.07) kg.m2)
- r : wheel radius (0.7m)
- new_speed : speed after
- speed : speed before
- DT : epsilon

`new_speed_squared = DT * p_sum / (0.5 * (mKg + ((I1 + I2) / (r^2)))) + status.speed * status.speed;`

# Virtual cyclist

Simulates a virtual cyclist on a give path.

Steps :

- Fix elevation
- Compute max speeds given abilities
- Simulate a course given power
- Compute one point per second
- Simplify course

## Fix elevation

Uses skadi data, available through AWS : https://registry.opendata.aws/terrain-tiles/

[Graphhopper](https://www.graphhopper.com/) code is used to retrieve elevation for a point.

## Compute max speeds given abilities

### First pass (forward) :

- given 3 points, compute the curvature (circle going through the three points)
- https://en.wikipedia.org/wiki/Bicycle_and_motorcycle_dynamics#Leaning : ![img.png](img/leaning.png)
    - `vmax = Math.sqrt(Constants.G * radius * cyclist.getTanMaxAngle());`
- for each point, minimum of this vmax and maximum cyclist speed

### Second pass (backward) :

- Uses a max breaking constant for cyclist (a = 0.6 * g m.s-2 for instance)
- Using kinematic equations for linear motion
    - give distance d between the two points
    - v0 is max speed at point 1, vf is max speed at point 2
    - time to get from v0 to vf : `t = (vf - v0) / a`
    - if time is negative, no need to brake
    - else distance to brake : `dBrake = v0 * t + (a * t * t) / 2`
    - if dBrake < d : v0 is OK, brake is feasible
    - otherwise, new max speed at point 1 is `v0 = Math.sqrt(vf * vf - 2 * a * d)`

## Simulate a course given power

