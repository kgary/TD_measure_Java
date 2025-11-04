package dao;

import com.mongodb.client.*;
import java.io.*;
import java.util.*;
import org.bson.Document;

public class MongoDataSourceDAOImpl implements DataSourceDAO {

    private String mongoUri;
    private String dbName;
    private String collectionName;

    public MongoDataSourceDAOImpl(
        String mongoUri,
        String dbName,
        String collectionName
    ) {
        this.mongoUri = mongoUri;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    public List<String> readData(String sessionId) throws IOException {
        List<String> jsonStrings = new ArrayList<>();

        Document query = new Document("sessionID", sessionId);
        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            System.out.println("=== Available Databases ===");
            for (String name : mongoClient.listDatabaseNames()) {
                System.out.println("Database: " + name);
            }
            System.out.println("=========================");
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(
                collectionName
            );
            System.out.println("=== Collections in " + dbName + " ===");
            for (String collName : db.listCollectionNames()) {
                System.out.println("Collection: " + collName);
            }
            System.out.println("=========================");
            for (Document doc : collection.find(query)) {
                doc.remove("_id");
                doc.remove("_kafka_topic");
                doc.remove("_kafka_partition");
                doc.remove("_kafka_offset");
                jsonStrings.add(doc.toJson());
            }
        } catch (Exception e) {
            throw new IOException("Failed to read from MongoDB", e);
        }
        return jsonStrings;
    }
}
