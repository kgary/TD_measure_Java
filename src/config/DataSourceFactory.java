package config;

import dao.*;

public class DataSourceFactory {

    public static DataSourceDAO createDataSource(
        ConfigManager config,
        String sourceType
    ) {
        switch (sourceType.toLowerCase()) {
            case "file":
                return new FileDataSourceDAOImpl(config.getFilePath());
            case "mongo":
                return new MongoDataSourceDAOImpl(
                    config.getMongoUri(),
                    config.getMongoDatabase(),
                    config.getMongoCollection()
                );
            default:
                throw new IllegalArgumentException(
                    "Unknown data source type: " + sourceType
                );
        }
    }
}
