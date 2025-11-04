package model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class SessionEntropyData {

    private String sessionID;
    private EntropyObject session_entropy;
    private Map<String, EntropyObject> scenarioEntropy;
    private Map<String, EntropyObject> pertubationEntropy;

    public SessionEntropyData() {}

    public SessionEntropyData(
        String sessionID,
        EntropyObject session_entropy,
        Map<String, EntropyObject> scenarioEntropy,
        Map<String, EntropyObject> pertubationEntropy
    ) {
        this.sessionID = sessionID;
        this.session_entropy = session_entropy;
        this.scenarioEntropy = scenarioEntropy;
        this.pertubationEntropy = pertubationEntropy;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public EntropyObject getSession_entropy() {
        return session_entropy;
    }

    public void setSession_entropy(EntropyObject session_entropy) {
        this.session_entropy = session_entropy;
    }

    public Map<String, EntropyObject> getScenarioEntropy() {
        return scenarioEntropy;
    }

    public void setScenarioEntropy(Map<String, EntropyObject> scenarioEntropy) {
        this.scenarioEntropy = scenarioEntropy;
    }

    public Map<String, EntropyObject> getPertubationEntropy() {
        return pertubationEntropy;
    }

    public void setPertubationEntropy(
        Map<String, EntropyObject> pertubationEntropy
    ) {
        this.pertubationEntropy = pertubationEntropy;
    }
}
