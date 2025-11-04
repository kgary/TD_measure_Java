package parser;

import java.io.*;
import java.util.*;
import model.*;

public interface DataParser {
    List<List<List<String>>> parseToSTTCLayers(List<String> rawData)
        throws IOException;
    SessionMetadata getSessionMetadata();
    Map<String, String> getTraineeInfo(List<String> rawData);
}
