package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.SessionEntropyService;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = {"/teamdynamics/*"})
public class TeamDynamicsServlet extends HttpServlet {

    private SessionEntropyService service;
    private ObjectMapper json;

    public TeamDynamicsServlet(SessionEntropyService service) {
        this.service = service;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sessionId = request.getParameter("sessionId");
        String scenarioId = request.getParameter("scenarioId");
        String pertubationId = request.getParameter("perturbationId");    

        if (sessionId == null || sessionId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendJsonError(response, "sessionId parameter is required (e.g., /teamdynamics?sessionId=session_id_here).");
            return;
        } else if (scenarioId == null || scenarioId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendJsonError(response, "scenarioId parameter is required (e.g., /teamdynamics?scenarioId=scenario_id_here).");
            return;
        }

        try {
            System.out.println("GET /teamdynamics - sessionId: " + sessionId + ", scenarioId: " + scenarioId);

            Map<String, List<Object>> teamDynamics = service.getScenarioTeamDynamics(sessionId, scenarioId, pertubationId);

            if (teamDynamics == null || teamDynamics.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendJsonError(response, "Team Dynamics not found for session ID: " + sessionId + " and scenario ID: " + scenarioId);
                return;
            }

            sendJsonResponse(response, teamDynamics);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendJsonError(response, "Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json.writeValueAsString(data));
    }

    private void sendJsonError(HttpServletResponse resp, String message)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(Map.of("error", message)));
    }
}
