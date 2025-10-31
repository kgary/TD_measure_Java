package teamDynamics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.*;

public class DynamicsCalculator {

    private double[][] prepareEntropyMatrix(Map<EntropyLayer, double[]> sessionEntropyMap) {

        EntropyLayer keyTrainee1 = EntropyLayer.TRAINEE1;
        EntropyLayer keyTrainee2 = EntropyLayer.TRAINEE2;
        EntropyLayer keyTrainee3 = EntropyLayer.TRAINEE3;
        EntropyLayer keyTeam = EntropyLayer.TEAM;

        if (sessionEntropyMap.isEmpty() || !sessionEntropyMap.containsKey(keyTrainee1)) {
            return new double[0][4];
        }
        double[] trainee1Array = sessionEntropyMap.get(keyTrainee1);
        int totalTime = trainee1Array.length;
        double[][] dataMatrix = new double[totalTime][4];

        for (int t = 0; t < totalTime; t++) {
            dataMatrix[t][0] = sessionEntropyMap.get(keyTrainee1)[t];
            dataMatrix[t][1] = sessionEntropyMap.get(keyTrainee2)[t];
            dataMatrix[t][2] = sessionEntropyMap.get(keyTrainee3)[t];
            dataMatrix[t][3] = sessionEntropyMap.get(keyTeam)[t];
        }

        return dataMatrix;
    }

    private List<Integer[]> prepareCommunicationMatrix(List<List<List<String>>> layers) {
        List<Integer[]> communicationMatrix = new ArrayList<>();
        Integer commLayerIndex = 0;

        for (List<List<String>> timeInstance : layers) {
            List<String> commCodes = timeInstance.get(commLayerIndex);

            if (commCodes.size() >= 3) {
                Integer[] codes = new Integer[commCodes.size()];

                try {
                    for (int i = 0; i < commCodes.size(); i++) {
                        codes[i] = Integer.parseInt(commCodes.get(i));
                    }
                    communicationMatrix.add(codes);

                } catch (NumberFormatException e) {
                    System.err.println("Error parsing communication code to integer: " + e.getMessage());
                }
            }
        }
        return communicationMatrix;
    }

    public static Map<String, List<Object>> replaceTraineeKeys(
            Map<String, List<Object>> metricsMap,
            Map<String, String> traineeRoles
    ) {
        Map<String, List<Object>> mappedMetrics = new LinkedHashMap<>();

        for (Map.Entry<String, List<Object>> entry : metricsMap.entrySet()) {
            String oldKey = entry.getKey();
            List<Object> values = entry.getValue();

            if (traineeRoles.containsKey(oldKey)) {
                String newKey = traineeRoles.get(oldKey);
                mappedMetrics.put(newKey, values);
            } else {
                mappedMetrics.put(oldKey, values);
            }
        }
        return mappedMetrics;
    }

    private static Map<String, List<Object>> combineFinalResults(
            Map<String, Integer[]> Concepts,
            Map<String, Double> Influence
    ) {
        Map<String, List<Object>> finalResults = new LinkedHashMap<>();

        for (Map.Entry<String, Integer[]> conceptEntry : Concepts.entrySet()) {
            String subject = conceptEntry.getKey();
            Integer[] conceptValues = conceptEntry.getValue();
            List<Object> metrics = new ArrayList<>();

            if (conceptValues != null) {
                for (Integer val : conceptValues) {
                    metrics.add(val);
                }
            } else {
                metrics.add(null);
                metrics.add(null);
                metrics.add(null);
            }
            if (Influence.containsKey(subject)) {
                metrics.add(Influence.get(subject));
            } else {
                metrics.add(null);
            }

            finalResults.put(subject, metrics);
        }

        return finalResults;
    }

    public Map<String, List<Object>> calculateDynamics(Map<EntropyLayer, double[]> sessionEntropyMap,
            List<List<List<String>>> layers) {      
        
                double[][] entropyMatrix = prepareEntropyMatrix(sessionEntropyMap);
        
        Map<String, Integer[]> Concepts = EntropyProcessor.processEntropyData(entropyMatrix);
        
        List<Integer[]> commListMatrix = prepareCommunicationMatrix(layers);
        
        Integer[][] communicationData = commListMatrix.toArray(new Integer[0][]);
        
        Map<String, Double> Influence = InfluenceProcessor.processCommunicationData(communicationData);
        
        Map<String, List<Object>> finalResults = combineFinalResults(Concepts, Influence);
        

        return finalResults;

    }
}
