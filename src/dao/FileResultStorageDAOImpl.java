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
        Map<String, List<Object>> teamDynamicsMap
    ) throws IOException {
        // Define the target file path
        String dynamicsFilePath = filePath.replace(".jsonl", "_dynamics.jsonl");
        File file = new File(dynamicsFilePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // The labels that define the order in the input List<Object>
        final String[] METRIC_LABELS = {
            "Enaction",
            "Adaptation",
            "Recovery",
            "Influence",
        };

        // Map to hold the final labeled metrics for all subjects (Trainees + Team)
        Map<String, Map<String, Object>> labeledDynamics =
            new LinkedHashMap<>();

        // 1. Iterate over subjects (e.g., "Medsupplier", "team")
        for (Map.Entry<
            String,
            List<Object>
        > entry : teamDynamicsMap.entrySet()) {
            String subjectKey = entry.getKey();
            List<Object> rawMetrics = entry.getValue();

            Map<String, Object> subjectMetrics = new LinkedHashMap<>();

            // 2. Map the raw values to their corresponding labels
            if (rawMetrics.size() == METRIC_LABELS.length) {
                for (int i = 0; i < METRIC_LABELS.length; i++) {
                    subjectMetrics.put(METRIC_LABELS[i], rawMetrics.get(i));
                }
            } else {
                // Fallback for unexpected size
                subjectMetrics.put("Raw_Data", rawMetrics);
            }

            labeledDynamics.put(subjectKey, subjectMetrics);
        }

        // 3. Create the final top-level record, ensuring sessionID comes first
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("sessionID", sessionID);
        record.put("scenarioID", scenarioID);
        record.put("teamDynamics", labeledDynamics); // Store the newly labeled structure

        String jsonLine = json.writeValueAsString(record);

        // 4. Write the labeled record to the file
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }

        System.out.println(
            "Team Dynamics results written to: " + dynamicsFilePath
        );
    }

    @Override
    public void storeSessionIds(
        Integer giftSessionId,
        String unitySessionId,
        String scenarioId
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
        String scenarioID
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
