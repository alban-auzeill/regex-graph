package com.auzeill.regex.graph;

import org.junit.jupiter.api.Test;

import static com.auzeill.regex.graph.RegExDump.transform;
import static org.assertj.core.api.Assertions.assertThat;

class RegExDumpTest {

  @Test
  void inner_class() {
    class A {
      int a = 2;
      class B {
        int b = 3;
      }
    }
    assertThat(transform(new A())).isEqualTo("" +
      "obj(1) com.auzeill.regex.graph.RegExDumpTest$1A {\n" +
      "  .a: 2\n" +
      "  .this$0:\n" +
      "    obj(2) com.auzeill.regex.graph.RegExDumpTest {\n" +
      "    }\n" +
      "}\n");
  }

  @Test
  void empty() {
    assertThat(transform("")).isEqualTo("" +
        "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: false\n" +
      "  .normalizedPattern: null\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$LastNode {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$Node {\n" +
      "              .next: null\n" +
      "            }\n" +
      "        }\n" +
      "      .minLength: 0\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 0\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void one_letter() {
    assertThat(transform("a")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"a\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"a\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$BmpCharProperty {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$LastNode {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$Node {\n" +
      "                  .next: null\n" +
      "                }\n" +
      "            }\n" +
      "          .predicate:\n" +
      "            obj(6) java.util.regex.Pattern$$Lambda {\n" +
      "              .arg$1: 97\n" +
      "            }\n" +
      "        }\n" +
      "      .minLength: 1\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 1\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void two_letters() {
    assertThat(transform("ab")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"ab\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"ab\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$Slice {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$LastNode {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$Node {\n" +
      "                  .next: null\n" +
      "                }\n" +
      "            }\n" +
      "          .buffer: [97, 98]\n" +
      "        }\n" +
      "      .minLength: 2\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 2\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void plus_qualifier() {
    assertThat(transform("a+abc")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"a+abc\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"a+abc\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$BmpCharPropertyGreedy {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$Slice {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$LastNode {\n" +
      "                  .next:\n" +
      "                    obj(6) java.util.regex.Pattern$Node {\n" +
      "                      .next: null\n" +
      "                    }\n" +
      "                }\n" +
      "              .buffer: [97, 98, 99]\n" +
      "            }\n" +
      "          .predicate:\n" +
      "            obj(7) java.util.regex.Pattern$$Lambda {\n" +
      "              .arg$1: 97\n" +
      "            }\n" +
      "          .cmin: 1\n" +
      "        }\n" +
      "      .minLength: 4\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 5\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_bad() {
    assertThat(transform("a++abc")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"a++abc\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"a++abc\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$Curly {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$Slice {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$LastNode {\n" +
      "                  .next:\n" +
      "                    obj(6) java.util.regex.Pattern$Node {\n" +
      "                      .next: null\n" +
      "                    }\n" +
      "                }\n" +
      "              .buffer: [97, 98, 99]\n" +
      "            }\n" +
      "          .atom:\n" +
      "            obj(7) java.util.regex.Pattern$BmpCharProperty {\n" +
      "              .next:\n" +
      "                ref(6)\n" +
      "              .predicate:\n" +
      "                obj(8) java.util.regex.Pattern$$Lambda {\n" +
      "                  .arg$1: 97\n" +
      "                }\n" +
      "            }\n" +
      "          .type:\n" +
      "            obj(9) java.util.regex.Pattern$Qtype {\n" +
      "              .name: \"POSSESSIVE\"\n" +
      "              .ordinal: 2\n" +
      "            }\n" +
      "          .cmin: 1\n" +
      "          .cmax: 2147483647\n" +
      "        }\n" +
      "      .minLength: 4\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 6\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_aabc_good() {
    assertThat(transform("aa++bc")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"aa++bc\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"aa++bc\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$BmpCharProperty {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$Curly {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$Slice {\n" +
      "                  .next:\n" +
      "                    obj(6) java.util.regex.Pattern$LastNode {\n" +
      "                      .next:\n" +
      "                        obj(7) java.util.regex.Pattern$Node {\n" +
      "                          .next: null\n" +
      "                        }\n" +
      "                    }\n" +
      "                  .buffer: [98, 99]\n" +
      "                }\n" +
      "              .atom:\n" +
      "                obj(8) java.util.regex.Pattern$BmpCharProperty {\n" +
      "                  .next:\n" +
      "                    ref(7)\n" +
      "                  .predicate:\n" +
      "                    obj(9) java.util.regex.Pattern$$Lambda {\n" +
      "                      .arg$1: 97\n" +
      "                    }\n" +
      "                }\n" +
      "              .type:\n" +
      "                obj(10) java.util.regex.Pattern$Qtype {\n" +
      "                  .name: \"POSSESSIVE\"\n" +
      "                  .ordinal: 2\n" +
      "                }\n" +
      "              .cmin: 1\n" +
      "              .cmax: 2147483647\n" +
      "            }\n" +
      "          .predicate:\n" +
      "            obj(11) java.util.regex.Pattern$$Lambda {\n" +
      "              .arg$1: 97\n" +
      "            }\n" +
      "        }\n" +
      "      .minLength: 4\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate: null\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 6\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_bad() {
    assertThat(transform("\\d*+[02468]")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"\\d*+[02468]\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"\\d*+[02468]\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$Curly {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$BmpCharProperty {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$LastNode {\n" +
      "                  .next:\n" +
      "                    obj(6) java.util.regex.Pattern$Node {\n" +
      "                      .next: null\n" +
      "                    }\n" +
      "                }\n" +
      "              .predicate:\n" +
      "                obj(7) java.util.regex.Pattern$$Lambda {\n" +
      "                  .arg$1:\n" +
      "                    obj(8) java.util.regex.Pattern$BitClass {\n" +
      "                      .next:\n" +
      "                        ref(6)\n" +
      "                      .predicate:\n" +
      "                        obj(9) java.util.regex.Pattern$BitClass$$Lambda {\n" +
      "                          .arg$1:\n" +
      "                            obj(10) [Z {\n" +
      "                            }\n" +
      "                        }\n" +
      "                      .bits:\n" +
      "                        ref(10)\n" +
      "                    }\n" +
      "                }\n" +
      "            }\n" +
      "          .atom:\n" +
      "            obj(11) java.util.regex.Pattern$BmpCharProperty {\n" +
      "              .next:\n" +
      "                ref(6)\n" +
      "              .predicate:\n" +
      "                obj(12) java.util.regex.CharPredicates$$Lambda {\n" +
      "                }\n" +
      "            }\n" +
      "          .type:\n" +
      "            obj(13) java.util.regex.Pattern$Qtype {\n" +
      "              .name: \"POSSESSIVE\"\n" +
      "              .ordinal: 2\n" +
      "            }\n" +
      "          .cmin: 0\n" +
      "          .cmax: 2147483647\n" +
      "        }\n" +
      "      .minLength: 1\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate:\n" +
      "    ref(12)\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 0\n" +
      "  .cursor: 11\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

  @Test
  void possessive_qualifier_even_number_good() {
    assertThat(transform("\\d*+(?<=[02468])")).isEqualTo("" +
      "obj(1) java.util.regex.Pattern {\n" +
      "  .pattern: \"\\d*+(?<=[02468])\"\n" +
      "  .flags: 0\n" +
      "  .flags0: 0\n" +
      "  .compiled: true\n" +
      "  .normalizedPattern: \"\\d*+(?<=[02468])\"\n" +
      "  .root:\n" +
      "    obj(2) java.util.regex.Pattern$Start {\n" +
      "      .next:\n" +
      "        obj(3) java.util.regex.Pattern$Curly {\n" +
      "          .next:\n" +
      "            obj(4) java.util.regex.Pattern$Behind {\n" +
      "              .next:\n" +
      "                obj(5) java.util.regex.Pattern$LastNode {\n" +
      "                  .next:\n" +
      "                    obj(6) java.util.regex.Pattern$Node {\n" +
      "                      .next: null\n" +
      "                    }\n" +
      "                }\n" +
      "              .cond:\n" +
      "                obj(7) java.util.regex.Pattern$GroupHead {\n" +
      "                  .next:\n" +
      "                    obj(8) java.util.regex.Pattern$BmpCharProperty {\n" +
      "                      .next:\n" +
      "                        obj(9) java.util.regex.Pattern$GroupTail {\n" +
      "                          .next:\n" +
      "                            obj(10) java.util.regex.Pattern$1 {\n" +
      "                              .next: null\n" +
      "                            }\n" +
      "                          .localIndex: 0\n" +
      "                          .groupIndex: 0\n" +
      "                        }\n" +
      "                      .predicate:\n" +
      "                        obj(11) java.util.regex.Pattern$$Lambda {\n" +
      "                          .arg$1:\n" +
      "                            obj(12) java.util.regex.Pattern$BitClass {\n" +
      "                              .next:\n" +
      "                                ref(6)\n" +
      "                              .predicate:\n" +
      "                                obj(13) java.util.regex.Pattern$BitClass$$Lambda {\n" +
      "                                  .arg$1:\n" +
      "                                    obj(14) [Z {\n" +
      "                                    }\n" +
      "                                }\n" +
      "                              .bits:\n" +
      "                                ref(14)\n" +
      "                            }\n" +
      "                        }\n" +
      "                    }\n" +
      "                  .localIndex: 0\n" +
      "                  .tail:\n" +
      "                    ref(9)\n" +
      "                }\n" +
      "              .rmax: 1\n" +
      "              .rmin: 1\n" +
      "            }\n" +
      "          .atom:\n" +
      "            obj(15) java.util.regex.Pattern$BmpCharProperty {\n" +
      "              .next:\n" +
      "                ref(6)\n" +
      "              .predicate:\n" +
      "                obj(16) java.util.regex.CharPredicates$$Lambda {\n" +
      "                }\n" +
      "            }\n" +
      "          .type:\n" +
      "            obj(17) java.util.regex.Pattern$Qtype {\n" +
      "              .name: \"POSSESSIVE\"\n" +
      "              .ordinal: 2\n" +
      "            }\n" +
      "          .cmin: 0\n" +
      "          .cmax: 2147483647\n" +
      "        }\n" +
      "      .minLength: 0\n" +
      "    }\n" +
      "  .matchRoot:\n" +
      "    ref(3)\n" +
      "  .buffer: null\n" +
      "  .predicate:\n" +
      "    ref(16)\n" +
      "  .namedGroups: null\n" +
      "  .groupNodes: null\n" +
      "  .topClosureNodes: null\n" +
      "  .localTCNCount: 0\n" +
      "  .hasGroupRef: false\n" +
      "  .temp: null\n" +
      "  .capturingGroupCount: 1\n" +
      "  .localCount: 1\n" +
      "  .cursor: 16\n" +
      "  .patternLength: 0\n" +
      "  .hasSupplementary: false\n" +
      "}\n");
  }

}
