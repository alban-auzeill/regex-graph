package com.auzeill.regex.graph;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

public class PatternGraph extends GraphWriter {

  private static final String NL = "\n";

  public static String transform(String stringLiteral) {
    String pattern = StringEscapeUtils.unescapeJava(stringLiteral.substring(1, stringLiteral.length() - 1));
    PatternGraph writer = new PatternGraph();
    Object compiled;
    try {
      compiled = Pattern.compile(pattern);
    } catch (Exception e) {
      compiled = Collections.singleton(new DirectValue(e.getClass().getSimpleName() + ":\n" + e.getMessage()));
    }
    return writer.graph(compiled);
  }

  @Override
  String writeClassName(Object object) {
    return super.writeClassName(object)
      .replaceFirst("^java\\.util\\.regex\\.Pattern\\$([^.]+)$", "$1")
      .replaceFirst("^java\\.util\\.regex\\.([^.]+)$", "$1");
  }

  @Override
  boolean isNode(Object object) {
    Class<?> objectClass = object.getClass();
    while (objectClass != null) {
      if (objectClass.getName().equals("java.util.regex.Pattern$Node")) {
        return true;
      }
      objectClass = objectClass.getSuperclass();
    }
    return super.isNode(object);
  }

  @Override
  Object filterValue(Object value) {
    if (value.getClass().getName().equals("java.util.regex.Pattern$BitClass")) {
      return bitClassValue(value);
    } else if (value instanceof Integer) {
      int ch = (Integer) value;
      if (ch >= 32 && ch < 256) {
        return (char) ch;
      }
    }
    return super.filterValue(value);
  }

  Object bitClassValue(Object value) {
    try {
      Field field = value.getClass().getDeclaredField("bits");
      boolean[] bits = (boolean[]) getObjectFieldValue(value, field);
      StringBuilder out = new StringBuilder();
      out.append("BitClass ");
      for (int i = 0; i < bits.length; i++) {
        if (i + 2 < bits.length && bits[i] && bits[i + 1] && bits[i + 2]) {
          out.append((char) i);
          out.append('…');
          while (i + 1 < bits.length && bits[i + 1]) {
            i++;
          }
          out.append((char) i);
          out.append(' ');
        } else if (bits[i]) {
          out.append((char) i);
          out.append(' ');
        }
      }
      return new DirectValue(out.toString());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

}
