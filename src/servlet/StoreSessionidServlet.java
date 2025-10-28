package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SessionEntropyService;

@WebServlet(urlPatterns = { "/bindSessions/*" })
public class StoreSessionidServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StoreSessionidServlet.class);

    private ObjectMapper json;
    private final SessionEntropyService sessionEntropyService;

    public StoreSessionidServlet(SessionEntropyService sessionEntropyService) {
        this.sessionEntropyService = sessionEntropyService;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
        logger.info("StoreSessionidServlet initialized");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            Map<String, Object> body = json.readValue(
                req.getInputStream(),
                new TypeReference<Map<String, Object>>() {}
            );

            Object giftObj = body.get("giftSessionId");
            Object unityObj = body.get("unitySessionId");
            Object scenarioObj = body.get("scenarioId");

            if (giftObj == null || unityObj == null || scenarioObj == null) {
                logger.warn("POST /storesession - missing required fields");
                resp.setStatus(400);
                sendJsonError(resp, Map.of(
                    "error", "All Fields are required"
                ));
                return;
            }
            Integer giftSessionId = null;
            if (giftObj instanceof Number) {
                giftSessionId = ((Number) giftObj).intValue();
            } else if (giftObj instanceof String) {
                try {
                    giftSessionId = Integer.parseInt((String) giftObj);
                } catch (NumberFormatException nfe) {
                    // handled below
                }
            }

            String unitySessionId = (unityObj instanceof String) ? (String) unityObj : null;
            if (unitySessionId == null || unitySessionId.isBlank()) {
                logger.warn("POST /storesession - invalid types or empty values");
                resp.setStatus(400);
                sendJsonError(resp, Map.of(
                    "error", "Invalid payload: unitySessionId is empty"
                ));
                return;
            }

            logger.info(
                "Storing session IDs: giftSessionId={}, unitySessionId={}",
                giftSessionId, unitySessionId
            );

            String scenarioID = (scenarioObj instanceof String) ? (String) scenarioObj : null;
            if (scenarioID == null || scenarioID.isBlank()) {
                logger.warn("POST /storesession - invalid scenarioId");
                resp.setStatus(400);
                sendJsonError(resp, Map.of(
                    "error", "Invalid payload: scenarioId is empty"
                ));
                return;
            }

            sessionEntropyService.StoreSessionIds(giftSessionId, unitySessionId, scenarioID);

            resp.setStatus(201);
            sendJsonResponse(resp, Map.of(
                "status", "created",
                "message", "Session IDs stored",
                "giftSessionId", giftSessionId,
                "unitySessionId", unitySessionId,
                "scenarioId", scenarioID
            ));
        } catch (Exception e) {
            logger.error("Error in POST /storesession: {}", e.getMessage(), e);
            resp.setStatus(500);
            sendJsonError(resp, Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    private void sendJsonError(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }
}
