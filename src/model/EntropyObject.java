package model;

import java.util.Map;

public class EntropyObject {

    private Map<EntropyLayer, double[]> layerEntropies;

    public EntropyObject() {}

    public EntropyObject(Map<EntropyLayer, double[]> layerEntropies) {
        this.layerEntropies = layerEntropies;
    }

    public Map<EntropyLayer, double[]> getLayerEntropies() {
        return layerEntropies;
    }

    public void setLayerEntropies(Map<EntropyLayer, double[]> layerEntropies) {
        this.layerEntropies = layerEntropies;
    }
}
