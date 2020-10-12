package com.auzeill.regex.graph;

import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

public class Server extends NanoHTTPD {

  private static final int DEFAULT_SERVER_PORT = Integer.parseInt(System.getProperty("port", "9000"));

  public static void main(String[] args) throws IOException {
    new Server(DEFAULT_SERVER_PORT);
  }

  public Server(int port) throws IOException {
    super(port);
    // check dot presence before starting the server
    Dot.dotPath();
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("Running! Open http://localhost:" + getListeningPort() + "/ in your browser.");
  }

  @Override
  public Response serve(IHTTPSession session) {
    Map<String, List<String>> parameters = session.getParameters();
    try {
      switch (session.getUri()) {
        case "/":
          return resourceResponse("/index.html", "text/html");
        case "/favicon.ico":
        case "/favicon.png":
          return resourceResponse("/favicon.png", "image/png");
        case "/pattern":
        case "/regex-tree":
          List<String> exp = parameters.get("exp");
          if (exp == null || exp.isEmpty()) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Missing exp parameter");
          }
          try {
            String stringLiteral = exp.get(0);
            if (!stringLiteral.startsWith("\"") || !stringLiteral.endsWith("\"")) {
              throw new IllegalArgumentException("Expecting a string literal with double quotes \"...\"");
            }
            boolean includeTrees = parameters.containsKey("trees");
            boolean includeStates = parameters.containsKey("states");
            boolean legend = parameters.containsKey("legend");
            String dot = session.getUri().equals("/pattern") ? PatternGraph.transform(stringLiteral) : RegexTreeGraph.transform(stringLiteral, includeTrees, includeStates, legend);
            if (parameters.containsKey("edit")) {
              String html = "<pre>\n" + dot.replace("&", "&amp;").replace("<", "&lt;") + "</pre>\n";
              return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, html);
            }
            byte[] svg = Dot.generateSVG(dot);
            return newFixedLengthResponse(Response.Status.OK, "image/svg+xml", new ByteArrayInputStream(svg), svg.length);
          } catch (Exception e) {
            byte[] svg = ("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"500\" width=\"1024\">" +
              "<text x=\"0\" y=\"250\" fill=\"red\">" +
              StringEscapeUtils.escapeHtml4("Exception(" + e.getClass().getName() + "): " + e.getMessage()) +
              "</text>" +
              "</svg>").getBytes(StandardCharsets.UTF_8);
            return newFixedLengthResponse(Response.Status.OK, "image/svg+xml", new ByteArrayInputStream(svg), svg.length);
          }
        default:
          return super.serve(session);
      }
    } catch (IOException ex) {
      String message = "Internal Error (" + ex.getClass().getName() + "): " + ex.getMessage();
      return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, message);
    }
  }

  public Response resourceResponse(String resourcePath, String mimeType) throws IOException {
    byte[] bytes = IOUtils.resourceToByteArray(resourcePath);
    return newFixedLengthResponse(Response.Status.OK, mimeType, new ByteArrayInputStream(bytes), bytes.length);
  }

}
