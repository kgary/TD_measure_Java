package teamDynamics;


import java.util.*;

public class LayeredDynamicRelaxationTimes {

    public static Integer[] LayeredDynamicsRelaxationTimes(int time1, int time2, double[] series) {
        // 1. Extract failure duration sub-array
        double[] failureDuration = Arrays.copyOfRange(series, time1, time2 + 1);

        // 2. Calculate 99th percentile
        double percentileValue = percentile99(failureDuration);

        // 3. Collect indices above or equal to percentile
        List<Integer> aboveThreshTimes = new ArrayList<>();
        List<Double> crossings = new ArrayList<>();
        for (int i = 0; i < failureDuration.length; i++) {
            if (failureDuration[i] >= percentileValue) {
                aboveThreshTimes.add(i);
                crossings.add(failureDuration[i]);
            }
        }

        // 4. rt_init: first index above threshold
        if (aboveThreshTimes.isEmpty()) {
            return null; // or some other indication of no valid times
        }

        Integer rt_init = aboveThreshTimes.isEmpty() ? null : aboveThreshTimes.get(0);

        // 5. rt_peak (Note: contains logical error as per JS version)
        List<Integer> rt_peak = new ArrayList<>();
        for (int i = 0; i < failureDuration.length; i++) {
            // TODO: revisit logic
            if (failureDuration[i] - 0 != 0) {
                rt_peak.add(i);
            }
        }

        Integer rt_peak_value = rt_peak.isEmpty() ? null : rt_peak.get(0);

        // 6. rt_last: last index above threshold
        Integer rt_last = aboveThreshTimes.isEmpty() ? null : aboveThreshTimes.get(aboveThreshTimes.size() - 1);

        return new Integer[]{rt_init, rt_peak_value, rt_last};
    }

    // Helper to compute the 99th percentile (rounded down)
    public static double percentile99(double[] arr) {
        double[] sorted = arr.clone();
        Arrays.sort(sorted);
        int index = (int) Math.ceil(0.99 * sorted.length) - 1;
        return sorted[Math.max(index, 0)];
    }

    // public static void main(String[] args) {
    //     double[] series = {0.0,0.0,0.0,0.0,0.0,0.0,0.8113,1.5,1.5,0.8113,0.8113,1.5,2.0,1.5,1.5,1.5,0.8113,0.0,0.0,0.0,0.8113,1.5,1.5,0.8113,0.8113,1.5,2.0,1.5,1.5,1.5,0.8113,0.0,0.0,0.0,0.8113,1.5,1.5,0.8113,0.8113,1.5,2.0,1.5};
    //     Integer[] result = LayeredDynamicsRelaxationTimes(0, 6, series);
    //     if (result == null) {
    //         System.out.println("No valid relaxation times found");
    //     } else {
    //         System.out.println("[" + result[0] + ", " + result[1] + ", " + result[2] + "]");
    //     }
    // }
}