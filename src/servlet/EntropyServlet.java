package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.IncompleteSessionException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SessionEntropyService;

@WebServlet(urlPatterns = { "/entropy/*" })
public class EntropyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(
        EntropyServlet.class
    );
    private ObjectMapper json;
    private SessionEntropyService service;

    public EntropyServlet(SessionEntropyService service) {
        this.service = service;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
        logger.info("EntropyServlet initialized");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String sessionId = req.getParameter("sessionID");
        String scenarioId = req.getParameter("scenarioID");
        String pertubationId = req.getParameter("pertubationID");
        String fromStr = req.getParameter("from");
        String toStr = req.getParameter("to");
        String timeStr = req.getParameter("time");

        logger.debug(
            "GET /entropy - sessionId={}, scenarioId={}, pertubationId={}, from={}, to={}, time={}",
            sessionId,
            scenarioId,
            pertubationId,
            fromStr,
            toStr,
            timeStr
        );

        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("Request rejected: sessionId is required");
            resp.setStatus(400);
            sendJsonError(resp, "sessionId is required");
            return;
        }

        try {
            Integer from = fromStr != null ? Integer.parseInt(fromStr) : null;
            Integer to = toStr != null ? Integer.parseInt(toStr) : null;
            Integer time = timeStr != null ? Integer.parseInt(timeStr) : null;

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
                result = service.getEntropyAtTime(sessionId, time);
            } else if (from != null && to != null) {
                logger.info(
                    "Fetching entropy in range [{}->{}] for sessionId={}",
                    from,
                    to,
                    sessionId
                );
                result = service.getEntropyInTimeRange(sessionId, from, to);
            } else if (scenarioId != null) {
                logger.info(
                    "Fetching entropy for scenarioId={}, sessionId={}",
                    scenarioId,
                    sessionId
                );
                result = service.getEntropyForScenario(sessionId, scenarioId);
            } else if (pertubationId != null) {
                logger.info(
                    "Fetching entropy for pertubationId={}, sessionId={}",
                    pertubationId,
                    sessionId
                );
                result = service.getEntropyForPerturbation(
                    sessionId,
                    pertubationId
                );
            } else {
                logger.info(
                    "Fetching entire session entropy for sessionId={}",
                    sessionId
                );
                result = service.getEntireSessionEntropy(sessionId);
            }

            if (result == null) {
                logger.warn("Session not found: sessionId={}", sessionId);
                resp.setStatus(409);
                sendJsonError(resp, "Session ID not found");
                return;
            }

            logger.debug(
                "Successfully retrieved {} for sessionId={}",
                result.getClass().getSimpleName(),
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
                "Server error processing request for sessionId={}: {}",
                sessionId,
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
