package dao;

import java.util.*;
import java.io.*;


public interface DataSourceDAO {
    List<String> readData() throws IOException;
}