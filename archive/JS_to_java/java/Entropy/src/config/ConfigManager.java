package config;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private Properties properties;

    public ConfigManager(String configFile) throws IOException {
        this.properties = new Properties();
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        }
    }

    public String getDataSourceType() {
        return properties.getProperty("data.source.type", "file");
    }

    public String getFilePath() {
        return properties.getProperty("file.path", "performance_data.txt");
    }
    
    public String getKafkaTopic() {
        return properties.getProperty("kafka.topic", "default");
    }

    public String getKafkaBootstrapServers() {
    return properties.getProperty("kafka.bootstrap.servers", "localhost:9092");
    }

    public String getKafkaGroupId() {
        return properties.getProperty("kafka.group.id", "default-group");
    }

    public String getMongoUri() {
        return properties.getProperty("mongo.uri", "mongodb://localhost:27017");
    }
    
    public String getMongoDatabase() {
        return properties.getProperty("mongo.database", "gift");
    }
    
    public String getMongoCollection() {
        return properties.getProperty("mongo.collection", "kafka_messages");
    }

}