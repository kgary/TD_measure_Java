// package parser;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.io.*;
// import java.util.*;
// import model.*;

// public class MongoBatchParser implements DataParser {

//     private final ObjectMapper mapper = new ObjectMapper();
//     private SessionMetadata sessionMetadata;

//     public SessionMetadata getSessionMetadata() {
//         return this.sessionMetadata;
//     }

//     @Override
//     public List<List<List<String>>> parseToSTTCLayers(List<String> rawData)
//         throws IOException {
//         List<List<List<String>>> sttcLayers = new ArrayList<>();

//         for (String jsonString : rawData) {
//             JsonNode record = mapper.readTree(jsonString);
//             String type = record.get("type").asText();
//             if (!"TimerBatch".equals(type)) {
//                 continue;
//             }

//             String payload = record.get("payload").asText();
//             JsonNode payloadJson = mapper.readTree(payload);
//             JsonNode messages = payloadJson.get("Messages");

//             if (messages != null && messages.isArray()) {
//                 for (JsonNode message : messages) {
//                     String messageString = message.toString();
//                     JsonNode messageRecord = mapper.readTree(messageString);

//                     // Extract trainees array
//                     JsonNode trainees = messageRecord.get("trainees");
//                     if (trainees != null && trainees.isArray()) {
//                         for (JsonNode trainee : trainees) {
//                             List<List<String>> dataFrame = new ArrayList<>();

//                             List<String> commLayer = new ArrayList<>();
//                             JsonNode communication = trainee.get(
//                                 "Communication"
//                             );
//                             String commValue = (communication != null &&
//                                     communication.asBoolean())
//                                 ? "1"
//                                 : "0";
//                             commLayer.add(commValue);

//                             List<String> vizLayer = new ArrayList<>();
//                             JsonNode visualActivity = trainee.get(
//                                 "VisualActivity"
//                             );
//                             if (visualActivity != null) {
//                                 String ooi = (visualActivity.get(
//                                                 "OOI_Watched"
//                                             ) !=
//                                             null &&
//                                         visualActivity
//                                             .get("OOI_Watched")
//                                             .size() >
//                                         0)
//                                     ? "1"
//                                     : "0";
//                                 String aoi = (visualActivity.get(
//                                                 "AOI_Watched"
//                                             ) !=
//                                             null &&
//                                         visualActivity
//                                             .get("AOI_Watched")
//                                             .size() >
//                                         0)
//                                     ? "1"
//                                     : "0";
//                                 String roi = (visualActivity.get(
//                                                 "ROI_Watched"
//                                             ) !=
//                                             null &&
//                                         visualActivity
//                                             .get("ROI_Watched")
//                                             .size() >
//                                         0)
//                                     ? "1"
//                                     : "0";

//                                 String vizValue = roi + aoi + ooi;
//                                 vizLayer.add(vizValue);
//                             } else {
//                                 vizLayer.add("000");
//                             }

//                             dataFrame.add(commLayer);
//                             dataFrame.add(vizLayer);

//                             sttcLayers.add(dataFrame);
//                         }
//                     }
//                 }
//             }
//         }
//         return sttcLayers;
//     }
// }
