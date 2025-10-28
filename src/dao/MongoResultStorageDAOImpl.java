package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.*;
import org.bson.Document;

public class MongoResultStorageDAOImpl implements ResultStorageDAO {

    private final ObjectMapper json;
    private final String mongoUri;
    private final String mongoDatabase;
    private final String mongoCollection;

    private final String csvExportDirectory;

    private final String ENTROPY_COLLECTION_SUFFIX = "-entropy";
    private final String DYNAMICS_COLLECTION_SUFFIX = "-dynamics";
    private final String METADATA_COLLECTION_SUFFIX = "-metadata";

    public MongoResultStorageDAOImpl(
        String mongoUri,
        String mongoDatabase,
        String mongoCollection,
        String csvExportDirectory
    ) {
        this.mongoUri = mongoUri;
        this.mongoDatabase = mongoDatabase;
        this.mongoCollection = mongoCollection;
        this.json = new ObjectMapper();
        this.csvExportDirectory = csvExportDirectory;
    }

    private MongoCollection<Document> getCollection(String suffix) {
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        return db.getCollection(mongoCollection + suffix);
    }

    @Override
    public void writeMetadata(SessionMetadata sessionMetadata)
        throws IOException {
        String collectionName = mongoCollection + METADATA_COLLECTION_SUFFIX;

        try {
            String jsonString = json.writeValueAsString(sessionMetadata);

            MongoCollection<Document> collection = getCollection(
                METADATA_COLLECTION_SUFFIX
            );
            Document doc = Document.parse(jsonString);

            Document filter = new Document(
                "sessionID",
                sessionMetadata.getSessionID()
            );
            collection.replaceOne(
                filter,
                doc,
                new ReplaceOptions().upsert(true)
            );

            System.out.println(
                "MongoDB: Successfully wrote Session Metadata to collection: " +
                    collectionName
            );
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error writing metadata: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public void storeSessionIds(
        Integer giftSessionId,
        String unitySessionId,
        String scenarioId
    ) throws IOException {
        String collectionName = mongoCollection + "-session-ids";

        try {
            MongoCollection<Document> collection = getCollection(
                "-sessionIDs"
            );

            Map<String, Object> record = new java.util.LinkedHashMap<>();
            record.put("giftSessionId", giftSessionId);
            record.put("unitySessionId", unitySessionId);
            record.put("scenarioId", scenarioId);

            String jsonString = json.writeValueAsString(record);
            Document doc = Document.parse(jsonString);
            collection.insertOne(doc);

            System.out.println(
                "MongoDB: Successfully stored session IDs to collection: " +
                    collectionName
            );
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error storing session IDs: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public void writeEntropy(SessionEntropyData sessionEntropyData)
        throws IOException {
        String collectionName = mongoCollection + ENTROPY_COLLECTION_SUFFIX;

        try {
            String jsonString = json.writeValueAsString(sessionEntropyData);

            MongoCollection<Document> collection = getCollection(
                ENTROPY_COLLECTION_SUFFIX
            );
            Document doc = Document.parse(jsonString);

            Document filter = new Document(
                "sessionID",
                sessionEntropyData.getSessionID()
            );
            collection.replaceOne(
                filter,
                doc,
                new ReplaceOptions().upsert(true)
            );

            System.out.println(
                "MongoDB: Successfully wrote SessionEntropyData to collection: " +
                    collectionName
            );
            String fileName =
                csvExportDirectory +
                "/" +
                sessionEntropyData.getSessionID() +
                "-all-entropy-ts.csv";

            try (
                PrintWriter writer = new PrintWriter(
                    new FileWriter(fileName, true)
                )
            ) {
                if (new java.io.File(fileName).length() == 0) {
                    writer.println(
                        "sessionID,entropyType,keyID,layer,entropyValue"
                    );
                }
                this.exportEntropyMapToCsv(
                    writer,
                    sessionEntropyData.getSessionID(),
                    "SCENARIO",
                    sessionEntropyData.getScenarioEntropy()
                );
                this.exportEntropyMapToCsv(
                    writer,
                    sessionEntropyData.getSessionID(),
                    "PERTURBATION",
                    sessionEntropyData.getPertubationEntropy()
                );
                EntropyObject sessionSummary =
                    sessionEntropyData.getSession_entropy();
                if (sessionSummary != null) {
                    this.exportEntropyObjectToCsv(
                        writer,
                        sessionEntropyData.getSessionID(),
                        "SESSION_SUMMARY",
                        "SESSION",
                        sessionSummary
                    );
                }

                System.out.println(
                    "CSV: Successfully exported SessionEntropyData to: " +
                        fileName
                );
            } catch (IOException csvE) {
                System.err.println(
                    "CSV Error writing entropy data: " + csvE.getMessage()
                );
            }
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error writing entropy data: " + e.getMessage(),
                e
            );
        }
    }

    private void exportEntropyMapToCsv(
        PrintWriter writer,
        String sessionID,
        String entropyType,
        Map<String, EntropyObject> entropyMap
    ) {
        if (entropyMap == null) return;

        for (Map.Entry<String, EntropyObject> entry : entropyMap.entrySet()) {
            String keyID = entry.getKey();
            EntropyObject entropyObj = entry.getValue();

            if (entropyObj != null) {
                this.exportEntropyObjectToCsv(
                    writer,
                    sessionID,
                    entropyType,
                    keyID,
                    entropyObj
                );
            }
        }
    }

    private void exportEntropyObjectToCsv(
        PrintWriter writer,
        String sessionID,
        String entropyType,
        String keyID,
        EntropyObject entropyObj
    ) {
        Map<EntropyLayer, double[]> layerMap = entropyObj.getLayerEntropies();
        if (layerMap == null) return;

        for (Map.Entry<
            EntropyLayer,
            double[]
        > layerEntry : layerMap.entrySet()) {
            String layerName = layerEntry.getKey().name();
            double[] entropyTimeSeries = layerEntry.getValue();

            if (entropyTimeSeries != null) {
                for (int i = 0; i < entropyTimeSeries.length; i++) {
                    writer.printf(
                        "%s,%s,%s,%s,%.6f%n",
                        sessionID,
                        entropyType,
                        keyID,
                        layerName,
                        entropyTimeSeries[i]
                    );
                }
            }
        }
    }

    @Override
    public void writeTeamDynamics(
        String sessionID,
        String scenarioID,
        Map<String, List<Object>> teamDynamics
    ) throws IOException {
        String collectionName = mongoCollection + DYNAMICS_COLLECTION_SUFFIX;

        try {
            final String[] METRIC_LABELS = {
                "Enaction",
                "Adaptation",
                "Recovery",
                "Influence",
            };

            MongoCollection<Document> collection = getCollection(
                DYNAMICS_COLLECTION_SUFFIX
            );
            Document existingQuery = new Document(
                "sessionID",
                sessionID
            ).append("scenarioID", scenarioID);
            Document existingRecord = collection.find(existingQuery).first();

            if (existingRecord != null) {
                System.out.println(
                    "MongoDB: Skipping write. Team Dynamics already exist for Session ID: " +
                        sessionID +
                        ", Scenario ID: " +
                        scenarioID
                );
                return;
            }

            Map<String, Map<String, Object>> labeledDynamics =
                new java.util.LinkedHashMap<>();

            for (Map.Entry<
                String,
                List<Object>
            > entry : teamDynamics.entrySet()) {
                String subjectKey = entry.getKey();
                List<Object> rawMetrics = entry.getValue();

                Map<String, Object> subjectMetrics =
                    new java.util.LinkedHashMap<>();
                if (rawMetrics.size() == METRIC_LABELS.length) {
                    for (int i = 0; i < METRIC_LABELS.length; i++) {
                        subjectMetrics.put(METRIC_LABELS[i], rawMetrics.get(i));
                    }
                } else {
                    subjectMetrics.put("Raw_Data", rawMetrics);
                }

                labeledDynamics.put(subjectKey, subjectMetrics);
            }

            Map<String, Object> record = new java.util.LinkedHashMap<>();
            record.put("sessionID", sessionID);
            record.put("scenarioID", scenarioID);
            record.put("teamDynamics", labeledDynamics);

            String jsonString = json.writeValueAsString(record);
            Document doc = Document.parse(jsonString);
            collection.insertOne(doc);

            System.out.println(
                "MongoDB: Successfully wrote Team Dynamics to collection: " +
                    collectionName +
                    " (New record for Scenario ID: " +
                    scenarioID +
                    ")"
            );

            String fileName =
                csvExportDirectory + "/" + sessionID + "-dynamics.csv";
            try (
                PrintWriter writer = new PrintWriter(
                    new FileWriter(fileName, true)
                )
            ) {
                if (new java.io.File(fileName).length() == 0) {
                    writer.print("sessionID,scenarioID,subjectID");
                    for (String label : METRIC_LABELS) {
                        writer.print("," + label);
                    }
                    writer.println();
                }

                for (Map.Entry<
                    String,
                    Map<String, Object>
                > entry : labeledDynamics.entrySet()) {
                    String subjectKey = entry.getKey();
                    Map<String, Object> metrics = entry.getValue();

                    writer.print(
                        sessionID + "," + scenarioID + "," + subjectKey
                    );

                    for (String label : METRIC_LABELS) {
                        Object value = metrics.getOrDefault(label, "");
                        writer.print("," + String.valueOf(value));
                    }

                    if (metrics.containsKey("Raw_Data")) {
                        writer.print(
                            ",Raw_Data:\"" + metrics.get("Raw_Data") + "\""
                        );
                    }

                    writer.println();
                }

                System.out.println(
                    "CSV: Successfully exported Team Dynamics to: " + fileName
                );
            } catch (IOException csvE) {
                System.err.println(
                    "CSV Error writing team dynamics: " + csvE.getMessage()
                );
            }
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error writing team dynamics: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public SessionEntropyData readEntropy(String sessionID) throws IOException {
        String collectionName = mongoCollection + ENTROPY_COLLECTION_SUFFIX;

        try {
            MongoCollection<Document> collection = getCollection(
                ENTROPY_COLLECTION_SUFFIX
            );
            Document query = new Document("sessionID", sessionID);
            Document result = collection.find(query).first();

            if (result != null) {
                String jsonString = result.toJson();
                return json.readValue(jsonString, SessionEntropyData.class);
            }
            System.out.println(
                "MongoDB: Read attempt for SessionEntropyData from collection: " +
                    collectionName
            );
            return null;
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error reading entropy data: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public SessionMetadata readMetadata(String sessionID) throws IOException {
        String collectionName = mongoCollection + METADATA_COLLECTION_SUFFIX;

        try {
            MongoCollection<Document> collection = getCollection(
                METADATA_COLLECTION_SUFFIX
            );
            Document query = new Document("sessionID", sessionID);
            Document result = collection.find(query).first();

            if (result != null) {
                String jsonString = result.toJson();
                return json.readValue(jsonString, SessionMetadata.class);
            }
            System.out.println(
                "MongoDB: Read attempt for SessionMetadata from collection: " +
                    collectionName
            );
            return null;
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error reading metadata: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public Map<String, List<Object>> readTeamDynamics(
        String sessionID,
        String scenarioID
    ) throws IOException {
        String collectionName = mongoCollection + DYNAMICS_COLLECTION_SUFFIX;

        try {
            MongoCollection<Document> collection = getCollection(
                DYNAMICS_COLLECTION_SUFFIX
            );
            Document query = new Document("sessionID", sessionID).append(
                "scenarioID",
                scenarioID
            );
            Document result = collection.find(query).first();

            if (result != null) {
                String jsonString = result.toJson();
                @SuppressWarnings("unchecked")
                Map<String, Object> wrapperData = json.readValue(
                    jsonString,
                    Map.class
                );

                if (wrapperData.containsKey("teamDynamics")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Object>> teamDynamics = (Map<
                        String,
                        List<Object>
                    >) wrapperData.get("teamDynamics");
                    if (teamDynamics != null) {
                        System.out.println(
                            "MongoDB: Successfully read Team Dynamics for Session ID: " +
                                sessionID +
                                ", Scenario ID: " +
                                scenarioID
                        );
                    }
                    return teamDynamics;
                }
            }
            System.out.println(
                "MongoDB: Read attempt for Team Dynamics from collection: " +
                    collectionName
            );
            return null;
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error reading team dynamics: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public List<String> listAllSessions() throws IOException {
        String collectionName = mongoCollection + ENTROPY_COLLECTION_SUFFIX;
        try {
            MongoCollection<Document> collection = getCollection(
                ENTROPY_COLLECTION_SUFFIX
            );
            List<String> sessionIDs = new ArrayList<>();
            collection.distinct("sessionID", String.class).into(sessionIDs);
            System.out.println(
                "MongoDB: Retrieved " +
                    sessionIDs.size() +
                    " sessions from collection: " +
                    collectionName
            );
            return sessionIDs;
        } catch (Exception e) {
            throw new IOException(
                "MongoDB Error Listing Sessions: " + e.getMessage()
            );
        }
    }
}
