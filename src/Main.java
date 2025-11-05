import config.*;
import java.io.*;
import java.util.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.*;
import servlet.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void dumpToCSV(double[][] data, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            int totalTime = data[0].length;

            writer.print("Time");
            for (int t = 0; t < totalTime; t++) {
                writer.print("," + t);
            }
            writer.println();

            for (int layer = 0; layer < data.length; layer++) {
                writer.print("Layer " + layer);
                for (int t = 0; t < totalTime; t++) {
                    writer.print("," + data[layer][t]);
                }
                writer.println();
            }

            System.out.println("Data exported to: " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void dumpToFile(List<String> data, String fileName)
        throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("[\n");
            for (int i = 0; i < data.size(); i++) {
                writer.write("  " + data.get(i));
                if (i < data.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("]\n");
            System.out.println("Data dumped to: " + fileName);
        }
    }

    public static void main(String[] Args) throws Exception {
        logger.info("Initializing STTC-Entropy Server");

        ConfigManager config = new ConfigManager(
            "resources/runtime.properties"
        );
        logger.debug("Configuration loaded from runtime.properties");

        SessionEntropyService sessionEntropyService = new SessionEntropyService(
            config
        );
        logger.info("SessionEntropyService initialized");

        Server server = new Server(Integer.parseInt(config.getServicePort()));
        ServletContextHandler context = new ServletContextHandler(
            ServletContextHandler.SESSIONS
        );
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(
            new ServletHolder(new EntropyServlet(sessionEntropyService)),
            "/entropy/*"
        );
        logger.info("Registered endpoint: /entropy/*");

        context.addServlet(
            new ServletHolder(new SessionServlet(sessionEntropyService)),
            "/session/*"
        );
        logger.info("Registered endpoint: /session/*");

        context.addServlet(
            new ServletHolder(new TeamDynamicsServlet(sessionEntropyService)),
            "/teamdynamics/*"
        );
        logger.info("Registered endpoint: /teamdynamics/*");

        context.addServlet(
            new ServletHolder(new StoreSessionidServlet(sessionEntropyService)),
            "/bindSessions/*"
        );
        logger.info("Registered endpoint: /bindSessions/*");
        context.addServlet(
            new ServletHolder(new SessionsServlet(sessionEntropyService)),
            "/sessions/*"
        );

        server.start();
        logger.info(
            "Server started successfully on port " + config.getServicePort()
        );
        logger.info("Available endpoints:");
        logger.info("  - http://localhost:8081/entropy/*");
        logger.info("  - http://localhost:8081/session/*");
        logger.info("  - http://localhost:8081/teamdynamics/*");
        logger.info("  - http://localhost:8081/bindSessions/*");
        logger.info("  - http://localhost:8081/sessions/*");

        server.join();
    }
}
