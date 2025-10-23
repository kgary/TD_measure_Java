package dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import model.*;

public interface ResultStorageDAO {
    public void writeEntropy(SessionEntropyData sessionEntropyData)
        throws IOException;

    public void writeMetadata(SessionMetadata sessionMetadata)
        throws IOException;
    public SessionEntropyData readEntropy(String sessionID) throws IOException;
    public SessionMetadata readMetadata(String sessionID) throws IOException;

    public void writeTeamDynamics(
        String sessionID,
        String scenarioID,
        Map<String, List<Object>> teamDynamics
    ) throws IOException;

    public Map<String, List<Object>> readTeamDynamics(
        String sessionID,
        String scenarioID
    ) throws IOException;
    public List<String> listAllSessions() throws IOException;
}
