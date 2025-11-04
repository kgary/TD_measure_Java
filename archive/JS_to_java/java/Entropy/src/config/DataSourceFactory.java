package config;
import dao.*;

import java.lang.IllegalArgumentException;

public class DataSourceFactory {

    public static DataSourceDAO createDataSource(ConfigManager config) {
        String sourceType = config.getDataSourceType();

        switch(sourceType.toLowerCase()) {
            case "file":
                return new FileDataSourceDAOImpl(config.getFilePath());
            case "kafka":
                return new KafkaDataSourceDAOImpl(
                    config.getKafkaTopic(),
                    config.getKafkaBootstrapServers(),  
                    config.getKafkaGroupId()   
                    );
            case "mongo":
                return new MongoDataSourceDAOImpl(
                    config.getMongoUri(),
                    config.getMongoDatabase(), 
                    config.getMongoCollection()
                );
            default:
                throw new IllegalArgumentException("Unknown data source type: " + sourceType);
        }
    }
}