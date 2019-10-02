package org.glandais.pbp;

import lombok.Data;

@Data
public class RiderState {

	private final RiderStatus status;

	private final Step step;

	private final double km;

}
