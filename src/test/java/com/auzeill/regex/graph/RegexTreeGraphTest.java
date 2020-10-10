package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegexTreeGraphTest extends AbstractTestOnTextFiles {

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testTreesAndStates(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, true, true);
    Path dotFile = associatedFile(txtFile, ".TreesAndStates.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testTrees(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, true, false);
    Path dotFile = associatedFile(txtFile, ".Trees.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void testStates(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = RegexTreeGraph.transform(literal, false, true);
    Path dotFile = associatedFile(txtFile, ".States.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

}
