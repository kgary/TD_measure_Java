package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class LayerStateExporter {

    public static void exportLayerStatesToCSV(
            List<List<List<String>>> layers,
            String sessionID,
            String outputDirectory
    ) throws IOException {

        String filename = outputDirectory + "/" + sessionID + "_layer_states.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

            // Write header
            writer.println(
                    "Time-step,"
                    + "Triage Status P1,Triage Status P2,Triage Status P3,"
                    + "Treatment P1,Treatment P2,Treatment P3,"
                    + "T1 looking at other teammates (AOI),T2 looking at other teammates (AOI),T3 looking at other teammates (AOI),"
                    + "T1 looking at AOIs (other than teammates),T2 looking at AOIs (other than teammates),T3 looking at AOIs (other than teammates),"
                    + "T1 looking at OOIs,T2 looking at OOIs,T3 looking at OOIs,"
                    + "T1 present in LOIs,T2 present in LOIs,T3 present in LOIs,"
                    + "T1 speaking,T2 speaking,T3 speaking"
            );

            // Process each time step
            for (int t = 0; t < layers.size(); t++) {
                List<List<String>> timeInstance = layers.get(t);

                // Extract layers
                List<String> communication = timeInstance.get(0); // [47, 49, 51]
                List<String> visual = timeInstance.get(1);        // [232934, 253136, 273339]
                List<String> casualty = timeInstance.get(2);      // [0117, 0619]
                List<String> movement = timeInstance.get(3);      // [40, 42, 44]

                // Build CSV row
                StringBuilder row = new StringBuilder();
                row.append(t); // Time-step

                // Casualty Layer (Triage + Treatment for up to 3 casualties)
                row.append(",").append(decodeCasualtyTriage(casualty, 0));
                row.append(",").append(decodeCasualtyTriage(casualty, 1));
                row.append(",").append(decodeCasualtyTriage(casualty, 2));
                row.append(",").append(decodeCasualtyTreatment(casualty, 0));
                row.append(",").append(decodeCasualtyTreatment(casualty, 1));
                row.append(",").append(decodeCasualtyTreatment(casualty, 2));

                // Visual Layer - Trainee looking at teammates (AOI)
                row.append(",").append(decodeVisualSegment(visual, 0, 0, 2)); // T1 digits 0-1
                row.append(",").append(decodeVisualSegment(visual, 1, 0, 2)); // T2 digits 0-1
                row.append(",").append(decodeVisualSegment(visual, 2, 0, 2)); // T3 digits 0-1

                // Visual Layer - Trainee looking at ROIs
                row.append(",").append(decodeVisualSegment(visual, 0, 2, 4)); // T1 digits 2-3
                row.append(",").append(decodeVisualSegment(visual, 1, 2, 4)); // T2 digits 2-3
                row.append(",").append(decodeVisualSegment(visual, 2, 2, 4)); // T3 digits 2-3

                // Visual Layer - Trainee looking at OOIs
                row.append(",").append(decodeVisualSegment(visual, 0, 4, 6)); // T1 digits 4-5
                row.append(",").append(decodeVisualSegment(visual, 1, 4, 6)); // T2 digits 4-5
                row.append(",").append(decodeVisualSegment(visual, 2, 4, 6)); // T3 digits 4-5

                // Movement Layer
                row.append(",").append(getValueOrNull(movement, 0)); // T1
                row.append(",").append(getValueOrNull(movement, 1)); // T2
                row.append(",").append(getValueOrNull(movement, 2)); // T3

                // Communication Layer
                row.append(",").append(getValueOrNull(communication, 0)); // T1
                row.append(",").append(getValueOrNull(communication, 1)); // T2
                row.append(",").append(getValueOrNull(communication, 2)); // T3

                writer.println(row.toString());
            }

            System.out.println("Layer states exported to: " + filename);
        }
    }

    private static String decodeCasualtyTriage(List<String> casualty, int index) {
        if (index >= casualty.size()) {
            return "NULL";
        }
        String code = casualty.get(index);
        if (code.length() >= 2) {
            return code.substring(0, 2); // First 2 digits = triage status
        }
        return "NULL";
    }

    private static String decodeCasualtyTreatment(List<String> casualty, int index) {
        if (index >= casualty.size()) {
            return "NULL";
        }
        String code = casualty.get(index);
        if (code.length() >= 4) {
            return code.substring(2, 4); // Last 2 digits = treatment status
        }
        return "NULL";
    }

    private static String decodeVisualSegment(List<String> visual, int traineeIndex, int start, int end) {
        if (traineeIndex >= visual.size()) {
            return "NULL";
        }
        String code = visual.get(traineeIndex);
        if (code.length() >= end) {
            return code.substring(start, end);
        }
        return "NULL";
    }

    private static String getValueOrNull(List<String> list, int index) {
        if (index >= list.size()) {
            return "NULL";
        }
        return list.get(index);
    }
}
