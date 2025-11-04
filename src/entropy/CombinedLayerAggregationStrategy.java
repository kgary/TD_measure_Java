package entropy;
import java.util.Arrays;
import java.util.List;

public class CombinedLayerAggregationStrategy implements AggregationStrategy {
    private int[] indices;

    public CombinedLayerAggregationStrategy(int[] indices) {
        this.indices = indices;
    }

    @Override
    public List<String> generateStateKeys(String[][] timePoint) {
        StringBuilder combinedKey = new StringBuilder();

        for (int layerIndex: indices) {
            for (String value: timePoint[layerIndex]) {
                combinedKey.append(value);
            }
        }

        return Arrays.asList(combinedKey.toString());
    }

    @Override
    public int getKeysPerTimePoint() {
        return 1;
    }
    
}
