package com.auzeill.regex.graph;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import static com.auzeill.regex.graph.RegexTreeGraph.transform;
import static org.assertj.core.api.Assertions.assertThat;

class RegexTreeGraphTest {

  @Test
  void one_letter() {
    assertThat(transform(literal("a"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // tree-and-state nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"box\", style=\"rounded,filled\", color=\"Blue\", fillcolor=\"Beige\"]\n" +
      "  1[ label=\"PlainCharacterTree:1\\{\\l  .range: \\{0, 1, \\\"a\\\"\\}\\l  .contents: \\{'a'\\}\\l\\}\\l\" ]\n" +
      "\n" +
      "  // state nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"box\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"LightSkyBlue\"]\n" +
      "  EndOfRegex[ label=\"EndOfRegex\\l  continuation: null\\l\" ]\n" +
      "\n" +
      "  // end nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"doublecircle\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"DodgerBlue\" fixedsize=true, width=\"0.12\"]\n" +
      "  end[ label=\"\\l\" ]\n" +
      "\n" +
      "  // start nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"circle\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"DodgerBlue\" fixedsize=true, width=\"0.20\"]\n" +
      "  start[ label=\"\\l\" ]\n" +
      "\n" +
      "  // successor edges\n" +
      "  edge [style=\"bold\", color=\"DodgerBlue\", fontcolor=\"DodgerBlue\", arrowhead=\"vee\", arrowtail=\"none\", dir=\"both\"]\n" +
      "  1 -> EndOfRegex [ taillabel=\"\" ]\n" +
      "  EndOfRegex -> end [ taillabel=\"\" ]\n" +
      "  start -> 1 [ taillabel=\"\" ]\n" +
      "\n" +
      "  // continuation edges\n" +
      "  edge [style=\"dashed\", color=\"DodgerBlue\", fontcolor=\"DodgerBlue\", arrowhead=\"vee\", arrowtail=\"none\", dir=\"both\"]\n" +
      "  1 -> EndOfRegex [ taillabel=\"\" ]\n" +
      "}\n");
  }

  @Test
  void regex_error() {
    assertThat(transform(literal("a("))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"box\", style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\"]\n" +
      "  1[ label=\"SequenceTree:1\\{\\l  .range: \\{0, 2, \\\"a(\\\"\\}\\l\\}\\l\" ]\n" +
      "  3[ label=\"PlainCharacterTree:3\\{\\l  .range: \\{0, 1, \\\"a\\\"\\}\\l  .contents: \\{'a'\\}\\l\\}\\l\" ]\n" +
      "  4[ label=\"CapturingGroupTree:4\\{\\l  .range: \\{1, 2, \\\"(\\\"\\}\\l  .kind: CAPTURING_GROUP\\l  .groupHeader: \\{1, 2, \\\"(\\\"\\}\\l  .name: null\\l  .groupNumber: 1\\l\\}\\l\" ]\n" +
      "  5[ label=\"SequenceTree:5\\{\\l  .range: \\{2, 2, \\\"\\\"\\}\\l  .items: :6[ ]\\l\\}\\l\" ]\n" +
      "\n" +
      "  // error nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"box\", style=\"rounded,filled\", color=\"Red\", fillcolor=\"Orange\"]\n" +
      "  err1[ label=\"SyntaxError:\\lExpected ')', but found the end of the regex\\l\" ]\n" +
      "\n" +
      "  // state nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"box\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"LightSkyBlue\"]\n" +
      "  EndOfRegex[ label=\"EndOfRegex\\l  continuation: null\\l\" ]\n" +
      "\n" +
      "  // end nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"doublecircle\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"DodgerBlue\" fixedsize=true, width=\"0.12\"]\n" +
      "  end[ label=\"\\l\" ]\n" +
      "\n" +
      "  // start nodes\n" +
      "  node [fontname=\"Monospace\", fontsize= \"10\", shape=\"circle\", style=\"rounded,filled\", color=\"DodgerBlue\", fillcolor=\"DodgerBlue\" fixedsize=true, width=\"0.20\"]\n" +
      "  start[ label=\"\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [style=\"solid\", color=\"SlateGray\", fontcolor=\"SlateGray\", arrowhead=\"vee\", arrowtail=\"none\", dir=\"both\"]\n" +
      "  1 -> 3 [ taillabel=\"items[0]\" ]\n" +
      "  4 -> 5 [ taillabel=\"element\" ]\n" +
      "  1 -> 4 [ taillabel=\"items[1]\" ]\n" +
      "\n" +
      "  // successor edges\n" +
      "  edge [style=\"bold\", color=\"DodgerBlue\", fontcolor=\"DodgerBlue\", arrowhead=\"vee\", arrowtail=\"none\", dir=\"both\"]\n" +
      "  1 -> EndOfRegex [ taillabel=\"\" ]\n" +
      "  EndOfRegex -> end [ taillabel=\"\" ]\n" +
      "  start -> 1 [ taillabel=\"\" ]\n" +
      "\n" +
      "  // continuation edges\n" +
      "  edge [style=\"dashed\", color=\"DodgerBlue\", fontcolor=\"DodgerBlue\", arrowhead=\"vee\", arrowtail=\"none\", dir=\"both\"]\n" +
      "  1 -> EndOfRegex [ taillabel=\"\" ]\n" +
      "}\n");
  }

  private static String literal(String value) {
    return "\"" + StringEscapeUtils.escapeJava(value) + "\"";
  }

}
