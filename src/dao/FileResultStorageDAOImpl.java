package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.*;

public class FileResultStorageDAOImpl implements ResultStorageDAO {

    public FileResultStorageDAOImpl() {}

    private ObjectMapper json;
    private String filePath;

    public FileResultStorageDAOImpl(String filePath) {
        this.filePath = filePath;
        this.json = new ObjectMapper();
    }

    @Override
    public SessionMetadata readMetadata(String sessionID) {
        return new SessionMetadata();
    }

    @Override
    public void writeMetadata(SessionMetadata sessionMetadata)
        throws IOException {}

    @Override
    public void writeEntropy(SessionEntropyData sessionEntropyData)
        throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        String jsonLine = json.writeValueAsString(sessionEntropyData);

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }
    }

    @Override
    public void writeTeamDynamics(
        String sessionID,
        String scenarioID,
        String perturbationId,
        Map<String, List<Object>> teamDynamicsMap
    ) throws IOException {
        String dynamicsFilePath = filePath.replace(".jsonl", "_dynamics.jsonl");
        File file = new File(dynamicsFilePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        final String[] METRIC_LABELS = {
            "Enaction",
            "Adaptation",
            "Recovery",
            "Influence",
        };

        Map<String, Map<String, Object>> labeledDynamics =
            new LinkedHashMap<>();

        for (Map.Entry<
            String,
            List<Object>
        > entry : teamDynamicsMap.entrySet()) {
            String subjectKey = entry.getKey();
            List<Object> rawMetrics = entry.getValue();

            Map<String, Object> subjectMetrics = new LinkedHashMap<>();

            if (rawMetrics.size() == METRIC_LABELS.length) {
                for (int i = 0; i < METRIC_LABELS.length; i++) {
                    subjectMetrics.put(METRIC_LABELS[i], rawMetrics.get(i));
                }
            } else {
                subjectMetrics.put("Raw_Data", rawMetrics);
            }

            labeledDynamics.put(subjectKey, subjectMetrics);
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("sessionID", sessionID);
        record.put("scenarioID", scenarioID);
        record.put("perturbationId", perturbationId);
        record.put("teamDynamics", labeledDynamics); 

        String jsonLine = json.writeValueAsString(record);

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }

        System.out.println(
            "Team Dynamics results written to: " + dynamicsFilePath
        );
    }

    @Override
    public void storeSessionIds(
        List<Integer> giftSessionId,
        String unitySessionId,
        List<String> scenarioId
    ) throws IOException {
        String idsFilePath = filePath.replace(".jsonl", "_sessionIDs.jsonl");
        File file = new File(idsFilePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        Map<String, Object> sessionIdRecord = new LinkedHashMap<>();
        sessionIdRecord.put("giftSessionId", giftSessionId);
        sessionIdRecord.put("unitySessionId", unitySessionId);
        sessionIdRecord.put("scenarioId", scenarioId);

        String jsonLine = json.writeValueAsString(sessionIdRecord);

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }
    }

    @Override
    public Map<String, List<Object>> readSessionIds(String unitySessionId) throws IOException {
        String idsFilePath = filePath.replace(".jsonl", "_sessionIDs.jsonl");
        File file = new File(idsFilePath);
        if (!file.exists()) {
            return null;
        }
        List<String> lines = Files.readAllLines(Paths.get(idsFilePath));
        for (String line : lines) {
            @SuppressWarnings("unchecked")
            Map<String, Object> record = json.readValue(line, Map.class);
            if (
                record.containsKey("unitySessionId") &&
                record.get("unitySessionId").equals(unitySessionId)
            ) {
                Map<String, List<Object>> result = new LinkedHashMap<>();
                @SuppressWarnings("unchecked")
                List<Object> giftIds = (List<Object>) record.get("giftSessionId");
                @SuppressWarnings("unchecked")
                List<Object> scenarioIds = (List<Object>) record.get("scenarioId");
                result.put("giftSessionIds", giftIds);
                result.put("scenarioIds", scenarioIds);
                return result;
            }
        }
        return null;
    }

    @Override
    public SessionEntropyData readEntropy(String sessionID) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            SessionEntropyData data = json.readValue(
                line,
                SessionEntropyData.class
            );
            if (data.getSessionID().equals(sessionID)) {
                return data;
            }
        }
        return null;
    }

    @Override
    public Map<String, List<Object>> readTeamDynamics(
        String sessionID,
        String scenarioID,
        String pertubationID
    ) throws IOException {
        String dynamicsFilePath = filePath.replace(".jsonl", "_dynamics.jsonl");
        File file = new File(dynamicsFilePath);

        if (!file.exists()) {
            return null;
        }

        List<String> lines = Files.readAllLines(Paths.get(dynamicsFilePath));

        for (String line : lines) {
            @SuppressWarnings("unchecked")
            Map<String, Object> wrapperData = json.readValue(line, Map.class);

            if (
                wrapperData.containsKey("sessionID") &&
                wrapperData.get("sessionID").equals(sessionID) &&
                wrapperData.containsKey("scenarioID") &&
                wrapperData.get("scenarioID").equals(scenarioID)
            ) {
                if (wrapperData.containsKey("teamDynamics")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Object>> teamDynamics = (Map<
                        String,
                        List<Object>
                    >) wrapperData.get("teamDynamics");
                    return teamDynamics;
                }
            }
        }
        System.out.println(
            "No Team Dynamics found for Session ID: " +
                sessionID +
                ", Scenario ID: " +
                scenarioID
        );
        return null;
    }

    @Override
    public List<String> listAllSessions() throws IOException {
        throw new UnsupportedOperationException(
            "Listing all sessions is not supported for file-based storage. " +
                "This operation is only available when using MongoDB storage."
        );
    }
}
