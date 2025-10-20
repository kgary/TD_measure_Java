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
            String[] layerData = timePoint[layerIndex]; 
            if (layerData == null || layerData.length <= individualIndex) {
                continue; 
                
            } else {
                individualKey.append(layerData[individualIndex]);
            }
        }

        return Arrays.asList(individualKey.toString());
    }

    @Override
    public int getKeysPerTimePoint() {
        return 1;
    }
}
