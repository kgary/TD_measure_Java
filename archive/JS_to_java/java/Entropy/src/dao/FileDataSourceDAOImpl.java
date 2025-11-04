package dao;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileDataSourceDAOImpl implements DataSourceDAO {
    private String filePath; 

    public FileDataSourceDAOImpl(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<String> readData() throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}