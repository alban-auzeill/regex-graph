package com.auzeill.regex.graph;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegExGraph {

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
    Deque<Object> objectToRender = new ArrayDeque<>();
    List<Link> links = new ArrayList<>();
    objectToRender.push(pattern);
    out.append("digraph G {").append(NL);
    while (!objectToRender.isEmpty()) {
      Object object = objectToRender.removeFirst();
      dumpNode(out, object, "  ", references, objectToRender, links);
    }
    for (Link link : links) {
      out.append("  ").append(link.toString()).append(NL);
    }
    out.append("}").append(NL);
    return out.toString();
  }

  private static String escape(String data) {
    return data
      .replace("\\", "\\\\")
      .replace("\r", "\\r")
      .replace("\n", "\\l")
      .replace("{", "\\{")
      .replace("}", "\\}")
      .replace("\"", "\\\"");
  }

  private static boolean isLiteral(Object object) {
    return object == null ||
      object instanceof Number ||
      object instanceof Boolean ||
      object instanceof String ||
      object instanceof boolean[] ||
      object instanceof int[] ||
      object instanceof Enum;
  }

  private static String dumpLiteral(Object object) {
    if (object == null) {
      return "null";
    } else if (object instanceof Number || object instanceof Boolean || object instanceof Enum) {
      return object.toString();
    } else if (object instanceof int[]) {
      return "[" + IntStream.of((int[]) object).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + "]";
    } else if (object instanceof boolean[]) {
      boolean[] arr = (boolean[]) object;
      if (arr.length > 3) {
        return "[" + IntStream.range(0, 3).mapToObj(idx -> arr[idx]).map(String::valueOf).collect(Collectors.joining(", ")) + ", ...]";
      } else {
        return "[" + IntStream.range(0, arr.length).mapToObj(idx -> arr[idx]).map(String::valueOf).collect(Collectors.joining(", ")) + "]";
      }
    } else if (object instanceof String) {
      return "\"" + object.toString() + "\"";
    } else {
      throw new IllegalArgumentException(object.getClass().getName());
    }
  }

  private static void dumpNode(StringBuilder out, Object object, String indent, HashMap<Object, Integer> references, Deque<Object> objectToRender, List<Link> links) {
    Integer reference = references.get(object);
    if (reference == null) {
      reference = references.size() + 1;
      references.put(object, reference);
    }
    StringBuilder description = new StringBuilder();
    String className = object.getClass().getName()
      .replace("java.util.regex.Pattern$", "")
      .replaceFirst("\\$Lambda\\$\\d+(/0x[0-9a-fA-F]+)?$", "\\$Lambda");
    description.append(className).append(" obj_").append(reference).append(" {").append(NL);
    dumpObjectFields(description, object, object.getClass(), "  ", references, objectToRender, links);
    description.append("}").append(NL);
    out.append(indent).append("obj_").append(reference).append(" [label=\"").append(escape(description.toString())).append("\"]").append(NL);
  }

  private static void dumpObject(StringBuilder out, Object object, String indent, HashMap<Object, Integer> references, Deque<Object> objectToRender, List<Link> links) {
    Integer reference = references.get(object);
    if (reference != null) {
      out.append(indent).append("ref obj_").append(reference).append(NL);
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
      out.append(indent).append(className).append(" obj_").append(reference).append(" {").append(NL);
      dumpObjectFields(out, object, object.getClass(), indent + "  ", references, objectToRender, links);
      out.append(indent).append("}").append(NL);
    }
  }

  private static void dumpObjectFields(StringBuilder out, Object object, Class<?> objectClass, String indent, HashMap<Object, Integer> references, Deque<Object> objectToRender, List<Link> links) {
    Class<?> superclass = objectClass.getSuperclass();
    if (superclass != null) {
      dumpObjectFields(out, object, superclass, indent, references, objectToRender, links);
    }
    for (Field field : objectClass.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        dumpObjectField(out, object, field, indent, references, objectToRender, links);
      }
    }
  }

  private static void dumpObjectField(StringBuilder out, Object object, Field field, String indent, HashMap<Object, Integer> references, Deque<Object> objectToRender, List<Link> links) {
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
    } else if (value.getClass().getName().equals("[Ljava.util.regex.Pattern$Node;")) {
      int length = Array.getLength(value);
      for (int i = 0; i < length; i++) {
        Object child = Array.get(value, i);
        if (child == null) {
          out.append(indent).append('.').append(field.getName()).append("[").append(i).append("]").append(": null").append(NL);
        } else {
          createNodeLink(object, child, field.getName() + "[" + i + "]", references, objectToRender, links);
        }
      }
    } else if (value.getClass().getName().startsWith("java.util.regex.Pattern")) {
      createNodeLink(object, value, field.getName(), references, objectToRender, links);
    } else {
      out.append(indent).append('.').append(field.getName()).append(':').append(NL);
      dumpObject(out, value, indent + "  ", references, objectToRender, links);
    }
  }

  private static void createNodeLink(Object source, Object target, String name, HashMap<Object, Integer> references, Deque<Object> objectToRender, List<Link> links) {
    Integer reference = references.get(target);
    if (reference == null) {
      reference = references.size() + 1;
      references.put(target, reference);
      objectToRender.push(target);
    }
    links.add(new Link("obj_" + references.get(source), "obj_" + reference, name));
  }

  static class Link {
    String from;
    String to;
    String label;

    public Link(String from, String to, String label) {
      this.from = from;
      this.to = to;
      this.label = label;
    }

    @Override
    public String toString() {
      return from + "  -> " + to + " [taillabel = \""+escape(label)+"\"]";
    }
  }

}
