package parser;

import java.io.*;
import java.util.*;


public interface DataParser {
    List<List<List<String>>> parseToSTTCLayers(List<String> rawData) throws IOException;
}