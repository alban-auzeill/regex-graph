package com.auzeill.regex.graph;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class DotTest {

  @Test
  void execute_dot() throws IOException, InterruptedException {
    byte[] svg = Dot.generateSVG("" +
      "digraph D {\n" +
      "  A -> B\n" +
      "}");
    assertThat(new String(svg, UTF_8)).contains("<svg").contains("</svg>");
  }

}
