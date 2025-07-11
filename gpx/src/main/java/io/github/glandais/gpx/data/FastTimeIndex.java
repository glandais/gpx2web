package io.github.glandais.gpx.data;

/**
 * Fast index for GPXPath elapsed time lookups using binary search
 * This provides O(log n) lookup performance vs O(n) linear search
 */
public class FastTimeIndex {

    private final double[] elapsedSeconds;
    private final boolean isSorted;

    public FastTimeIndex(GPXPath path) {
        if (path.getPoints().isEmpty()) {
            this.elapsedSeconds = new double[0];
            this.isSorted = true;
            return;
        }

        this.elapsedSeconds = new double[path.getPoints().size()];

        // Extract elapsed seconds and check if sorted
        boolean sorted = true;
        double previousTime = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < path.getPoints().size(); i++) {
            Double elapsed = path.getPoints().get(i).getElapsedSeconds();
            double time = elapsed != null ? elapsed : 0.0;

            this.elapsedSeconds[i] = time;

            if (time < previousTime) {
                sorted = false;
            }
            previousTime = time;
        }

        this.isSorted = sorted;
    }

    /**
     * Finds the index of the point at or before the given elapsed time
     * Uses binary search for O(log n) performance
     *
     * @param targetTime Target elapsed time in seconds
     * @return Index of the point at or before the target time, or -1 if before all points
     */
    public int findTimeIndex(double targetTime) {
        if (elapsedSeconds.length == 0) {
            return -1;
        }

        if (!isSorted) {
            // Fallback to linear search if not sorted
            return findTimeIndexLinear(targetTime);
        }

        // Binary search - optimized for sorted data
        int left = 0;
        int right = elapsedSeconds.length - 1;

        // Handle edge cases
        if (targetTime < elapsedSeconds[0]) {
            return -1;
        }
        if (targetTime >= elapsedSeconds[right]) {
            return right;
        }

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (elapsedSeconds[mid] == targetTime) {
                return mid;
            } else if (elapsedSeconds[mid] < targetTime) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // Return the index of the largest element smaller than targetTime
        return right;
    }

    /**
     * Linear search fallback for unsorted data
     */
    private int findTimeIndexLinear(double targetTime) {
        int bestIndex = -1;

        for (int i = 0; i < elapsedSeconds.length; i++) {
            if (elapsedSeconds[i] <= targetTime) {
                bestIndex = i;
            } else {
                break;
            }
        }

        return bestIndex;
    }

    /**
     * Gets the elapsed time at a specific index
     */
    public double getElapsedTime(int index) {
        if (index < 0 || index >= elapsedSeconds.length) {
            return Double.NaN;
        }
        return elapsedSeconds[index];
    }

    /**
     * Returns the size of the index
     */
    public int size() {
        return elapsedSeconds.length;
    }

    /**
     * Returns true if the data is sorted (enables binary search optimization)
     */
    public boolean isSorted() {
        return isSorted;
    }
}
