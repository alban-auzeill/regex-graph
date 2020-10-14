package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FileUtils {

  private FileUtils() {
    // utility class
  }

  static String readQuotedStringLiteral(Path txtFile) throws IOException {
    String literal = readFile(txtFile);
    literal = literal
      .replaceFirst("^\\s++", "")
      .replaceFirst("\\s++$", "");
    if (!literal.startsWith("\"") || !literal.endsWith("\"")) {
      throw new IllegalStateException("Missing double quote around: " + literal);
    }
    return literal;
  }

  static String readFile(Path file) throws IOException {
    return Files.readString(file, UTF_8);
  }

}
