package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.auzeill.regex.graph.FileUtils.readFile;
import static com.auzeill.regex.graph.FileUtils.readQuotedStringLiteral;
import static org.assertj.core.api.Assertions.assertThat;

class RegexTreeGraphTest extends AbstractTestOnTextFiles {

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testTreesAndStates(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, true, true, false);
    Path dotFile = associatedFile(txtFile, ".TreesAndStates.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testTrees(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, true, false, false);
    Path dotFile = associatedFile(txtFile, ".Trees.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testStates(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, false, true, false);
    Path dotFile = associatedFile(txtFile, ".States.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

  @Test
  void testLegend() throws IOException {
    String actual = RegexTreeGraph.transform("\"\"", true, true, true);
    Path dotFile = Paths.get("src", "test", "resources", "regex", "legend.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

}
