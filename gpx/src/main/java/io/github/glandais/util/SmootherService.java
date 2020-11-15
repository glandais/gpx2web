package io.github.glandais.util;

public class SmootherService {

	public static double computeNewValue(int i, double dist, double[] data, double[] dists) {
		// double dsample = 1;

		double ac = dists[i];

		int mini = i - 1;
		while (mini >= 0 && (ac - dists[mini]) <= dist) {
			mini--;
		}
		mini++;

		int maxi = i + 1;
		while (maxi < data.length && (dists[maxi] - ac) <= dist) {
			maxi++;
		}

		double totc = 0;
		double totv = 0;
		for (int j = mini; j < maxi; j++) {
			double c = 1 - (Math.abs(dists[j] - ac) / dist);
			totc = totc + c;
			totv = totv + data[j] * c;
		}

		if (totc == 0) {
			return data[i];
		} else {
			return totv / totc;
		}

	}

}
