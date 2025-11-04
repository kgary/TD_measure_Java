package entropy;
import java.util.List;

public interface AggregationStrategy {
    List<String> generateStateKeys(String[][] timePoint);
    int getKeysPerTimePoint();
}
