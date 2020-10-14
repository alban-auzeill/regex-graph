package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class GenerateAllPng {

  public static void main(String[] args) throws IOException, InterruptedException {
    Path srcDirectory = Paths.get("src", "test", "resources", "regex");
    Path destDirectory = Paths.get("build", "tmp", "png");
    Files.createDirectories(destDirectory);

    String legendDot = RegexTreeGraph.transform("\"\"", true, true, true);
    Files.write(destDirectory.resolve("legend.png"), Dot.generate(legendDot, Dot.Type.PNG));

    List<Function<String, String>> transforms = Arrays.asList(
      PatternGraph::transform,
      stringLiteral -> RegexTreeGraph.transform(stringLiteral, true, false, false),
      stringLiteral -> RegexTreeGraph.transform(stringLiteral, false, true, false),
      stringLiteral -> RegexTreeGraph.transform(stringLiteral, true, true, false)
    );

    List<String> transformSuffixes = Arrays.asList(
      ".pattern.png",
      ".tree.png",
      ".state.png",
      ".tree-state.png"
    );

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDirectory, "*.txt")) {
      for (Path txtFile : stream) {
        String fileName = txtFile.getFileName().toString().replaceFirst("\\.txt$", "");
        System.out.println(" - " + txtFile);
        String stringLiteral = FileUtils.readQuotedStringLiteral(txtFile);

        for (int i = 0; i < transforms.size(); i++) {
          Path pngFile = destDirectory.resolve(fileName + transformSuffixes.get(i));
          System.out.println("      " + pngFile);
          String dot = transforms.get(i).apply(stringLiteral);
          Files.write(pngFile, Dot.generate(dot, Dot.Type.PNG));
        }
      }
    }
  }

}
