package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.auzeill.regex.graph.FileUtils.readFile;
import static com.auzeill.regex.graph.FileUtils.readQuotedStringLiteral;
import static org.assertj.core.api.Assertions.assertThat;

class PatternGraphTest extends AbstractTestOnTextFiles {

  @ParameterizedTest
  @MethodSource("regexTextFilesArguments")
  void test(Path txtFile) throws IOException {
    String literal = readQuotedStringLiteral(txtFile);
    String actual = PatternGraph.transform(literal);
    Path dotFile = associatedFile(txtFile, ".Pattern.dot");
    autoFixExpectedFileIfNeeded(dotFile, actual);
    assertThat(actual).isEqualTo(readFile(dotFile));
  }

}
