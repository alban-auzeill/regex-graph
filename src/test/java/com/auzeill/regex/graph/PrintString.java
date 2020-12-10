package com.auzeill.regex.graph;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PrintString extends PrintStream {

  public static final String NL = System.lineSeparator();

  public PrintString() {
    super(new ByteArrayOutputStream(), true, UTF_8);
  }

  @Override
  public String toString() {
    return ((ByteArrayOutputStream) out).toString(UTF_8);
  }

  public static String lines(String... lines) {
    StringBuilder out = new StringBuilder();
    for (String line : lines) {
      out.append(line).append(NL);
    }
    return out.toString();
  }

}
