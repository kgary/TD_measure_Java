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
