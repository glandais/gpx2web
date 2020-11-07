package io.github.glandais.virtual;

import lombok.Data;

@Data
public class CyclistStatus {

    // m
    double odo = 0.0;
    // s
    double ellapsed = 0.0;
    // m.s-2
    double speed = 0.0;

}
