package service;

import config.*;
import dao.*;
import entropy.*;
import exception.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.*;
import parser.*;
import teamDynamics.DynamicsCalculator;
import util.LayerStateExporter;

public class SessionEntropyService {

    private ConfigManager defaultConfig;
    private ResultStorageDAO defaultResultStorage;

    public SessionEntropyService(ConfigManager configManager) {
        this.defaultConfig = configManager;
        this.defaultResultStorage = ResultStorageFactory.createResultStorage(
                defaultConfig,
                defaultConfig.getDataSourceType()
        );
    }

    private static String[][][] convertToArray(
            List<List<List<String>>> listData
    ) {
        String[][][] arrayData = new String[listData.size()][][];
        for (int i = 0; i < listData.size(); i++) {
            List<List<String>> timePoint = listData.get(i);
            arrayData[i] = new String[timePoint.size()][];
            for (int j = 0; j < timePoint.size(); j++) {
                List<String> layer = timePoint.get(j);
                arrayData[i][j] = layer.toArray(new String[0]);
            }
        }
        return arrayData;
    }

    private String[][][] sliceLayerData(
            String[][][] layerData,
            int startIdx,
            int endIdx
    ) {
        int sliceLength = endIdx - startIdx;
        String[][][] sliced = new String[sliceLength][][];

        for (int i = 0; i < sliceLength; i++) {
            sliced[i] = layerData[startIdx + i];
        }

        return sliced;
    }

    private int determineEndIdx(
            Map<String, Integer> allIds,
            String currentId,
            int startIdx,
            int totalLength
    ) {
        int nextStartIdx = totalLength;

        for (Integer idx : allIds.values()) {
            if (idx > startIdx && idx < nextStartIdx) {
                nextStartIdx = idx;
            }
        }

        return nextStartIdx;
    }

    public SessionEntropyData getEntireSessionEntropy(String sessionId)
            throws IOException {
        return defaultResultStorage.readEntropy(sessionId);
    }

    public Map<String, List<Object>> getScenarioTeamDynamics(
            String sessionId,
            String scenarioId,
            String dataSourceType
    ) throws IOException {
        ResultStorageDAO resultStorageDAO
                = ResultStorageFactory.createResultStorage(
                        defaultConfig,
                        dataSourceType
                );
        return resultStorageDAO.readTeamDynamics(sessionId, scenarioId);
    }

    public EntropyObject getEntropyForScenario(
            String sessionId,
            String scenarioId
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }
        return data.getScenarioEntropy().get(scenarioId);
    }

    public SessionMetadata getSessionMetadata(String sessionId)
            throws IOException {
        SessionMetadata data = defaultResultStorage.readMetadata(sessionId);
        if (data == null) {
            return null;
        }
        return data;
    }

    public EntropyObject getEntropyForPerturbation(
            String sessionId,
            String pertubationId
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }
        return data.getPertubationEntropy().get(pertubationId);
    }

    public EntropyObject getEntropyAtTime(String sessionId, int time)
            throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }

        Map<EntropyLayer, double[]> sliced = new HashMap<>();
        for (Map.Entry<EntropyLayer, double[]> entry : data
                .getSession_entropy()
                .getLayerEntropies()
                .entrySet()) {
            double[] values = entry.getValue();
            if (time < values.length) {
                sliced.put(entry.getKey(), new double[]{values[time]});
            }
        }
        return new EntropyObject(sliced);
    }

    public EntropyObject getEntropyInTimeRange(
            String sessionId,
            int from,
            int to
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }

        Map<EntropyLayer, double[]> sliced = new HashMap<>();
        for (Map.Entry<EntropyLayer, double[]> entry : data
                .getSession_entropy()
                .getLayerEntropies()
                .entrySet()) {
            double[] values = entry.getValue();
            int length = Math.min(to, values.length) - from;
            if (length > 0) {
                double[] range = new double[length];
                System.arraycopy(values, from, range, 0, length);
                sliced.put(entry.getKey(), range);
            }
        }
        return new EntropyObject(sliced);
    }

    private void validateSessionComplete(String sessionID, List<String> rawData)
            throws IncompleteSessionException {
        boolean foundApplicationStop = rawData
                .stream()
                .filter(line -> line.contains("\"scenarioEvent\":\"Application\""))
                .anyMatch(line -> line.contains("\"event\":\"stop\""));

        if (!foundApplicationStop) {
            throw new IncompleteSessionException(
                    "Session "
                    + sessionID
                    + " is incomplete - missing Application stop event"
            );
        }
    }

    public Map<String, Object> listAllSessions() throws IOException {
        List<String> sessionIDs = defaultResultStorage.listAllSessions();
        Map<String, Object> response = new HashMap<>();
        response.put("sessionIDs: ", sessionIDs);
        response.put("totalCount", sessionIDs.size());
        return response;
    }

    public void storeSessionIds(Integer giftSessionId, String unitySessionId, String scenarioId)
            throws IOException {

        List<Integer> giftSessionIds = new ArrayList<>();
        List<String> scenarioIds = new ArrayList<>();

        Map<String, List<Object>> sessionIds = defaultResultStorage.readSessionIds(unitySessionId);

        if (sessionIds == null || sessionIds.isEmpty()) {
            giftSessionIds.add(giftSessionId);
            scenarioIds.add(scenarioId);
        } else {
            if (sessionIds.get("giftSessionId") != null) {
                giftSessionIds = new ArrayList<>((List<Integer>) (List<?>) sessionIds.get("giftSessionId"));
            }
            if (sessionIds.get("scenarioId") != null) {
                scenarioIds = new ArrayList<>((List<String>) (List<?>) sessionIds.get("scenarioId"));
            }

            if (!giftSessionIds.contains(giftSessionId)) {
                giftSessionIds.add(giftSessionId);
            }
            if (!scenarioIds.contains(scenarioId)) {
                scenarioIds.add(scenarioId);
            }
        }
        defaultResultStorage.storeSessionIds(giftSessionIds, unitySessionId, scenarioIds);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> normalizeToList(Object raw) {
        if (raw == null) {
            return new ArrayList<>();
        }

        if (raw instanceof List<?>) {
            return new ArrayList<>((List<Object>) raw);
        }

        if (raw instanceof Map<?, ?>) {
            return new ArrayList<>(((Map<?, ?>) raw).values());
        }
        return new ArrayList<>(Collections.singletonList(raw));
    }

    private static List<Object> calculateAverage(List<Object> list1, List<Object> list2) {
        List<Object> averagedList = new ArrayList<>();
        int size = Math.min(list1.size(), list2.size());

        for (int i = 0; i < size; i++) {
            Object obj1 = list1.get(i);
            Object obj2 = list2.get(i);

            if (obj1 instanceof Number && obj2 instanceof Number) {
                double avg = (((Number) obj1).doubleValue() + ((Number) obj2).doubleValue()) / 2.0;
                averagedList.add(avg);
            } else {
                averagedList.add(obj1);
            }
        }

        return averagedList;
    }

    public void CalculateEntropy(String sessionID, String dataSourceType)
            throws IOException, IncompleteSessionException {
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            dataSourceType = defaultConfig.getDataSourceType();
        }

        DataSourceDAO dataSource = DataSourceFactory.createDataSource(
                defaultConfig,
                dataSourceType
        );
        DataParser parser = ParserFactory.createParser(dataSourceType);
        ResultStorageDAO resultStorageDAO
                = ResultStorageFactory.createResultStorage(
                        defaultConfig,
                        dataSourceType
                );

        List<String> rawData = dataSource.readData(sessionID);
        // validateSessionComplete(sessionID, rawData);
        List<List<List<String>>> layers = parser.parseToSTTCLayers(rawData);
        try {
            LayerStateExporter.exportLayerStatesToCSV(layers, sessionID, "results");
        } catch (IOException e) {
            System.err.println("Failed to export layer states: " + e.getMessage());
        }
        Map<String, String> traineeRoles = parser.getTraineeInfo(rawData);
        SessionMetadata sessionMetadata = parser.getSessionMetadata();
        String[][][] layerData = convertToArray(layers);

        Map<EntropyLayer, double[]> sessionEntropyMap = new HashMap<>();

        sessionEntropyMap.put(
                EntropyLayer.COMMUNICATION,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(0)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.VISUAL,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(1)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.CASUALTY,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(2)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.MOVEMENT,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(3)
                )
        );

        sessionEntropyMap.put(
                EntropyLayer.TRAINEE1,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                0
                        )
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TRAINEE2,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                1
                        )
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TRAINEE3,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                2
                        )
                )
        );

        sessionEntropyMap.put(
                EntropyLayer.SYSTEM,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new CombinedLayerAggregationStrategy(new int[]{0, 1, 2, 3})
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TEAM,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new CombinedLayerAggregationStrategy(new int[]{0, 3})
                )
        );

        Map<String, EntropyObject> scenarioEntropyMap = new HashMap<>();
        for (Map.Entry<String, Integer> scenario : sessionMetadata
                .getScenarioIDs()
                .entrySet()) {
            String scenarioId = scenario.getKey();
            int startIdx = scenario.getValue();
            int endIdx = determineEndIdx(
                    sessionMetadata.getScenarioIDs(),
                    scenarioId,
                    startIdx,
                    layerData.length
            );

            Map<EntropyLayer, double[]> scenarioLayers = new HashMap<>();
            String[][][] slicedData = sliceLayerData(
                    layerData,
                    startIdx,
                    endIdx
            );

            scenarioLayers.put(
                    EntropyLayer.COMMUNICATION,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(0)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.VISUAL,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(1)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.CASUALTY,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(2)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.MOVEMENT,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(3)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE1,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    0
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE2,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    1
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE3,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    2
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.SYSTEM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(
                                    new int[]{0, 1, 2, 3}
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TEAM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(new int[]{0, 3})
                    )
            );

            scenarioEntropyMap.put(
                    scenarioId,
                    new EntropyObject(scenarioLayers)
            );
        }

        Map<String, EntropyObject> perturbationEntropyMap = new HashMap<>();
        for (Map.Entry<String, List<Integer>> perturbation : sessionMetadata
                .getPertubationIDs()
                .entrySet()) {
            String perturbationId_temp = perturbation.getKey();
            String[] parts = perturbationId_temp.split("split");
            String scenarioID = parts[0];
            String perturbationId = parts[1];
            int startIdx = perturbation.getValue().get(0);
            int endIdx = perturbation.getValue().get(1);
            Map<EntropyLayer, double[]> perturbationLayers = new HashMap<>();
            String[][][] slicedData = sliceLayerData(
                    layerData,
                    startIdx,
                    endIdx
            );

            perturbationLayers.put(
                    EntropyLayer.COMMUNICATION,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(0)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.VISUAL,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(1)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.CASUALTY,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(2)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.MOVEMENT,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(3)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE1,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    0
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE2,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    1
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE3,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    2
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.SYSTEM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(
                                    new int[]{0, 1, 2, 3}
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TEAM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(new int[]{0, 3})
                    )
            );

            perturbationEntropyMap.put(
                    perturbationId,
                    new EntropyObject(perturbationLayers)
            );

            DynamicsCalculator dynamicsFacade = new DynamicsCalculator();
            Map<String, List<Object>> teamDynamics
                    = dynamicsFacade.calculateDynamics(perturbationLayers, layers);
            Map<String, List<Object>> roleMappedDynamics
                    = dynamicsFacade.replaceTraineeKeys(teamDynamics, traineeRoles);

            Map<String, ?> existingDynamics
                    = (Map<String, ?>) resultStorageDAO.readTeamDynamics(sessionID, scenarioID);        

            if (existingDynamics != null && !existingDynamics.isEmpty()) {
                resultStorageDAO.writeTeamDynamics(
                        sessionID,
                        scenarioID,
                        perturbationId,
                        roleMappedDynamics
                );
                for (Map.Entry<String, ?> entry : existingDynamics.entrySet()) {
                    String key = entry.getKey();
                    Object rawExistingValue = entry.getValue();
                    if (rawExistingValue != null && !(rawExistingValue instanceof List<?>)) {
                        List<Object> existingList = normalizeToList(rawExistingValue);
                        List<Object> currentList = normalizeToList(roleMappedDynamics.get(key));
                        List<Object> newList = new ArrayList<>();
                        newList = calculateAverage(existingList, currentList);
                        roleMappedDynamics.put(key, newList);
                    }
                   perturbationId = "averaged";
                }
            }
            resultStorageDAO.writeTeamDynamics(
                    sessionID,
                    scenarioID,
                    perturbationId,
                    roleMappedDynamics
            );
        }

        EntropyObject sessionEntropy = new EntropyObject(sessionEntropyMap);

        SessionEntropyData sessionEntropyData = new SessionEntropyData(
                sessionID,
                sessionEntropy,
                scenarioEntropyMap,
                perturbationEntropyMap
        );
        resultStorageDAO.writeEntropy(sessionEntropyData);
        resultStorageDAO.writeMetadata(sessionMetadata);
    }
}
