package com.auzeill.regex.graph;

import org.junit.jupiter.api.Test;

import static com.auzeill.regex.graph.RegExGraph.transform;
import static org.assertj.core.api.Assertions.assertThat;

class RegExGraphTest {

  @Test
  void inner_class() {
    class A {
      int a = 2;

      class B {
        int b = 3;
      }
    }
    assertThat(transform(new A())).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"com.auzeill.regex.graph.RegExGraphTest$1A obj_1 \\{\\l  .a: 2\\l  .this$0:\\l    com.auzeill.regex.graph.RegExGraphTest obj_2 \\{\\l    \\}\\l\\}\\l\"]\n" +
      "}\n");
  }

  @Test
  void empty() {
    assertThat(transform("")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: false\\l  .normalizedPattern: null\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 0\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n" +
      "  obj_3 [label=\"LastNode obj_3 \\{\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"Node obj_4 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 0\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void one_letter() {
    assertThat(transform("a")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"a\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 1\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n" +
      "  obj_3 [label=\"BmpCharProperty obj_3 \\{\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"$Lambda obj_5 \\{\\l  .arg$1: 97\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"LastNode obj_4 \\{\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"Node obj_6 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 1\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"predicate\"]\n" +
      "  obj_4  -> obj_6 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void two_letters() {
    assertThat(transform("ab")).isEqualTo("digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"ab\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"ab\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 2\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n"
      +
      "  obj_3 [label=\"Slice obj_3 \\{\\l  .buffer: [97, 98]\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"LastNode obj_4 \\{\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"Node obj_5 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 2\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_4  -> obj_5 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void plus_qualifier() {
    assertThat(transform("a+abc")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"a+abc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a+abc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 5\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n" +
      "  obj_3 [label=\"BmpCharPropertyGreedy obj_3 \\{\\l  .cmin: 1\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"$Lambda obj_5 \\{\\l  .arg$1: 97\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"Slice obj_4 \\{\\l  .buffer: [97, 98, 99]\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"LastNode obj_6 \\{\\l\\}\\l\"]\n" +
      "  obj_7 [label=\"Node obj_7 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 4\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"predicate\"]\n" +
      "  obj_4  -> obj_6 [taillabel = \"next\"]\n" +
      "  obj_6  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_bad() {
    assertThat(transform("a++abc")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"a++abc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"a++abc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 6\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n"
      +
      "  obj_3 [label=\"Curly obj_3 \\{\\l  .type: POSSESSIVE\\l  .cmin: 1\\l  .cmax: 2147483647\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"BmpCharProperty obj_5 \\{\\l\\}\\l\"]\n" +
      "  obj_7 [label=\"$Lambda obj_7 \\{\\l  .arg$1: 97\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"Node obj_6 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"Slice obj_4 \\{\\l  .buffer: [97, 98, 99]\\l\\}\\l\"]\n" +
      "  obj_8 [label=\"LastNode obj_8 \\{\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 4\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"atom\"]\n" +
      "  obj_5  -> obj_6 [taillabel = \"next\"]\n" +
      "  obj_5  -> obj_7 [taillabel = \"predicate\"]\n" +
      "  obj_4  -> obj_8 [taillabel = \"next\"]\n" +
      "  obj_8  -> obj_6 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_good() {
    assertThat(transform("aa++bc")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"aa++bc\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"aa++bc\\\"\\l  .buffer: null\\l  .predicate: null\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 6\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n" +
      "  obj_3 [label=\"BmpCharProperty obj_3 \\{\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"$Lambda obj_5 \\{\\l  .arg$1: 97\\l\\}\\l\"]\n" +
      "  obj_4 [label=\"Curly obj_4 \\{\\l  .type: POSSESSIVE\\l  .cmin: 1\\l  .cmax: 2147483647\\l\\}\\l\"]\n" +
      "  obj_7 [label=\"BmpCharProperty obj_7 \\{\\l\\}\\l\"]\n" +
      "  obj_9 [label=\"$Lambda obj_9 \\{\\l  .arg$1: 97\\l\\}\\l\"]\n" +
      "  obj_8 [label=\"Node obj_8 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"Slice obj_6 \\{\\l  .buffer: [98, 99]\\l\\}\\l\"]\n" +
      "  obj_10 [label=\"LastNode obj_10 \\{\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 4\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_4 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"predicate\"]\n" +
      "  obj_4  -> obj_6 [taillabel = \"next\"]\n" +
      "  obj_4  -> obj_7 [taillabel = \"atom\"]\n" +
      "  obj_7  -> obj_8 [taillabel = \"next\"]\n" +
      "  obj_7  -> obj_9 [taillabel = \"predicate\"]\n" +
      "  obj_6  -> obj_10 [taillabel = \"next\"]\n" +
      "  obj_10  -> obj_8 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_bad() {
    assertThat(transform("\\d*+[02468]")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"\\\\d*+[02468]\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"\\\\d*+[02468]\\\"\\l  .buffer: null\\l  .predicate:\\l    java.util.regex.CharPredicates$$Lambda obj_4 \\{\\l    \\}\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 0\\l  .cursor: 11\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n"
      +
      "  obj_3 [label=\"Curly obj_3 \\{\\l  .type: POSSESSIVE\\l  .cmin: 0\\l  .cmax: 2147483647\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"BmpCharProperty obj_6 \\{\\l  .predicate:\\l    ref obj_4\\l\\}\\l\"]\n" +
      "  obj_7 [label=\"Node obj_7 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"BmpCharProperty obj_5 \\{\\l\\}\\l\"]\n" +
      "  obj_9 [label=\"$Lambda obj_9 \\{\\l\\}\\l\"]\n" +
      "  obj_10 [label=\"BitClass obj_10 \\{\\l  .bits: [false, false, false, ...]\\l\\}\\l\"]\n" +
      "  obj_11 [label=\"BitClass$$Lambda obj_11 \\{\\l  .arg$1: [false, false, false, ...]\\l\\}\\l\"]\n" +
      "  obj_8 [label=\"LastNode obj_8 \\{\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 1\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_6 [taillabel = \"atom\"]\n" +
      "  obj_6  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_5  -> obj_8 [taillabel = \"next\"]\n" +
      "  obj_5  -> obj_9 [taillabel = \"predicate\"]\n" +
      "  obj_9  -> obj_10 [taillabel = \"arg$1\"]\n" +
      "  obj_10  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_10  -> obj_11 [taillabel = \"predicate\"]\n" +
      "  obj_8  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_good() {
    assertThat(transform("\\d*+(?<=[02468])")).isEqualTo("" +
      "digraph G {\n" +
      "  obj_1 [label=\"java.util.regex.Pattern obj_1 \\{\\l  .pattern: \\\"\\\\d*+(?<=[02468])\\\"\\l  .flags: 0\\l  .flags0: 0\\l  .compiled: true\\l  .normalizedPattern: \\\"\\\\d*+(?<=[02468])\\\"\\l  .buffer: null\\l  .predicate:\\l    java.util.regex.CharPredicates$$Lambda obj_4 \\{\\l    \\}\\l  .namedGroups: null\\l  .groupNodes: null\\l  .topClosureNodes: null\\l  .localTCNCount: 0\\l  .hasGroupRef: false\\l  .temp: null\\l  .capturingGroupCount: 1\\l  .localCount: 1\\l  .cursor: 16\\l  .patternLength: 0\\l  .hasSupplementary: false\\l\\}\\l\"]\n" +
      "  obj_3 [label=\"Curly obj_3 \\{\\l  .type: POSSESSIVE\\l  .cmin: 0\\l  .cmax: 2147483647\\l\\}\\l\"]\n" +
      "  obj_6 [label=\"BmpCharProperty obj_6 \\{\\l  .predicate:\\l    ref obj_4\\l\\}\\l\"]\n" +
      "  obj_7 [label=\"Node obj_7 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_5 [label=\"Behind obj_5 \\{\\l  .rmax: 1\\l  .rmin: 1\\l\\}\\l\"]\n" +
      "  obj_9 [label=\"GroupHead obj_9 \\{\\l  .localIndex: 0\\l\\}\\l\"]\n" +
      "  obj_11 [label=\"GroupTail obj_11 \\{\\l  .localIndex: 0\\l  .groupIndex: 0\\l\\}\\l\"]\n" +
      "  obj_12 [label=\"1 obj_12 \\{\\l  .next: null\\l\\}\\l\"]\n" +
      "  obj_10 [label=\"BmpCharProperty obj_10 \\{\\l\\}\\l\"]\n" +
      "  obj_13 [label=\"$Lambda obj_13 \\{\\l\\}\\l\"]\n" +
      "  obj_14 [label=\"BitClass obj_14 \\{\\l  .bits: [false, false, false, ...]\\l\\}\\l\"]\n" +
      "  obj_15 [label=\"BitClass$$Lambda obj_15 \\{\\l  .arg$1: [false, false, false, ...]\\l\\}\\l\"]\n" +
      "  obj_8 [label=\"LastNode obj_8 \\{\\l\\}\\l\"]\n" +
      "  obj_2 [label=\"Start obj_2 \\{\\l  .minLength: 0\\l\\}\\l\"]\n" +
      "  obj_1  -> obj_2 [taillabel = \"root\"]\n" +
      "  obj_1  -> obj_3 [taillabel = \"matchRoot\"]\n" +
      "  obj_3  -> obj_5 [taillabel = \"next\"]\n" +
      "  obj_3  -> obj_6 [taillabel = \"atom\"]\n" +
      "  obj_6  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_5  -> obj_8 [taillabel = \"next\"]\n" +
      "  obj_5  -> obj_9 [taillabel = \"cond\"]\n" +
      "  obj_9  -> obj_10 [taillabel = \"next\"]\n" +
      "  obj_9  -> obj_11 [taillabel = \"tail\"]\n" +
      "  obj_11  -> obj_12 [taillabel = \"next\"]\n" +
      "  obj_10  -> obj_11 [taillabel = \"next\"]\n" +
      "  obj_10  -> obj_13 [taillabel = \"predicate\"]\n" +
      "  obj_13  -> obj_14 [taillabel = \"arg$1\"]\n" +
      "  obj_14  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_14  -> obj_15 [taillabel = \"predicate\"]\n" +
      "  obj_8  -> obj_7 [taillabel = \"next\"]\n" +
      "  obj_2  -> obj_3 [taillabel = \"next\"]\n" +
      "}\n");
  }

}
