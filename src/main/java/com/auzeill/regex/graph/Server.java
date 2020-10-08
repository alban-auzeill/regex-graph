package com.auzeill.regex.graph;

import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

public class Server extends NanoHTTPD {

  private static final int SERVER_PORT = 9000;

  public static void main(String[] args) throws IOException {
    new Server();
  }

  public Server() throws IOException {
    super(SERVER_PORT);
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("Running! Open http://localhost:" + SERVER_PORT + "/ in your browser.");
  }

  @Override
  public Response serve(IHTTPSession session) {
    try {
      switch (session.getUri()) {
        case "/":
          return resourceResponse("/index.html", "text/html");
        case "/favicon.ico":
        case "/favicon.png":
          return resourceResponse("/favicon.png", "image/png");
        case "/pattern":
        case "/regex-tree":
          List<String> exp = session.getParameters().get("exp");
          if (exp == null || exp.isEmpty()) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Missing exp parameter");
          }
          try {
            String stringLiteral = exp.get(0);
            if (!stringLiteral.startsWith("\"") || !stringLiteral.endsWith("\"")) {
              throw new IllegalArgumentException("Expecting a string literal with double quotes \"...\"");
            }
            String dot = session.getUri().equals("/pattern") ? PatternGraph.transform(stringLiteral) : RegexTreeGraph.transform(stringLiteral);
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
