package com.auzeill.regex.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.auzeill.regex.graph.PrintString.lines;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class GeneratePngTest {

  @Test
  void test_generate(@TempDir Path tempDir) throws IOException, InterruptedException {
    String imageList = lines(
      "",
      "# comment",
      "f1.png:P:\"a*\"",
      "f2.png:T:\"a+\"",
      "f3.png:A:\"a?\"",
      "f4.png:TA:\"ab\"",
      "f5.png:TA,10,10,15,15:\"ab\"",
      "line error");
    Path listPath = tempDir.resolve("list.txt");
    Files.write(listPath, imageList.getBytes(UTF_8));
    PrintString out = new PrintString();
    PrintString err = new PrintString();
    GeneratePng.generate(tempDir, listPath, out, err);
    assertThat(out).hasToString(lines(
      "[INFO] create png for \"a*\", format: P target: f1.png",
      "[INFO] create png for \"a+\", format: T target: f2.png",
      "[INFO] create png for \"a?\", format: A target: f3.png",
      "[INFO] create png for \"ab\", format: TA target: f4.png",
      "[INFO] create png for \"ab\", format: TA target: f5.png",
      "[INFO] crop f5.png"));
    assertThat(err).hasToString(lines(
      "[ERROR] Invalid format: line error"));
    assertThat(tempDir.resolve("f1.png")).exists();
    assertThat(tempDir.resolve("f2.png")).exists();
    assertThat(tempDir.resolve("f3.png")).exists();
    assertThat(tempDir.resolve("f4.png")).exists();
    assertThat(tempDir.resolve("f5.png")).exists();
    assertThat(Files.size(tempDir.resolve("f5.png"))).isLessThan(Files.size(tempDir.resolve("f4.png")));
  }

}
