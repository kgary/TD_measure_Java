package dao;

import java.io.*;
import java.util.*;

public interface DataSourceDAO {

    public List<String> readData(String sessionId) throws IOException;
}
