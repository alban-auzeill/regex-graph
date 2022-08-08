package com.auzeill.regex.graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Dot {

  enum Type {
    SVG,
    PNG
  }
  public static byte[] generate(String dot, Type type) throws IOException, InterruptedException {
    Path dotFile = Files.createTempFile("graph", ".dot").toRealPath();
    Files.writeString(dotFile, dot, UTF_8);

    Path svgFile = Files.createTempFile("graph", ".svg").toRealPath();

    String[] args = {dotPath(), "-T" + type.name().toLowerCase(), "-o", svgFile.toString(), dotFile.toString()};
    Process dotProcess = Runtime.getRuntime().exec(args);
    dotProcess.waitFor();

    byte[] svgData = Files.readAllBytes(svgFile);
    Files.delete(dotFile);
    Files.delete(svgFile);
    return svgData;
  }

  public static String dotPath() throws IOException {
    String path = System.getProperty("dot", System.getenv("GRAPHVIZ_DOT"));
    if (path != null && !path.isBlank()) {
      if (!Files.exists(Paths.get(path))) {
        throw new IOException("Invalid dot path: " + path);
      }
      return path;
    } else if (File.separatorChar=='/') {
      if (Files.exists(Paths.get("/usr/bin/dot"))) {
        return "/usr/bin/dot";
      } else if (Files.exists(Paths.get("/usr/local/bin/dot"))) {
        return "/usr/local/bin/dot";
      } else if (Files.exists(Paths.get("/opt/homebrew/bin/dot"))) {
        return "/opt/homebrew/bin/dot";
      }
    }
    throw new IOException("Missing Graphviz dot in /usr/bin/dot, install 'graphviz' or define GRAPHVIZ_DOT environment variable.");
  }

}
