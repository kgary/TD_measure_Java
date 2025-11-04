package dao;

import java.util.*;
import java.io.*;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDataSourceDAOImpl implements DataSourceDAO {
    private String mongoUri;
    private String dbName;
    private String collectionName;

    public MongoDataSourceDAOImpl(String mongoUri, String dbName, String collectionName) {
        this.mongoUri = mongoUri;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    public List<String> readData() throws IOException {
        List<String> jsonStrings = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(collectionName);

            for (Document doc: collection.find()) {
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