package io.github.glandais.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point2D {

	private double x;

	private double y;

	public Point2D sub(Point2D p) {
		return new Point2D(x - p.x, y - p.y);
	}

}
