import java.util.*;

public class LayeredDynamicRelaxationTimes {
    public static int[] LayeredDynamicsRelaxationTimes(int time1, int time2, int[] series) {
        // 1. Extract failure duration sub-array
        int[] failureDuration = Arrays.copyOfRange(series, time1, time2 + 1);

        // 2. Calculate 99th percentile
        int percentileValue = percentile99(failureDuration);

        // 3. Collect indices above or equal to percentile
        List<Integer> aboveThreshTimes = new ArrayList<>();
        List<Integer> crossings = new ArrayList<>();
        for (int i = 0; i < failureDuration.length; i++) {
            if (failureDuration[i] >= percentileValue) {
                aboveThreshTimes.add(i);
                crossings.add(failureDuration[i]);
            }
        }

        // 4. rt_init: first index above threshold
        int rt_init = aboveThreshTimes.get(0);

        // 5. rt_peak (Note: contains logical error as per JS version)
        List<Integer> rt_peak = new ArrayList<>();
        for (int i = 0; i < failureDuration.length; i++) {
            // TODO: revisit logic
            if (failureDuration[i] - 0 != 0) {  // TODO: revisit logic – matches JS bug behavior
                rt_peak.add(i);
            }
        }

        // 6. rt_last: last index above threshold
        int rt_last = aboveThreshTimes.get(aboveThreshTimes.size() - 1);

        return new int[]{rt_init, rt_peak.get(0), rt_last};
    }

    // Helper to compute the 99th percentile (rounded down)
    public static int percentile99(int[] arr) {
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        int index = (int) Math.ceil(0.99 * sorted.length) - 1;
        return sorted[Math.max(index, 0)];
    }

    public static void main(String[] args) {
        int[] series = {1,2,3,3,2,1,2,1,1};
        int[] result = LayeredDynamicsRelaxationTimes(0, 5, series);
        System.out.println("[" + result[0] + ", " + result[1] + ", " + result[2] + "]");
    }
}
