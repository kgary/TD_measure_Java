package entropy;

import java.util.Arrays;
import java.util.List;

public class LayerAggregationStrategy implements AggregationStrategy{

    private int layerIndex;

    public LayerAggregationStrategy(int layerIndex) {
        this.layerIndex = layerIndex;
    }

    @Override 
    public List<String> generateStateKeys(String[][] timePoint) {
        StringBuilder layerKey = new StringBuilder();
        
        for (String value: timePoint[layerIndex]) {
            layerKey.append(value);
        }
        return Arrays.asList(layerKey.toString());
    }

    @Override
    public int getKeysPerTimePoint() {
        return 1;
    }
}
