package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.*;
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

@WebServlet(urlPatterns = { "/sessions/*" })
public class SessionsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(
        SessionsServlet.class
    );

    private ObjectMapper json;
    private final SessionEntropyService sessionEntropyService;

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
        logger.info("SessionsServlet initialized");
    }

    public SessionsServlet(SessionEntropyService sessionEntropyService) {
        this.sessionEntropyService = sessionEntropyService;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleListSessions(req, resp);
        }
        String[] parts = pathInfo.substring(1).split("/");
        String sessionId = parts[0];

        if (parts.length == 1) {
            handleGetSession(sessionId, req, resp);
        } else if (parts.length == 2 && "entropy".equals(parts[1])) {
            handleSessionEntropy(sessionId, req, resp);
        } else if (
            parts.length == 4 &&
            "scenarios".equals(parts[1]) &&
            "entropy".equals(parts[3])
        ) {
            handleScenarioEntropy(sessionId, parts[2], req, resp);
        } else if (
            parts.length == 4 &&
            "perturbations".equals(parts[1]) &&
            "entropy".equals(parts[3])
        ) {
            // return perturbation entropy
        }
    }

    public void handleListSessions(
        HttpServletRequest req,
        HttpServletResponse resp
    ) throws IOException {
        logger.debug("GET /sessions - Fetching all sessions");

        try {
            Map<String, Object> result =
                sessionEntropyService.listAllSessions();
            logger.info(
                "Successfully retrieved {} sessions",
                result.get("totalCount")
            );
            sendJsonResponse(resp, result);
        } catch (UnsupportedOperationException e) {
            logger.warn(
                "List sessions operation not supported: {}",
                e.getMessage()
            );
            resp.setStatus(501);
            sendJsonError(
                resp,
                "List sessions not supported for current storage configuration"
            );
        } catch (Exception e) {
            logger.error("Error fetching sessions list: {}", e.getMessage(), e);
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void handleGetSession(
        String sessionId,
        HttpServletRequest req,
        HttpServletResponse resp
    ) throws IOException {
        logger.debug("GET /sessions/{} - Fetching session metadata", sessionId);
        try {
            Object result = sessionEntropyService.getSessionMetadata(sessionId);

            if (result == null) {
                logger.warn(
                    "Session metadata not found: sessionId={}",
                    sessionId
                );
                resp.setStatus(404);
                sendJsonError(resp, "Session not found");
                return;
            }

            logger.info(
                "Successfully retrieved metadata for sessionId={}",
                sessionId
            );
            sendJsonResponse(resp, result);
        } catch (Exception e) {
            logger.error(
                "Error fetching metadata for sessionId={}: {}",
                sessionId,
                e.getMessage(),
                e
            );
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void handleSessionEntropy(
        String sessionId,
        HttpServletRequest req,
        HttpServletResponse resp
    ) throws IOException {
        String fromStr = req.getParameter("from");
        String toStr = req.getParameter("to");
        String timeStr = req.getParameter("time");

        logger.debug(
            "GET /sessions/{}/entropy - from={}, to={}, time={}",
            sessionId,
            fromStr,
            toStr,
            timeStr
        );

        try {
            Integer from = fromStr != null ? Integer.parseInt(fromStr) : null;
            Integer to = toStr != null ? Integer.parseInt(toStr) : null;
            Integer time = timeStr != null ? Integer.parseInt(timeStr) : null;

            if (time != null && (from != null || to != null)) {
                logger.warn(
                    "Invalid params: cannot use 'time' with 'from'/'to'"
                );
                resp.setStatus(400);
                sendJsonError(
                    resp,
                    "Cannot specify 'time' together with 'from' or 'to'"
                );
                return;
            }

            if ((from != null && to == null) || (from == null && to != null)) {
                logger.warn(
                    "Invalid params: must specify both 'from' and 'to'"
                );
                resp.setStatus(400);
                sendJsonError(
                    resp,
                    "Must specify both 'from' and 'to' for range query"
                );
                return;
            }

            if (from != null && to != null && from >= to) {
                logger.warn(
                    "Invalid range: from={} must be less than to={}",
                    from,
                    to
                );
                resp.setStatus(400);
                sendJsonError(resp, "from must be less than to");
                return;
            }

            Object result;
            if (time != null) {
                logger.info(
                    "Fetching entropy at time={} for sessionId={}",
                    time,
                    sessionId
                );
                result = sessionEntropyService.getEntropyAtTime(
                    sessionId,
                    time
                );
            } else if (from != null && to != null) {
                logger.info(
                    "Fetching entropy in range [{}->{}] for sessionId={}",
                    from,
                    to,
                    sessionId
                );
                result = sessionEntropyService.getEntropyInTimeRange(
                    sessionId,
                    from,
                    to
                );
            } else {
                logger.info(
                    "Fetching entire session entropy for sessionId={}",
                    sessionId
                );
                result = sessionEntropyService.getEntireSessionEntropy(
                    sessionId
                );
            }

            if (result == null) {
                logger.warn("Entropy not found for sessionId={}", sessionId);
                resp.setStatus(404);
                sendJsonError(resp, "Session entropy not found");
                return;
            }

            logger.debug(
                "Successfully retrieved entropy for sessionId={}",
                sessionId
            );
            sendJsonResponse(resp, result);
        } catch (NumberFormatException e) {
            logger.error(
                "Invalid number format in query parameters: {}",
                e.getMessage()
            );
            resp.setStatus(400);
            sendJsonError(resp, "Invalid number format for query parameters");
        } catch (IncompleteSessionException e) {
            logger.warn("Incomplete session data: {}", e.getMessage());
            resp.setStatus(422);
            sendJsonError(resp, "Incomplete session: " + e.getMessage());
        } catch (Exception e) {
            logger.error(
                "Error fetching entropy for sessionId={}: {}",
                sessionId,
                e.getMessage(),
                e
            );
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void handleScenarioEntropy(
        String sessionId,
        String scenarioId,
        HttpServletRequest req,
        HttpServletResponse resp
    ) throws IOException {
        logger.debug(
            "GET /sessions/{}/scenarios/{}/entropy",
            sessionId,
            scenarioId
        );

        try {
            Object result = sessionEntropyService.getEntropyForScenario(
                sessionId,
                scenarioId
            );

            if (result == null) {
                logger.warn(
                    "Scenario entropy not found: sessionId={}, scenarioId={}",
                    sessionId,
                    scenarioId
                );
                resp.setStatus(404);
                sendJsonError(resp, "Scenario entropy not found");
                return;
            }

            logger.info(
                "Successfully retrieved entropy for sessionId={}, scenarioId={}",
                sessionId,
                scenarioId
            );
            sendJsonResponse(resp, result);
        } catch (Exception e) {
            logger.error(
                "Error fetching scenario entropy: sessionId={}, scenarioId={}: {}",
                sessionId,
                scenarioId,
                e.getMessage(),
                e
            );
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(200);
        resp.getWriter().write(json.writeValueAsString(data));
    }

    private void sendJsonError(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }
}
