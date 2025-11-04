import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateBitfieldVector {
    /**
     * Reads a CSV file into a List of row maps (headers → values).
     */
    public static List<Map<String, String>> readCsvFile(String fileName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String headerLine = br.readLine();
            if (headerLine == null) return data;
            String[] headers = headerLine.split(",", -1);

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], i < cols.length ? cols[i] : "");
                }
                data.add(row);
            }
        }
        return data;
    }

    /**
     * Mirrors the JS createBitfieldVector logic using CSV input.
     * Returns a two-element List: [long[] bitfieldVec, String[] labelsVec].
     */
    public static List<Object> createBitfieldVector(String csvFile) throws IOException {
        List<Map<String, String>> data = readCsvFile(csvFile);
        int nrow = data.size();
        int numChannels = data.isEmpty() ? 0 : data.get(0).size();

        // Prepare power-of-two values and fixed-width binary strings
        long[] vec = new long[numChannels];
        String[] bitfields = new String[numChannels];
        List<String> labels = data.isEmpty() ? new ArrayList<>() : new ArrayList<>(data.get(0).keySet());
        for (int i = 0; i < numChannels; i++) {
            vec[i] = 1L << i;
            String bin = Long.toBinaryString(vec[i]);
            if (bin.length() < numChannels) {
                bin = String.join("", Collections.nCopies(numChannels - bin.length(), "0")) + bin;
            }
            bitfields[i] = bin;
        }

        // Outputs
        long[] bitfieldVec = new long[nrow];
        String[] labelsVec = new String[nrow];

        // Process each row
        for (int i = 0; i < nrow; i++) {
            Map<String, String> row = data.get(i);
            List<Integer> ind = new ArrayList<>();
            List<String> b0 = new ArrayList<>();
            int j = 0;
            for (String key : labels) {
                String valStr = row.get(key);
                double v = 0;
                if (valStr != null && !valStr.isEmpty()) {
                    try {
                        v = Double.parseDouble(valStr);
                    } catch (NumberFormatException e) {
                        v = 0;
                    }
                }
                if (v != 0) {
                    ind.add(j);
                    b0.add(bitfields[j]);
                }
                j++;
            }

            long binaryValue = 0;
            StringBuilder labelBuilder = new StringBuilder();
            for (int k = 0; k < ind.size(); k++) {
                binaryValue += Long.parseLong(b0.get(k));
                labelBuilder.append(labels.get(ind.get(k)));
                if (k < ind.size() - 1) labelBuilder.append(" + ");
            }

            bitfieldVec[i] = binaryValue;
            labelsVec[i] = labelBuilder.toString();
        }

        return Arrays.asList(bitfieldVec, labelsVec);
    }

    public static void main(String[] args) throws IOException {
        String csvFile = "BinaryMatrix5911_v2_columns(Columns).csv";
        List<Object> result = createBitfieldVector(csvFile);
        long[] bf = (long[]) result.get(0);
        String[] lv = (String[]) result.get(1);

        System.out.println("Bitfields:");
        System.out.println(Arrays.toString(bf));

        System.out.println("\nLabels:");
        System.out.println(Arrays.toString(lv));
    }
}
