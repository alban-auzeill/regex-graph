package com.auzeill.regex.graph;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphWriterTest {

  GraphWriter writer = new GraphWriter() {
    @Override
    String writeClassName(Object object) {
      return super.writeClassName(object).toString()
        .replaceFirst("^com\\.auzeill\\.regex\\.graph\\.([^.]+)$", "$1");
    }

    @Override
    boolean isNode(Object object) {
      return object instanceof C || super.isNode(object);
    }
  };

  @Test
  void class_name() {
    assertThat(writer.writeClassName("abc")).isEqualTo("String");
    assertThat(writer.writeClassName(new HashSet<>())).isEqualTo("HashSet");
    assertThat(writer.writeClassName(new java.sql.Date(0))).isEqualTo("java.sql.Date");
  }

  @Test
  void is_single_line() {
    assertThat(writer.isSingleLine("")).isTrue();
    assertThat(writer.isSingleLine("abc")).isTrue();
    assertThat(writer.isSingleLine("\n")).isFalse();
    assertThat(writer.isSingleLine("a\nb\nc\n")).isFalse();
    assertThat(writer.isSingleLine("a\nb\nc")).isFalse();
  }

  @Test
  void write_null() {
    assertThat(writer.write(null)).isEqualTo("null");
  }

  @Test
  void write_primitive() {
    assertThat(writer.write(true)).hasToString("true");
    assertThat(writer.write(false)).hasToString("false");
    assertThat(writer.write((byte) 2)).hasToString("2");
    assertThat(writer.write((short) 3)).hasToString("3");
    assertThat(writer.write(4)).hasToString("4");
    assertThat(writer.write(5L)).hasToString("5");
    assertThat(writer.write(6f)).hasToString("6.0");
    assertThat(writer.write(7d)).hasToString("7.0");

    assertThat(writer.write('a')).hasToString("'a'");
    assertThat(writer.write('\n')).hasToString("'\\n'");
    assertThat(writer.write('\'')).hasToString("'\\''");
    assertThat(writer.write('"')).hasToString("'\"'");
  }

  @Test
  void write_enum() {
    assertThat(writer.write(DayOfWeek.MONDAY)).hasToString("MONDAY");
  }

  @Test
  void write_string() {
    assertThat(writer.write("")).hasToString("\"\"");
    assertThat(writer.write("abc")).hasToString("\"abc\"");
    assertThat(writer.write("a\n b\r c\t d\f e\b f\0 g\\ h\" i'"))
      .hasToString("\"a\\n b\\r c\\t d\\f e\\b f\\0 g\\\\ h\\\" i'\"");

    assertThat(writer.write("12345678901234567890123456789012345678"))
      .hasToString("\"12345678901234567890123456789012345678\"");
    assertThat(writer.write("123456789012345678901234567890123456789"))
      .hasToString("\"1234567890123456789012345678901234567…\"");
  }

  @Test
  void write_array() {
    assertThat(writer.writeArray(new int[] {}, "1")).hasToString(":1[ ]");
    assertThat(writer.writeArray(new int[] {1}, "1")).hasToString(":1[ 1 ]");
    assertThat(writer.writeArray(new int[] {1, 2, 3}, "1")).hasToString(":1[ 1, 2, 3 ]");
    assertThat(writer.writeArray(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, "1"))
      .hasToString(":1[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]");
    assertThat(writer.writeArray(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, "1"))
      .hasToString(":1[ 1, 2, 3, 4, 5, 6, 7, 8, 9, … ]");
    assertThat(writer.writeArray(new String[] {"____________ long value ____________"}, "1"))
      .hasToString(":1[\n" +
        "  \"____________ long value ____________\"\n" +
        "]\n");
    assertThat(writer.writeArray(new int[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110}, "1"))
      .hasToString(":1[\n" +
        "  10,\n" +
        "  20,\n" +
        "  30,\n" +
        "  40,\n" +
        "  50,\n" +
        "  60,\n" +
        "  70,\n" +
        "  80,\n" +
        "  90,\n" +
        "  …\n" +
        "]\n");
  }

  @Test
  void write_sort_and_long_array() {
    assertThat(writer.write(new String[] {"a", "b", "c"})).hasToString(":1[ \"a\", \"b\", \"c\" ]");
    assertThat(writer.write(new String[] {"very very very very very", "long", "names"}))
      .hasToString(":2[\n" +
        "  \"very very very very very\",\n" +
        "  \"long\",\n" +
        "  \"names\"\n" +
        "]\n");
  }

  static class A {
    static int s;
    Object a;
    int b;

    public A(Object a, int b) {
      this.a = a;
      this.b = b;
    }
  }
  static class B extends A {
    Object c;

    public B(Object a, int b, Object c) {
      super(a, b);
      this.c = c;
    }
  }
  static class C {
    Object previous;
    Object next;
  }

  @Test
  void indent() {
    assertThat(writer.indent("abc")).hasToString("  abc\n");
    assertThat(writer.indent("abc", ";")).hasToString("  abc;\n");
    assertThat(writer.indent("abc\n")).hasToString("  abc\n");
    assertThat(writer.indent("abc\n", ";")).hasToString("  abc;\n");
    assertThat(writer.indent("a\nb\nc\n")).hasToString("  a\n  b\n  c\n");
    assertThat(writer.indent("a\nb\nc\n", ";")).hasToString("  a\n  b\n  c;\n");
  }

  @Test
  void non_static_fields() {
    assertThat(writer.nonStaticFields(Object.class)).isEmpty();
    assertThat(writer.nonStaticFields(A.class)).hasSize(2);
    assertThat(writer.nonStaticFields(B.class)).hasSize(3);
  }

  @Test
  void write_object_field() throws NoSuchFieldException {
    Field valueField = A.class.getDeclaredField("a");
    assertThat(writer.writeObjectField(new B("abc".toCharArray(), 0, null), valueField)).hasToString(":1[ 'a', 'b', 'c' ]");
    assertThat(writer.writeObjectNamedField(new B("abc".toCharArray(), 0, null), valueField)).hasToString("" +
      ".a: :2[ 'a', 'b', 'c' ]\n");
    assertThat(writer.writeObjectNamedField(new B("long name".toCharArray(), 0, null), valueField))
      .hasToString("" +
        ".a:\n" +
        "  :3[\n" +
        "    'l',\n" +
        "    'o',\n" +
        "    'n',\n" +
        "    'g',\n" +
        "    ' ',\n" +
        "    'n',\n" +
        "    'a',\n" +
        "    'm',\n" +
        "    'e'\n" +
        "  ]\n");
  }

  @Test
  void write_int_field() throws NoSuchFieldException {
    Field valueField = A.class.getDeclaredField("b");
    assertThat(writer.writeObjectField(new B(null, 42, null), valueField)).hasToString("42");
    assertThat(writer.writeObjectNamedField(new B(null, 42, null), valueField)).hasToString(".b: 42\n");
  }

  @Test
  void write_empty_object() {
    assertThat(writer.write(new Object())).hasToString("Object:1{ }");
  }

  @Test
  void write_empty_child_object() {
    assertThat(writer.write(new B("abc".toCharArray(), 0, new Object()))).hasToString("" +
      "GraphWriterTest$B:1{\n" +
      "  .a: :2[ 'a', 'b', 'c' ]\n" +
      "  .b: 0\n" +
      "  .c: Object:3{ }\n" +
      "}\n");
  }

  @Test
  void write_nested_objects() {
    assertThat(writer.write(new B("abc", 0, new A(32, -2)))).hasToString("" +
      "GraphWriterTest$B:1{\n" +
      "  .a: \"abc\"\n" +
      "  .b: 0\n" +
      "  .c:\n" +
      "    GraphWriterTest$A:2{\n" +
      "      .a: 32\n" +
      "      .b: -2\n" +
      "    }\n" +
      "}\n");
  }

  @Test
  void write_object_reference() {
    A a = new A(32, -2);
    assertThat(writer.write(new B(a, 0, a))).hasToString("" +
      "GraphWriterTest$B:1{\n" +
      "  .a:\n" +
      "    GraphWriterTest$A:2{\n" +
      "      .a: 32\n" +
      "      .b: -2\n" +
      "    }\n" +
      "  .b: 0\n" +
      "  .c: {2}\n" +
      "}\n");
  }

  @Test
  void graph_null() {
    assertThat(writer.graph(null)).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "}\n");
  }

  @Test
  void graph_one_node() {
    A a = new A(32, -2);
    assertThat(writer.graph(new B(a, 0, a))).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  1[ label=\"GraphWriterTest$B:1\\{\\l  .a:\\l    GraphWriterTest$A:2\\{\\l      .a: 32\\l      .b: -2\\l    \\}\\l  .b: 0\\l  .c: \\{2\\}\\l\\}\\l\" ]\n" +
      "}\n");
  }

  @Test
  void graph_several_nodes() {
    C o2 = new C();
    C o3 = new C();
    C o4 = new C();
    o2.next = o3;
    o2.previous = o4;
    o3.next = o3;
    o3.previous = o2;
    o4.next = o3;
    o4.previous = o2;
    B o1 = new B(o2, 0, o2);
    assertThat(writer.graph(o1)).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  1[ label=\"GraphWriterTest$B:1\\{\\l  .b: 0\\l\\}\\l\" ]\n" +
      "  2[ label=\"GraphWriterTest$C:2\\{\\l\\}\\l\" ]\n" +
      "  3[ label=\"GraphWriterTest$C:3\\{\\l\\}\\l\" ]\n" +
      "  4[ label=\"GraphWriterTest$C:4\\{\\l\\}\\l\" ]\n" +
      "  3 -> 2 [ taillabel=\"previous\" ]\n" +
      "  4 -> 2 [ taillabel=\"previous\" ]\n" +
      "  4 -> 4 [ taillabel=\"next\" ]\n" +
      "  3 -> 4 [ taillabel=\"next\" ]\n" +
      "  2 -> 3 [ taillabel=\"previous\" ]\n" +
      "  2 -> 4 [ taillabel=\"next\" ]\n" +
      "  1 -> 2 [ taillabel=\"a\" ]\n" +
      "  1 -> 2 [ taillabel=\"c\" ]\n" +
      "}\n");
  }

  @Test
  void graph_array_list() {
    C o3 = new C();
    o3.previous = null;
    o3.next = "next of o3";

    C o4 = new C();
    o4.previous = null;
    o4.next = "next of o4";

    List<Object> o2 = new ArrayList<>();
    o2.add(o3);
    o2.add(o4);

    C o1 = new C();
    o1.previous = o2;
    o1.next = o3;

    assertThat(writer.graph(o1)).isEqualTo("" +
      "digraph G {\n" +
      "  rankdir=LR;\n" +
      "  graph [ fontname=\"Monospace\", fontsize=10 ]\n" +
      "  node [ fontname=\"Monospace\", fontsize=10, shape=box, style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\" ]\n" +
      "  edge [ fontname=\"Monospace\", fontsize=10, color=\"Navy\" ]\n" +
      "  1[ label=\"GraphWriterTest$C:1\\{\\l\\}\\l\" ]\n" +
      "  3[ label=\"GraphWriterTest$C:3\\{\\l  .previous: null\\l  .next: \\\"next of o3\\\"\\l\\}\\l\" ]\n" +
      "  4[ label=\"GraphWriterTest$C:4\\{\\l  .previous: null\\l  .next: \\\"next of o4\\\"\\l\\}\\l\" ]\n" +
      "  1 -> 3 [ taillabel=\"previous[0]\" ]\n" +
      "  1 -> 4 [ taillabel=\"previous[1]\" ]\n" +
      "  1 -> 3 [ taillabel=\"next\" ]\n" +
      "}\n");
  }
}
