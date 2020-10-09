package com.auzeill.regex.graph;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import static com.auzeill.regex.graph.PatternGraph.transform;
import static org.assertj.core.api.Assertions.assertThat;

class PatternGraphTest {

  @Test
  void empty() {
    assertThat(transform(literal(""))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: false\\l  .normalizedPattern: null\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 0\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 0\\l\\}\\l\" ]\n" +
      "  3[ label=\"LastNode:3\\{\\l\\}\\l\" ]\n" +
      "  4[ label=\"Node:4\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void one_letter() {
    assertThat(transform(literal("a"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"a\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 1\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 1\\l\\}\\l\" ]\n" +
      "  3[ label=\"BmpCharProperty:3\\{\\l  .predicate:\\l    $Lambda:6\\{\\l      .arg$1: 'a'\\l    \\}\\l\\}\\l\" ]\n" +
      "  4[ label=\"LastNode:4\\{\\l\\}\\l\" ]\n" +
      "  5[ label=\"Node:5\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void two_letters() {
    assertThat(transform(literal("ab"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"ab\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"ab\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 2\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 2\\l\\}\\l\" ]\n" +
      "  3[ label=\"Slice:3\\{\\l  .buffer: :6[ 'a', 'b' ]\\l\\}\\l\" ]\n" +
      "  4[ label=\"LastNode:4\\{\\l\\}\\l\" ]\n" +
      "  5[ label=\"Node:5\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void plus_qualifier() {
    assertThat(transform(literal("a+abc"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"a+abc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a+abc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 5\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 4\\l\\}\\l\" ]\n" +
      "  3[ label=\"BmpCharPropertyGreedy:3\\{\\l  .predicate:\\l    $Lambda:8\\{\\l      .arg$1: 'a'\\l    \\}\\l  .cmin: 1\\l\\}\\l\" ]\n" +
      "  4[ label=\"Slice:4\\{\\l  .buffer: :7[ 'a', 'b', 'c' ]\\l\\}\\l\" ]\n" +
      "  5[ label=\"LastNode:5\\{\\l\\}\\l\" ]\n" +
      "  6[ label=\"Node:6\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  5 -> 6 [ taillabel=\"next\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_bad() {
    assertThat(transform(literal("a++abc"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"a++abc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a++abc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 6\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 4\\l\\}\\l\" ]\n" +
      "  3[ label=\"Curly:3\\{\\l  .type: POSSESSIVE\\l  .cmin: 1\\l  .cmax: 2147483647\\l\\}\\l\" ]\n" +
      "  4[ label=\"Slice:4\\{\\l  .buffer: :7[ 'a', 'b', 'c' ]\\l\\}\\l\" ]\n" +
      "  5[ label=\"LastNode:5\\{\\l\\}\\l\" ]\n" +
      "  6[ label=\"Node:6\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "  8[ label=\"BmpCharProperty:8\\{\\l  .predicate:\\l    $Lambda:9\\{\\l      .arg$1: 'a'\\l    \\}\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  5 -> 6 [ taillabel=\"next\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  8 -> 6 [ taillabel=\"next\" ]\n" +
      "  3 -> 8 [ taillabel=\"atom\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_good() {
    assertThat(transform(literal("aa++bc"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"aa++bc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"aa++bc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 6\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 4\\l\\}\\l\" ]\n" +
      "  3[ label=\"BmpCharProperty:3\\{\\l  .predicate:\\l    $Lambda:11\\{\\l      .arg$1: 'a'\\l    \\}\\l\\}\\l\" ]\n" +
      "  4[ label=\"Curly:4\\{\\l  .type: POSSESSIVE\\l  .cmin: 1\\l  .cmax: 2147483647\\l\\}\\l\" ]\n" +
      "  5[ label=\"Slice:5\\{\\l  .buffer: :8[ 'b', 'c' ]\\l\\}\\l\" ]\n" +
      "  6[ label=\"LastNode:6\\{\\l\\}\\l\" ]\n" +
      "  7[ label=\"Node:7\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "  9[ label=\"BmpCharProperty:9\\{\\l  .predicate:\\l    $Lambda:10\\{\\l      .arg$1: 'a'\\l    \\}\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  6 -> 7 [ taillabel=\"next\" ]\n" +
      "  5 -> 6 [ taillabel=\"next\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  9 -> 7 [ taillabel=\"next\" ]\n" +
      "  4 -> 9 [ taillabel=\"atom\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_bad() {
    assertThat(transform(literal("\\d*+[02468]"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"\\\\\\\\d*+[02468]\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"\\\\\\\\d*+[02468]\\\"\\l  .buffer: null\\l  .predicate: \\{9\\}\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 11\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 1\\l\\}\\l\" ]\n" +
      "  3[ label=\"Curly:3\\{\\l  .type: POSSESSIVE\\l  .cmin: 0\\l  .cmax: 2147483647\\l\\}\\l\" ]\n" +
      "  4[ label=\"BmpCharProperty:4\\{\\l  .predicate:\\l    $Lambda:7\\{\\l      .arg$1: BitClass 0 2 4 6 8 \\l    \\}\\l\\}\\l\" ]\n" +
      "  5[ label=\"LastNode:5\\{\\l\\}\\l\" ]\n" +
      "  6[ label=\"Node:6\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "  8[ label=\"BmpCharProperty:8\\{\\l  .predicate: CharPredicates$$Lambda:9\\{ \\}\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  5 -> 6 [ taillabel=\"next\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  8 -> 6 [ taillabel=\"next\" ]\n" +
      "  3 -> 8 [ taillabel=\"atom\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_good() {
    assertThat(transform(literal("\\d*+(?<=[02468])"))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "\n" +
      "  // default nodes\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  1[ label=\"Pattern:1\\{\\l  .pattern: \\\"\\\\\\\\d*+(?<=[02468])\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"\\\\\\\\d*+(?<=[02468])\\\"\\l  .buffer: null\\l  .predicate: \\{13\\}\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 1\\l  .cursor: 16\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\" ]\n" +
      "  2[ label=\"Start:2\\{\\l  .minLength: 0\\l\\}\\l\" ]\n" +
      "  3[ label=\"Curly:3\\{\\l  .type: POSSESSIVE\\l  .cmin: 0\\l  .cmax: 2147483647\\l\\}\\l\" ]\n" +
      "  4[ label=\"Behind:4\\{\\l  .rmax: 1\\l  .rmin: 1\\l\\}\\l\" ]\n" +
      "  5[ label=\"LastNode:5\\{\\l\\}\\l\" ]\n" +
      "  6[ label=\"Node:6\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "  7[ label=\"GroupHead:7\\{\\l  .localIndex: 0\\l\\}\\l\" ]\n" +
      "  8[ label=\"BmpCharProperty:8\\{\\l  .predicate:\\l    $Lambda:11\\{\\l      .arg$1: BitClass 0 2 4 6 8 \\l    \\}\\l\\}\\l\" ]\n" +
      "  9[ label=\"GroupTail:9\\{\\l  .localIndex: 0\\l  .groupIndex: 0\\l\\}\\l\" ]\n" +
      "  10[ label=\"1:10\\{\\l  .next: null\\l\\}\\l\" ]\n" +
      "  12[ label=\"BmpCharProperty:12\\{\\l  .predicate: CharPredicates$$Lambda:13\\{ \\}\\l\\}\\l\" ]\n" +
      "\n" +
      "  // default edges\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  5 -> 6 [ taillabel=\"next\" ]\n" +
      "  4 -> 5 [ taillabel=\"next\" ]\n" +
      "  9 -> 10 [ taillabel=\"next\" ]\n" +
      "  8 -> 9 [ taillabel=\"next\" ]\n" +
      "  7 -> 8 [ taillabel=\"next\" ]\n" +
      "  7 -> 9 [ taillabel=\"tail\" ]\n" +
      "  4 -> 7 [ taillabel=\"cond\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  12 -> 6 [ taillabel=\"next\" ]\n" +
      "  3 -> 12 [ taillabel=\"atom\" ]\n" +
      "  2 -> 3 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"root\" ]\n" +
      "  1 -> 3 [ taillabel=\"matchRoot\" ]\n" +
      "}\n");
  }

  private static String literal(String value) {
    return "\"" + StringEscapeUtils.escapeJava(value) + "\"";
  }

}
