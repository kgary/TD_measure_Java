package parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import model.*;

public class FileFormatParser implements DataParser {

    private static Map<String, String> triageStatus = new HashMap<>();
    private static Map<Integer, Boolean> treatmentStatus = new HashMap<>();
    private static double[] movementROI = null;
    private SessionMetadata sessionMetadata;
    private static Map<String, Integer> casualtiesId = new HashMap<>();
    private static Map<Integer, String> casualtyIdReversed = new HashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    private void initializeFromScenarioDefinition(List<String> rawData) {
        for (String line : rawData) {
            try {
                JsonNode node = mapper.readTree(line);
                if ("ScenarioDefinition".equals(
                        node.get("scenarioEvent").asText()
                )) {
                    initializeCasualties(node);
                    initializeMovementROI(node);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeCasualties(JsonNode jsonObject) {
        JsonNode casualties = jsonObject.get("Scenario").get("Casualties");
        int count = 1;
        for (JsonNode casualty : casualties) {
            String casualtyId = casualty.get("id").asText();
            switch (count) {
                case 1:
                    triageStatus.put(casualtyId, "01");        
                    break;
                case 2:
                    triageStatus.put(casualtyId, "06");        
                    break;
                case 3:
                    triageStatus.put(casualtyId, "11");        
                    break;    
            }
            treatmentStatus.put(count, false);
            casualtiesId.put(casualtyId, count);
            casualtyIdReversed.put(count, casualtyId);
            count++;
        }
    }

    public void initializeMovementROI(JsonNode jsonObject) {
        try {
            JsonNode scenario = jsonObject.get("Scenario");
            if (scenario == null) {
                System.err.println("No 'Scenario' field found in JSON");
                return;
            }

            JsonNode regions = scenario.get("RegionsOfInterest");
            if (regions == null) {
                System.err.println(
                        "No 'RegionsOfInterest' field found in Scenario"
                );
                return;
            }

            for (JsonNode region : regions) {
                JsonNode idNode = region.get("id");
                if (idNode != null
                        && "MovementLayerImportant".equals(idNode.asText())) {
                    double[] location = mapper.treeToValue(
                            region.get("location"),
                            double[].class
                    );

                    if (location.length < 6) {
                        System.err.println(
                                "Location array too short, expected at least 6 elements"
                        );
                        return;
                    }

                    double centerX = location[0];
                    double centerZ = location[2];
                    double midX = location[3];
                    double midZ = location[5];

                    movementROI = new double[]{centerX, centerZ, midX, midZ};
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "Error processing JSON for movement ROI: " + e.getMessage()
            );
        }
    }

    public static List<String> extractCommunication(JsonNode jsonObject) {
        List<String> comms = new ArrayList<>();
        try {
            JsonNode trainees = jsonObject.get("trainees");
            if (trainees == null || !trainees.isArray()) {
                return comms;
            }
            for (int i = 0; i < trainees.size(); i++) {
                JsonNode trainee = trainees.get(i);
                JsonNode commNode = trainee.get("Communication");
                if (i == 0) {
                    comms.add(commNode.asBoolean() ? "46" : "47");
                }
                if (i == 1) {
                    comms.add(commNode.asBoolean() ? "48" : "49");
                }
                if (i == 2) {
                    comms.add(commNode.asBoolean() ? "50" : "51");
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "Error extracting communication: " + e.getMessage()
            );
        }
        return comms;
    }

    public static List<String> extractVisual(JsonNode jsonObject) {
        List<String> visual = new ArrayList<>();

        try {
            JsonNode trainees = jsonObject.get("trainees");
            for (int i = 0; i < trainees.size(); i++) {
                StringBuilder state = new StringBuilder();
                JsonNode trainee = trainees.get(i);
                JsonNode visualArray = trainee.get("VisualActivity");

                boolean hasTraineeWatched = false;
                boolean hasOOIWatched = false;
                boolean hasROIWatched = false;

                if (visualArray != null && visualArray.isArray()) {
                    for (JsonNode va  : visualArray) {
                        if (va.has("Trainee_Watched")) {
                            hasTraineeWatched = true;
                        }
                        if (va.has("OOI_Watched")) {
                            hasOOIWatched = true;
                        }
                        if (va.has("ROI_Watched")) {
                            hasROIWatched = true;
                        }
                    }
                }

                if (hasTraineeWatched) {
                    if (i == 0) {
                        state.append("22");
                    }
                    if (i == 1) {
                        state.append("24");
                    }
                    if (i == 2) {
                        state.append("26");
                    }
                } else {
                    if (i == 0) {
                        state.append("23");
                    }
                    if (i == 1) {
                        state.append("25");
                    }
                    if (i == 2) {
                        state.append("27");
                    }
                }

                if (hasROIWatched) {
                    if (i == 0) {
                        state.append("28");
                    }
                    if (i == 1) {
                        state.append("30");
                    }
                    if (i == 2) {
                        state.append("32");
                    }
                } else {
                    if (i == 0) {
                        state.append("29");
                    }
                    if (i == 1) {
                        state.append("31");
                    }
                    if (i == 2) {
                        state.append("33");
                    }
                }

                if (hasOOIWatched) {
                    if (i == 0) {
                        state.append("34");
                    }
                    if (i == 1) {
                        state.append("36");
                    }
                    if (i == 2) {
                        state.append("38");
                    }
                } else {
                    if (i == 0) {
                        state.append("35");
                    }
                    if (i == 1) {
                        state.append("37");
                    }
                    if (i == 2) {
                        state.append("39");
                    }
                }

                visual.add(state.toString());
            }
        } catch (Exception e) {
            System.err.println("Error extracting visual: " + e.getMessage());
        }

        return visual;
    }

    public List<String> extractMovement(JsonNode jsonObject) {
        List<String> movementStates = new ArrayList<>();
        JsonNode trainees = jsonObject.get("trainees");
        try {
            for (int i = 0; i < trainees.size(); i++) {
                JsonNode traineeMovement = trainees.get(i).get("Movement");
                double[] headCoords = mapper.treeToValue(
                        traineeMovement.get("Head"),
                        double[].class
                );

                double headX = headCoords[0];
                double headZ = headCoords[2];

                double minX = movementROI[0] - movementROI[2] / 2;
                double maxX = movementROI[0] + movementROI[2] / 2;
                double minZ = movementROI[1] - movementROI[3] / 2;
                double maxZ = movementROI[1] + movementROI[3] / 2;

                boolean isInROI = (headX >= minX
                        && headX <= maxX
                        && headZ >= minZ
                        && headZ <= maxZ);
                if (i == 0) {
                    movementStates.add(isInROI ? "40" : "41");
                }
                if (i == 1) {
                    movementStates.add(isInROI ? "42" : "43");
                }
                if (i == 2) {
                    movementStates.add(isInROI ? "44" : "45");
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting movement: " + e.getMessage());
        }

        return movementStates;
    }

    public static void updateTreatmentStatus(JsonNode jsonObject) {
        if ("treatment".equals(jsonObject.get("subtype").asText())
                && "stop".equals(jsonObject.get("event").asText())) {
            String subtypeId = jsonObject.get("subtype_id").asText();
            int casualtyId = casualtiesId.get(subtypeId);
            treatmentStatus.put(casualtyId, true);
        }
    }

    public static void updateCasualtyStatus(JsonNode jsonObject) {
        String triageState = "";
        String casualty = jsonObject.get("casualty_id").asText();
        String stateChange = jsonObject.get("stateChange").asText();

        int casualtyId = casualtiesId.get(casualty);

        switch (casualtyId) {
            case 1:
                switch (stateChange) {
                    case "not_triaged":
                        triageState = "01";
                        break;
                    case "urgent":
                        triageState = "02";
                        break;
                    case "delayed":
                        triageState = "03";
                        break;
                    case "minimal":
                        triageState = "04";
                        break;
                    case "expectant":
                        triageState = "05";
                        break;
                    default:
                        triageState = "01";
                        break;
                }
                break;
            case 2:
                switch (stateChange) {
                    case "not_triaged":
                        triageState = "06";
                        break;
                    case "urgent":
                        triageState = "07";
                        break;
                    case "delayed":
                        triageState = "08";
                        break;
                    case "minimal":
                        triageState = "09";
                        break;
                    case "expectant":
                        triageState = "10";
                        break;
                    default:
                        triageState = "06";
                        break;
                }
                break;
            case 3:
                switch (stateChange) {
                    case "not_triaged":
                        triageState = "11";
                        break;
                    case "urgent":
                        triageState = "12";
                        break;
                    case "delayed":
                        triageState = "13";
                        break;
                    case "minimal":
                        triageState = "14";
                        break;
                    case "expectant":
                        triageState = "15";
                        break;
                    default:
                        triageState = "11";
                        break;
                }
                break;
        }
        triageStatus.put(casualty, triageState);
    }

    private List<String> getCasualtyState() {
        List<String> casualtyStates = new ArrayList<>();
        String finalCasualty = "";
        int numCasualties = casualtiesId.size(); 
        String treatmentState = "";
        boolean treatmentValue = false;
        String triageState = "";
        String casualtyStringId = "";

        for (int casualtyId = 1; casualtyId <= numCasualties; casualtyId++) {

            casualtyStringId = casualtyIdReversed.get(casualtyId);
            treatmentValue = treatmentStatus.getOrDefault(casualtyId, false);

            if (treatmentValue) {
                switch (casualtyId) {
                    case 1:
                        treatmentState = "16";
                        break;
                    case 2:
                        treatmentState = "18";
                        break;
                    case 3:
                        treatmentState = "20";
                        break;
                    default:
                        treatmentState = "17";
                        break;
                }
            } else {
                switch (casualtyId) {
                    case 1:
                        treatmentState = "17";
                        break;
                    case 2:
                        treatmentState = "19";
                        break;
                    case 3:
                        treatmentState = "21";
                        break;
                    default:
                        treatmentState = "17";
                        break;
                }
            }

            triageState = triageStatus.get(casualtyStringId);
            finalCasualty = triageState + treatmentState;
            casualtyStates.add(finalCasualty);
        }
        return casualtyStates;
    }

    public SessionMetadata getSessionMetadata() {
        return sessionMetadata;
    }

    @Override
    public List<List<List<String>>> parseToSTTCLayers(List<String> rawData)
            throws IOException {
        boolean logging = true;

        List<List<List<String>>> sttcLayers = new ArrayList<>();
        List<List<String>> visual = new ArrayList<>();
        List<List<String>> comm = new ArrayList<>();
        List<List<String>> casualty = new ArrayList<>();
        List<List<String>> movement = new ArrayList<>();

        String sessionID;
        Map<String, Integer> scenarioIDs;
        Map<String, Integer> perturbationIDs;
        int currentTimeIndex = 0;

        initializeFromScenarioDefinition(rawData);
        for (String line : rawData) {
            JsonNode node = mapper.readTree(line);
            String eventType = node.get("scenarioEvent").asText();
            List<List<String>> dataFrame = new ArrayList<>();

            if ("Timer".equals(eventType)) {
                List<String> traineeComm = extractCommunication(node);
                List<String> traineeViz = extractVisual(node);
                List<String> traineeMov = extractMovement(node);
                List<String> traineeCas = getCasualtyState();

                comm.add(traineeComm);
                visual.add(traineeViz);
                movement.add(traineeMov);
                casualty.add(traineeCas);

                dataFrame.add(traineeComm);
                dataFrame.add(traineeViz);
                dataFrame.add(traineeCas);
                dataFrame.add(traineeMov);
                sttcLayers.add(dataFrame);

                currentTimeIndex++;
            } else if ("Domain".equals(eventType)) {
                if ("perturbation".equals(node.get("subtype").asText())) {
                    String perturbationID = node.get("subtype_id").asText();
                    if (!perturbationIDs.containsKey(perturbationID)) {
                        perturbationIDs.put(perturbationID, currentTimeIndex);
                    }
                }
                updateCasualtyStatus(node);
            } else if ("ScenarioDefinition".equals(eventType)) {
                String scenarioID = node.get("Scenario").get("id").asText();
                scenarioIDs.put(scenarioID, currentTimeIndex);
            } else if ("Application".equals(eventType)) {
                sessionID = node.get("sessionID").asText();
            } else if ("Triage".equals(eventType)) {
                if ("state".equals(node.get("subtype_id").asText())) {
                    updateCasualtyStatus(node);
                }
            }
        }
        this.sessionMetadata = new SessionMetadata(
            sessionID,
            scenarioIDs,
            perturbationIDs
        );
        if (logging) {
            System.out.println("======Visual Layer: ======");
            System.out.println(visual);
            System.out.println("======Communication Layer: ======");
            System.out.println(comm);
            System.out.println("======Casualty Layer: ======");
            System.out.println(casualty);
            System.out.println("======Movement Layer: ======");
            System.out.println(movement);

            System.out.println("========STTC LAYERS:============");
            for (int t = 0; t < sttcLayers.size(); t++) {
                System.out.println("Time " + t + ":");
                List<List<String>> timeInstance = sttcLayers.get(t);

                if (timeInstance.size() >= 4) {
                    System.out.println(
                            "  Communication: " + timeInstance.get(0)
                    );
                    System.out.println(
                            "  Visual:        " + timeInstance.get(1)
                    );
                    System.out.println(
                            "  Casualty:      " + timeInstance.get(2)
                    );
                    System.out.println(
                            "  Movement:      " + timeInstance.get(3)
                    );
                } else {
                    System.out.println(
                            "  Incomplete data - only "
                            + timeInstance.size()
                            + " layers"
                    );
                    for (int layer = 0; layer < timeInstance.size(); layer++) {
                        System.out.println(
                                "  Layer " + layer + ": " + timeInstance.get(layer)
                        );
                    }
                }
                System.out.println();
            }
        }
        return sttcLayers;
    }
}
