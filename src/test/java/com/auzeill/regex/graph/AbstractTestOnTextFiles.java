package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.fail;

public abstract class AbstractTestOnTextFiles {

  static Stream<Arguments> regexTextFilesArguments() throws IOException {
    return textFilesArguments(Paths.get("src", "test", "resources", "regex"));
  }

  static Stream<Arguments> textFilesArguments(Path parentDirectory) throws IOException {
    List<Arguments> arguments = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDirectory, "*.txt")) {
      stream.forEach(path -> arguments.add(Arguments.of(path)));
    }
    if (arguments.isEmpty()) {
      throw new IllegalStateException("Unexpected directory without *.txt files: " + parentDirectory);
    }
    return arguments.stream();
  }

  static void autoFixExpectedFileIfNeeded(Path expectedFile, String content) throws IOException {
    if ("true".equals(System.getProperty("fixTest"))) {
      Files.writeString(expectedFile, content, UTF_8);
    }
    if (!expectedFile.getFileName().toString().contains("error")) {
      String lowerCaseContent = content.toLowerCase();
      if (lowerCaseContent.contains("error") || lowerCaseContent.contains("exception")) {
        fail("Unexpected error in file '" + expectedFile + "' content:\n\n" + content);
      }
    }
  }

  static Path associatedFile(Path txtFile, String suffix) {
    String dotFileName = txtFile.getFileName().toString() + suffix;
    return txtFile.getParent().resolve(dotFileName);
  }

}
