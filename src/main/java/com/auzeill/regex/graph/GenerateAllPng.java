package com.auzeill.regex.graph;

import com.auzeill.regex.graph.GeneratePng.Format;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateAllPng {

  public static void main(String[] args) throws IOException, InterruptedException {
    Path srcDirectory = Paths.get("src", "test", "resources", "regex");
    Path destDirectory = Paths.get("build", "tmp", "png");
    Files.createDirectories(destDirectory);
    GeneratePng.createPng(System.out, destDirectory.resolve("legend.png"), Format.L, "\"\"");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDirectory, "*.txt")) {
      for (Path txtFile : stream) {
        String fileName = txtFile.getFileName().toString().replaceFirst("\\.txt$", "");
        System.out.println("[INFO] read " + txtFile);
        String regex = FileUtils.readQuotedStringLiteral(txtFile);
        for (Format format : Format.valuesWithoutLegend()) {
          Path pngFile = destDirectory.resolve(fileName + format.fileSuffix);
          GeneratePng.createPng(System.out, pngFile, format, regex);
        }
      }
    }
  }

}
