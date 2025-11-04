package dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileDataSourceDAOImpl implements DataSourceDAO {
    private String filePath; 

    public FileDataSourceDAOImpl(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<String> readData(String sessionID) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}