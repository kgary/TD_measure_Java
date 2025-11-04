import java.util.*;

public class UniqueSpeechComponentsAmiNoInt {
    /**
     * Mirrors JS unique_speech_components_ami_noInt: tags components, slides window, computes AMI, builds summary.
     * @param T1 2D int array [time][channel] of 0/1
     * @param ws window size (must be even)
     * @return List<Object> containing [double[][] amiSeries, Object[][] tableAMI]
     */
    public static List<Object> uniqueSpeechComponentsAmiNoInt(int[][] T1, int ws) {
        int numChannels = T1[0].length;
        int speechSeriesLength = T1.length;

        // Tag each '1' with channel ID in-place
        int[][] compSeries = T1;
        for (int i = 0; i < numChannels; i++) {
            for (int j = 0; j < speechSeriesLength; j++) {
                if (T1[j][i] == 1) {
                    compSeries[j][i] = i + 1;
                }
            }
        }

        int halfWs = ws / 2;
        double[][] amiSeries = new double[speechSeriesLength - halfWs][numChannels];
        for (double[] row : amiSeries) {
            Arrays.fill(row, 0.0);
        }

        // Computed pairwise AMI in sliding windows
        for (int i = 0; i < numChannels; i++) {
            for (int j = 0; j < numChannels; j++) {
                if (i != j) {
                    // Builds two-column time series as doubles
                    double[][] xy = new double[speechSeriesLength][2];
                    for (int k = 0; k < speechSeriesLength; k++) {
                        xy[k][0] = compSeries[k][i];
                        xy[k][1] = compSeries[k][j];
                    }

                    // Sliding window
                    for (int l = halfWs; l < speechSeriesLength - halfWs; l++) {
                        int start = l - halfWs;
                        int endExcl = l + halfWs + 1;
                        double[][] windowData = new double[endExcl - start][2];
                        for (int m = start; m < endExcl; m++) {
                            windowData[m - start][0] = xy[m][0];
                            windowData[m - start][1] = xy[m][1];
                        }

                        // Call AMI (expects int[][] for bins)
                        Object[] res = ami.ami(windowData, new int[][]{{ws}}, 0);
                        double[][] amisMatrix = (double[][]) res[0];
                        // flatten matrix into 1D array
                        int totalLen = 0;
                        for (double[] rowArr : amisMatrix) totalLen += rowArr.length;
                        double[] amis = new double[totalLen];
                        int pos = 0;
                        for (double[] rowArr : amisMatrix) {
                            for (double v : rowArr) {
                                amis[pos++] = v;
                            }
                        }

                        // Average and accumulate
                        double sum = 0.0;
                        for (double v : amis) sum += v;
                        double avg = sum / amis.length;
                        avg = Math.round(avg * 10000) / 10000.0;
                        amiSeries[l][i] += avg;
                    }
                }
            }
        }

        // Build summary table
        Object[][] tableAMI = new Object[numChannels][2];
        for (int i = 0; i < numChannels; i++) {
            tableAMI[i][0] = "Channel" + (i + 1);
            double sumCol = 0.0;
            for (double[] row : amiSeries) sumCol += row[i];
            double avgCol = sumCol / amiSeries.length;
            avgCol = Math.round(avgCol * 10000) / 10000.0;
            tableAMI[i][1] = avgCol;
        }

        // Log the AMI series with 4-decimal formatting
        System.out.println("[");
        for (int r = 0; r < amiSeries.length; r++) {
            System.out.print("  [");
            for (int c = 0; c < numChannels; c++) {
                System.out.printf("%.4f", amiSeries[r][c]);
                if (c < numChannels - 1) System.out.print(", ");
            }
            System.out.print("]");
            if (r < amiSeries.length - 1) System.out.println(",");
        }
        System.out.println("\n]");
        return Arrays.<Object>asList(amiSeries, tableAMI);
    }

    public static void main(String[] args) throws Exception {
        // Load T via CSV reader and histogram function
        List<Map<String, String>> T = CreateBitfieldVector.readCsvFile("BinaryMatrix5911_v2_columns(Columns).csv");
        List<Object> hist = SpeakerHistogramsTDMSNoInt.speakerHistogramsTDMS_noInt(T, 1, T.size());
        int[][] T1 = (int[][]) hist.get(0);
        uniqueSpeechComponentsAmiNoInt(T1, 10);
    }
}
