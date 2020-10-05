package com.auzeill.regex.graph;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegExDump {

  private static final String NL = "\n";

  public static void main(String[] args) {
    for (String arg : args) {
      System.out.println(transform(arg));
    }
  }

  public static String transform(String pattern) {
    return transform(Pattern.compile(pattern));
  }

  public static String transform(Object pattern) {
    StringBuilder out = new StringBuilder();
    HashMap<Object, Integer> references = new HashMap<>();
    dumpObject(out, pattern, "", references);
    return out.toString();
  }

  private static boolean isLiteral(Object object) {
    return object == null || object instanceof Number || object instanceof Boolean || object instanceof String || object instanceof int[];
  }

  private static String dumpLiteral(Object object) {
    if (object == null) {
      return "null";
    } else if (object instanceof Number || object instanceof Boolean) {
      return object.toString();
    } else if (object instanceof int[]) {
      return "[" + IntStream.of((int[]) object).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + "]";
    } else if (object instanceof String) {
      return "\"" + object.toString() + "\"";
    } else {
      throw new IllegalArgumentException(object.getClass().getName());
    }
  }

  private static void dumpObject(StringBuilder out, Object object, String indent, HashMap<Object, Integer> references) {
    Integer reference = references.get(object);
    if (reference != null) {
      out.append(indent).append("ref(").append(reference).append(')').append(NL);
    } else if (object == null) {
      out.append(indent).append("null").append(NL);
    } else if (object instanceof Number || object instanceof Boolean) {
      out.append(indent).append(object.toString()).append(NL);
    } else if (object instanceof String) {
      out.append(indent).append('"').append(object.toString()).append('"').append(NL);
    } else {
      reference = references.size() + 1;
      references.put(object, reference);
      String className = object.getClass().getName()
        .replaceFirst("\\$Lambda\\$\\d+(/0x[0-9a-fA-F]+)?$", "\\$Lambda");
      out.append(indent).append("obj(").append(reference).append(") ").append(className).append(" {").append(NL);
      dumpObjectFields(out, object, object.getClass(), indent + "  ", references);
      out.append(indent).append("}").append(NL);
    }
  }

  private static void dumpObjectFields(StringBuilder out, Object object, Class<?> objectClass, String indent, HashMap<Object, Integer> references) {
    Class<?> superclass = objectClass.getSuperclass();
    if (superclass != null) {
      dumpObjectFields(out, object, superclass, indent, references);
    }
    for (Field field : objectClass.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        dumpObjectField(out, object, field, indent, references);
      }
    }
  }

  private static void dumpObjectField(StringBuilder out, Object object, Field field, String indent, HashMap<Object, Integer> references) {
    if (!field.isAccessible()) {
      field.setAccessible(true);
    }
    Object value = null;
    try {
      value = field.get(object);
    } catch (IllegalAccessException e) {
      value = "<" + e.getClass().getName() + ": " + e.getMessage() + ">";
    }
    if (isLiteral(value)) {
      out.append(indent).append('.').append(field.getName()).append(": ").append(dumpLiteral(value)).append(NL);
    } else {
      out.append(indent).append('.').append(field.getName()).append(':').append(NL);
      dumpObject(out, value, indent + "  ", references);
    }
  }

}
