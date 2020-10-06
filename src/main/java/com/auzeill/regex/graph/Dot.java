package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Dot {

  private static final String DOT = "/usr/bin/dot";

  public static byte[] generateSVG(String dot) throws IOException, InterruptedException {
    Path dotFile = Files.createTempFile("graph", ".dot").toRealPath();
    Files.writeString(dotFile, dot, UTF_8);

    Path svgFile = Files.createTempFile("graph", ".svg").toRealPath();

    String[] args = {DOT, "-Tsvg", "-o", svgFile.toString(), dotFile.toString()};
    Process dotProcess = Runtime.getRuntime().exec(args);
    dotProcess.waitFor();

    byte[] svgData = Files.readAllBytes(svgFile);
    Files.delete(dotFile);
    Files.delete(svgFile);
    return svgData;
  }

}
