package entropy;
import java.util.Arrays;
import java.util.List;

public class IndividualEntityAggregationStrategy implements AggregationStrategy {
    private int[] layerIndices;
    private int individualIndex;

    public IndividualEntityAggregationStrategy(int[] layerIndices, int individualIndex) {
        this.layerIndices = layerIndices;
        this.individualIndex = individualIndex;
    }

    @Override
    public List<String> generateStateKeys(String[][] timePoint) {
        StringBuilder individualKey = new StringBuilder();
        
        for (int layerIndex: layerIndices) {
            individualKey.append(timePoint[layerIndex][individualIndex]);
        }

        return Arrays.asList(individualKey.toString());
    }

    @Override
    public int getKeysPerTimePoint() {
        return 1;
    }
}
