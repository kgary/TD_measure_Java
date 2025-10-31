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
            // return session entropy
        } else if (
            parts.length == 4 &&
            "scenarios".equals(parts[1]) &&
            "entropy".equals(parts[3])
        ) {
            // return scenario entropy
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
