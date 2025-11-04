import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SpeakerHistogramsTDMSNoInt {
    /**
     * Mirrors JS speakerHistogramsTDMS_noInt: builds a time-windowed submatrix and channel histograms.
     * @param T       List of row maps (header → "0"/"1")
     * @param timeOn  1-based start index inclusive
     * @param timeOff 1-based end index inclusive
     * @return        [int[][] T1, Object[][] T2]
     */
    public static List<Object> speakerHistogramsTDMS_noInt(List<Map<String,String>> T,
                                                           int timeOn,
                                                           int timeOff) {
        int nrow = T.size();
        int ncol = nrow > 0 ? T.get(0).size() : 0;
        List<String> channels = nrow > 0 ? new ArrayList<>(T.get(0).keySet())
                                         : new ArrayList<>();
        // JS had `var a = Object.values(channels[0])` which is unused

        // Initializing channelCount
        int[] channelCount = new int[ncol];

        // Determining loop bounds (JS: size = min(timeOff, nrow))
        int size = timeOff < nrow ? timeOff : nrow;
        int length = size - timeOn + 1; // JS: Array(size - timeOn + 1)

        // Allocating T1: int[length][ncol]
        int[][] T1 = new int[length][ncol];
        int k = 0;
        for (int i = timeOn - 1; i < size; i++) {
            Map<String,String> row = T.get(i);
            int j = 0;
            for (String key : channels) {
                int val = 0;
                String s = row.get(key);
                if (s != null && !s.isEmpty()) {
                    try {
                        val = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        val = 0;
                    }
                }
                T1[k][j] = val;
                if (val == 1) {
                    channelCount[j]++;
                }
                j++;
            }
            k++;
        }

        // Allocating T2: Object[ncol][2]
        Object[][] T2 = new Object[ncol][2];
        for (int i = 0; i < ncol; i++) {
            T2[i][0] = channels.get(i);
            T2[i][1] = channelCount[i];
        }

        // Sorting T2 by count descending
        Arrays.sort(T2, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] a, Object[] b) {
                return ((Integer) b[1]) - ((Integer) a[1]);
            }
        });

        
        //System.out.println(Arrays.deepToString(T2));
        

        return Arrays.<Object>asList(T1, T2);
    }

    public static void main(String[] args) throws IOException {
        // JS: var T = readFile("BinaryMatrix5911_v2_columns.xlsx");
        // Here, reusing the CSV reader from CreateBitfieldVector:
        List<Map<String,String>> T = CreateBitfieldVector.readCsvFile("BinaryMatrix5911_v2_columns(Columns).csv");
        speakerHistogramsTDMS_noInt(T, 1, T.size());
    }
}
