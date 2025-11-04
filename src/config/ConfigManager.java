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
        return properties.getProperty("data.source.type", "mongo");
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
        return properties.getProperty("mongo.uri", "mongodb://10.203.43.162:27017");
    }
    
    public String getMongoDatabase() {
        return properties.getProperty("mongo.database", "Gift-Unity");
    }
    
    public String getMongoCollection() {
        return properties.getProperty("mongo.collection", "scenario-topic");
    }

    public String getCsvExportDirectory() {
        return properties.getProperty("csv.export.directory", "results");
    }

}