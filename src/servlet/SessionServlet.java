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
import service.*;

@WebServlet(urlPatterns = { "/session/*" })
public class SessionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(
        EntropyServlet.class
    );
    private ObjectMapper json;
    private SessionEntropyService sessionEntropyService;

    public SessionServlet(SessionEntropyService sessionEntropyService) {
        this.sessionEntropyService = sessionEntropyService;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
        logger.info("EntropyServlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String sessionID = req.getParameter("sessionID");

        logger.debug("GET /session - sessionID={}", sessionID);

        if (sessionID == null || sessionID.isEmpty()) {
            logger.warn("Request rejected: sessionID is required");
            resp.setStatus(400);
            sendJsonError(resp, "sessionId is required");
            return;
        }

        try {
            logger.info("Fetching metadata for sessionID={}", sessionID);
            Object result = sessionEntropyService.getSessionMetadata(sessionID);

            if (result == null) {
                logger.warn(
                    "Session metadata not found: sessionID={}",
                    sessionID
                );
                resp.setStatus(404);
                sendJsonError(resp, "Session ID not found");
                return;
            }

            logger.debug(
                "Successfully retrieved {} for sessionID={}",
                result.getClass().getSimpleName(),
                sessionID
            );
            sendJsonResponse(resp, result);
        } catch (Exception e) {
            logger.error(
                "Error fetching session metadata for sessionID={}: {}",
                sessionID,
                e.getMessage(),
                e
            );
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            Map<String, String> body = json.readValue(
                req.getInputStream(),
                new TypeReference<Map<String, String>>() {}
            );

            String sessionID = body.get("sessionID");
            String dataSourceType = body.get("dataSourceType");

            logger.debug(
                "POST /session - sessionID={}, dataSourceType={}",
                sessionID,
                dataSourceType
            );

            if (sessionID == null) {
                logger.warn(
                    "Request rejected: sessionID missing in request body"
                );
                resp.setStatus(400);
                sendJsonError(resp, "sessionID missing");
                return;
            }

            logger.info(
                "Starting entropy calculation for sessionID={}, dataSourceType={}",
                sessionID,
                dataSourceType
            );

            sessionEntropyService.CalculateEntropy(sessionID, dataSourceType);

            logger.info(
                "Entropy calculation accepted for sessionID={}",
                sessionID
            );

            resp.setStatus(202);
            resp.setContentType("application/json");
            resp
                .getWriter()
                .print(
                    "{\"status\": \"accepted\", \"message\": \"Processing started\"}"
                );
        } catch (Exception e) {
            logger.error(
                "Error processing entropy calculation request: {}",
                e.getMessage(),
                e
            );
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void sendJsonError(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(200);
        resp.getWriter().write(json.writeValueAsString(data));
    }
}
