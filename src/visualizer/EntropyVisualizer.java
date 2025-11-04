package visualizer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class EntropyVisualizer {

    public void generateVisualization(
        List<double[]> entropyData,
        List<String> entropyNames,
        String filename
    ) throws IOException {
        String jsonData = entropyToJson(entropyData, entropyNames);
        String html = generateHTML(jsonData);
        Files.write(Paths.get(filename), html.getBytes());
        System.out.println("Entropy visualization saved to: " + filename);
    }

    private String entropyToJson(
        List<double[]> entropyData,
        List<String> entropyNames
    ) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int layer = 0; layer < entropyData.size(); layer++) {
            json.append("{");
            json
                .append("\"name\": \"")
                .append(entropyNames.get(layer))
                .append("\",");
            json.append("\"data\": [");

            double[] layerData = entropyData.get(layer);
            for (int time = 0; time < layerData.length; time++) {
                json
                    .append("{\"time\": ")
                    .append(time)
                    .append(", \"entropy\": ")
                    .append(layerData[time])
                    .append("}");
                if (time < layerData.length - 1) {
                    json.append(",");
                }
            }

            json.append("]");
            json.append("}");
            if (layer < entropyData.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private String generateHTML(String jsonData) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"en\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append(
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        );
        html.append("<title>Team Dynamics Entropy Plots</title>");
        html.append("<script src=\"https://d3js.org/d3.v7.min.js\"></script>");
        html.append("<style>");
        html.append(
            "body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }"
        );
        html.append(
            ".header { background: #2c3e50; color: white; padding: 15px; margin-bottom: 20px; }"
        );
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(
            ".charts-container { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; }"
        );
        html.append(
            ".chart { background: white; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }"
        );
        html.append(
            ".chart h3 { margin-top: 0; text-align: center; color: #2c3e50; }"
        );
        html.append(
            ".line { fill: none; stroke: steelblue; stroke-width: 2px; }"
        );
        html.append(".axis { font-size: 12px; }");
        html.append(
            ".axis path, .axis line { fill: none; stroke: #333; shape-rendering: crispEdges; }"
        );
        html.append(
            ".grid line { stroke: lightgrey; stroke-opacity: 0.7; shape-rendering: crispEdges; }"
        );
        html.append(".grid path { stroke-width: 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"header\">");
        html.append("<h1>Team Dynamics Entropy Plots</h1>");
        html.append("<p>Trainees = 3, Casualties = 3, Window Size = 4</p>");
        html.append("</div>");
        html.append("<div class=\"charts-container\" id=\"charts\"></div>");
        html.append("<script>");
        html.append("const data = ").append(jsonData).append(";");
        html.append(
            "const margin = {top: 20, right: 30, bottom: 40, left: 80};"
        );
        html.append("const width = 560 - margin.left - margin.right;");
        html.append("const height = 240 - margin.top - margin.bottom;");
        html.append(
            "data.forEach((layer, index) => { createChart(layer, index); });"
        );
        html.append("function createChart(layerData, index) {");
        html.append(
            "const chartDiv = d3.select('#charts').append('div').attr('class', 'chart');"
        );
        html.append("chartDiv.append('h3').text(layerData.name + ' Entropy');");
        html.append("const svg = chartDiv.append('svg')");
        html.append(".attr('width', width + margin.left + margin.right)");
        html.append(".attr('height', height + margin.top + margin.bottom)");
        html.append(".append('g')");
        html.append(
            ".attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');"
        );
        html.append(
            "const xScale = d3.scaleLinear().domain(d3.extent(layerData.data, d => d.time)).range([0, width]);"
        );
        html.append(
            "const yScale = d3.scaleLinear().domain(d3.extent(layerData.data, d => d.entropy)).nice().range([height, 0]);"
        );
        html.append(
            "const line = d3.line().x(d => xScale(d.time)).y(d => yScale(d.entropy)).curve(d3.curveLinear);"
        );
        html.append(
            "svg.append('g').attr('class', 'grid').attr('transform', 'translate(0,' + height + ')')"
        );
        html.append(
            ".call(d3.axisBottom(xScale).tickSize(-height).tickFormat(''));"
        );
        html.append("svg.append('g').attr('class', 'grid')");
        html.append(
            ".call(d3.axisLeft(yScale).tickSize(-width).tickFormat(''));"
        );
        html.append(
            "svg.append('path').datum(layerData.data).attr('class', 'line').attr('d', line);"
        );
        html.append(
            "svg.append('g').attr('class', 'axis').attr('transform', 'translate(0,' + height + ')')"
        );

        html.append(".call(d3.axisBottom(xScale));");
        html.append(
            "svg.append('g').attr('class', 'axis').call(d3.axisLeft(yScale));"
        );
        html.append(
            "svg.append('g').attr('class', 'axis').call(d3.axisTop(xScale).tickFormat(''));"
        );
        html.append(
            "svg.append('g').attr('class', 'axis').attr('transform', 'translate(' + width + ',0)').call(d3.axisRight(yScale).tickFormat(''));"
        );
        html.append("svg.append('text')");
        html.append(
            ".attr('transform', 'translate(' + (width/2) + ' ,' + (height + margin.bottom - 5) + ')')"
        );
        html.append(
            ".style('text-anchor', 'middle').style('font-size', '12px').text('Time (Sec)');"
        );
        html.append("svg.append('text').attr('transform', 'rotate(-90)')");
        html.append(
            ".attr('y', 0 - margin.left + 15).attr('x', 0 - (height / 2))"
        );
        html.append(
            ".style('text-anchor', 'middle').style('font-size', '12px').text('Entropy');"
        );
        html.append("}");
        html.append("</script>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
