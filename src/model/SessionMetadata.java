package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMetadata {

    private String sessionID;
    private Map<String, Integer> scenarioIDs;
    private Map<String, List<Integer>> pertubationIDs;

    public SessionMetadata() {}

    public SessionMetadata(
        String sessionID,
        Map<String, Integer> scenarioIDs,
        Map<String, List<Integer>> pertubationIDs
    ) {
        this.sessionID = sessionID;
        this.scenarioIDs = scenarioIDs;
        this.pertubationIDs = pertubationIDs;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Map<String, Integer> getScenarioIDs() {
        return scenarioIDs;
    }

    public void setScenarioIDs(Map<String, Integer> scenarioIDs) {
        this.scenarioIDs = scenarioIDs;
    }

    public Map<String, List<Integer>> getPertubationIDs() {
        return pertubationIDs;
    }

    public void setPertubationIDs(Map<String, List<Integer>> pertubationIDs) {
        this.pertubationIDs = pertubationIDs;
    }
}
