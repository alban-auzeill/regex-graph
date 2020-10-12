package com.auzeill.regex.graph;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphWriter {
  public static final char NL = '\n';
  public static final String STR_NL = String.valueOf(NL);
  public static final String INDENTATION = "  ";
  public static final int MAX_STRING_LENGTH = 40;
  public static final int MAX_ARRAY_SIZE = 10;

  GraphContext context = new GraphContext();

  public GraphWriter() {

  }

  boolean isNode(Object object) {
    return false;
  }

  String nodeType(Object object) {
    return "default";
  }

  String graph(Object object) {
    GraphContext savedContext = context;
    context = new GraphContext();
    write(object);
    extendsContext(object, context);
    StringBuilder out = new StringBuilder();
    out.append("digraph G {").append(NL);
    writeGraphStyle(out);

    context.nodes().stream()
      .collect(Collectors.groupingBy(node -> node.type, LinkedHashMap::new, Collectors.toList()))
      .forEach((type, nodes) -> {
        out.append(NL);
        out.append(INDENTATION).append("// ").append(type).append(" nodes").append(NL);
        writeNodesStyle(out, type);
        nodes.forEach(node -> out.append("  ")
          .append(node.name)
          .append("[ label=").append(writeLeftAlignDotLabel(node.label)).append(" ]")
          .append(NL));
      });
    context.edges().stream()
      .collect(Collectors.groupingBy(edge -> edge.type, LinkedHashMap::new, Collectors.toList()))
      .forEach((type, edges) -> {
        out.append(NL);
        out.append(INDENTATION).append("// ").append(type).append(" edges").append(NL);
        writeEdgesStyle(out, type);
        edges.forEach(edge -> out.append("  ")
          .append(edge.source).append(" -> ").append(edge.target)
          .append(" [ label=").append(writeCenteredDotLabel(edge.centerLabel)).append(",  taillabel=").append(writeCenteredDotLabel(edge.tailLabel)).append(" ]")
          .append(NL));
      });
    out.append("}").append(NL);
    context = savedContext;
    return out.toString();
  }

  void extendsContext(Object object, GraphContext context) {
    // can be overridden
  }

  void writeGraphStyle(StringBuilder out) {
    out.append(INDENTATION).append("rankdir=LR;").append(NL);
    out.append(INDENTATION).append("graph [fontname=\"Monospace\", fontsize=\"11\"]").append(NL);
  }

  void writeNodesStyle(StringBuilder out, String nodeType) {
    out.append(INDENTATION).append("node [fontname=\"Monospace\", fontsize=\"9\", shape=\"box\", style=\"rounded,filled\", color=\"LightGray\", fillcolor=\"Beige\"]").append(NL);
  }

  void writeEdgesStyle(StringBuilder out, String edgeType) {
    out.append(INDENTATION).append("edge [fontname=\"Monospace\", fontsize=\"9\", color=\"Navy\"]").append(NL);
  }

  static class DirectValue {
    final String value;

    DirectValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  String write(Object object) {
    if (object == null) {
      return "null";
    }
    object = filterValue(object);
    if (object instanceof DirectValue || object instanceof Boolean || object instanceof Number) {
      // Number: Byte Short Integer Long Float Double BigInteger BigDecimal ...
      return object.toString();
    } else if (object instanceof Character) {
      return "'" + escapeJavaString(object.toString(), true) + "'";
    } else if (object instanceof String) {
      return "\"" + escapeJavaString(object.toString(), false) + "\"";
    } else if (object instanceof Enum) {
      return ((Enum<?>) object).name();
    } else {
      return writeObjectOrReference(object);
    }
  }

  String writeArray(Object object, String reference) {
    return writeIterable(arrayObjects(object), reference);
  }

  String writeIterable(Iterable<?> iterable, String reference) {
    StringBuilder singleLine = new StringBuilder();
    StringBuilder multiLine = new StringBuilder();
    singleLine.append(":").append(reference).append("[");
    multiLine.append(":").append(reference).append("[").append(NL);
    int index = 0;
    boolean hasEdge = false;
    boolean hasNonEdge = false;
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object value = iterator.next();
      String currentField = context.pushFieldName("", "[" + index + "]");
      String content = write(value);
      context.popFieldName();
      if (createEdge(currentField, value)) {
        hasEdge = true;
      } else if (value != null) {
        hasNonEdge = true;
      }
      String separator = iterator.hasNext() ? "," : "";
      if (index + 1 == MAX_ARRAY_SIZE && iterator.hasNext()) {
        content = "…";
        separator = "";
      } else if (content == null) {
        content = ".";
      }
      if (index < MAX_ARRAY_SIZE) {
        singleLine.append(" ").append(content).append(separator);
        multiLine.append(indent(content, separator));
      }
      index++;
    }
    singleLine.append(" ]");
    multiLine.append("]").append(NL);
    if (hasEdge && !hasNonEdge) {
      return null;
    }
    return singleLine.length() > MAX_STRING_LENGTH ? multiLine.toString() : singleLine.toString();
  }

  String writeMap(Map<?, ?> map, String reference) {
    if (map.isEmpty()) {
      return ":" + reference + "{ }";
    }
    StringBuilder out = new StringBuilder();
    out.append(":").append(reference).append("{").append(NL);
    boolean hasEdge = false;
    boolean hasNonEdge = false;
    int index = 0;
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String currentField = context.pushFieldName("", "[" + index + "].key");
      Object key = entry.getKey();
      String keyContent = write(key);
      context.popFieldName();
      if (createEdge(currentField, key)) {
        hasEdge = true;
      } else if (key != null) {
        hasNonEdge = true;
      }
      currentField = context.pushFieldName("", "[" + index + "].val");
      Object value = entry.getValue();
      String valueContent = write(value);
      context.popFieldName();
      if (createEdge(currentField, value)) {
        hasEdge = true;
      } else if (value != null) {
        hasNonEdge = true;
      }
      String separator = index + 1 < map.size() ? "," : "";
      if (index + 1 == MAX_ARRAY_SIZE && index + 1 < map.size()) {
        out.append(indent("…"));
      } else if (index < MAX_ARRAY_SIZE) {
        if (isSingleLine(keyContent) && isSingleLine(valueContent)) {
          out.append(indent(keyContent + ": " + valueContent, separator));
        } else {
          out.append(indent(keyContent, ":"));
          out.append(indent(indent(valueContent, separator)));
        }
      }
      index++;
    }
    out.append("}").append(NL);
    if (hasEdge && !hasNonEdge) {
      return null;
    }
    return out.toString();
  }

  Iterable<Object> arrayObjects(Object object) {
    List<Object> result = new ArrayList<>();
    int length = Array.getLength(object);
    for (int i = 0; i < length; i++) {
      result.add(Array.get(object, i));
    }
    return result;
  }

  String writeObjectOrReference(Object object) {
    String reference = context.getObjectReference(object);
    if (reference != null) {
      return "{" + reference + "}";
    }
    boolean firstObject = context.referenceCount() == 0;
    reference = context.createObjectReference(object);
    GraphContext.Node node = null;
    if (firstObject || isNode(object)) {
      context.setNodeReference(object, reference);
      node = new GraphContext.Node(reference, "⛏", nodeType(object));
      context.add(node);
      context.nodeStack().push(object);
      context.pushEmptyFieldName();
    }
    String content = writeObjectContent(object, reference);
    if (node != null) {
      context.popFieldName();
      context.nodeStack().pop();
      node.label = content;
      if (!firstObject) {
        return "{" + reference + "}";
      }
    }
    return content;
  }

  String writeObjectContent(Object object, String reference) {
    if (object.getClass().isArray()) {
      return writeArray(object, reference);
    } else if (object instanceof Iterable) {
      return writeIterable((Iterable<?>) object, reference);
    } else if (object instanceof Map) {
      return writeMap((Map<?, ?>) object, reference);
    }
    return writeObjectAndFields(object, reference);
  }

  String writeObjectAndFields(Object object, String reference) {
    List<Field> fields = nonStaticFields(object.getClass());
    String className = writeClassName(object) + ":" + reference;
    if (fields.isEmpty()) {
      return className + "{ }";
    }
    StringBuilder out = new StringBuilder();
    out.append(className).append("{").append(NL);
    for (Field field : fields) {
      String content = writeObjectNamedField(object, field);
      if (content != null) {
        out.append(indent(content));
      }
    }
    out.append("}").append(NL);
    return out.toString();
  }

  String writeObjectNamedField(Object object, Field field) {
    String value = writeObjectField(object, field);
    if (value == null) {
      return null;
    }
    String prefix = "." + field.getName() + ":";
    if (isSingleLine(value) && prefix.length() + 1 + value.length() <= MAX_STRING_LENGTH) {
      return prefix + " " + value + NL;
    }
    return prefix + NL + indent(value);
  }

  String writeObjectField(Object object, Field field) {
    try {
      Object value = getObjectFieldValue(object, field);
      String currentField = context.pushFieldName(".", field.getName());
      String content = write(value);
      context.popFieldName();
      return createEdge(currentField, value) ? null : content;
    } catch (IllegalAccessException e) {
      return "<" + e.getClass().getName() + ": " + e.getMessage() + ">";
    }
  }

  Object getObjectFieldValue(Object object, Field field) throws IllegalAccessException {
    if (!field.canAccess(object)) {
      field.setAccessible(true);
    }
    return field.get(object);
  }

  Object filterValue(Object value) {
    return value;
  }

  boolean createEdge(String currentField, Object value) {
    Object parent = context.nodeStack().peek();
    String parentNode = context.getNodeReference(parent);
    String valueNode = context.getNodeReference(value);
    if (parentNode != null && valueNode != null) {
      context.add(new GraphContext.Edge(parentNode, valueNode, edgeLabel(parent, currentField), edgeType(parentNode, valueNode)));
      return true;
    }
    return false;
  }

  String edgeLabel(Object parent, String field) {
    return field;
  }

  String edgeType(Object source, Object target) {
    return "default";
  }

  boolean isSingleLine(String data) {
    return data.length() == 0 || data.indexOf(NL) == -1;
  }

  String indent(String data) {
    return indent(data, "");
  }

  String indent(String data, String separatorSuffix) {
    if (isSingleLine(data)) {
      return INDENTATION + data + separatorSuffix + NL;
    }
    StringBuilder out = new StringBuilder();
    String[] lines = data.split(STR_NL, 0);
    for (int i = 0; i < lines.length; i++) {
      out.append(INDENTATION);
      out.append(lines[i]);
      if (i + 1 == lines.length) {
        out.append(separatorSuffix);
      }
      out.append(NL);
    }
    return out.toString();
  }

  String writeClassName(Object object) {
    return object.getClass().getName()
      .replaceFirst("^java\\.(?:lang|util)\\.([^.]+)$", "$1")
      .replaceFirst("\\$Lambda\\$\\d+(/0x[0-9a-fA-F]+)?$", "\\$Lambda");
  }

  String escapeJavaString(String data, boolean singleQuoted) {
    String result = data
      .replace("\\", "\\\\")
      .replace("\r", "\\r")
      .replace("\n", "\\n")
      .replace("\t", "\\t")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\0", "\\0")
      .replace(singleQuoted ? "'" : "\"", singleQuoted ? "\\'" : "\\\"");
    if (result.length() > MAX_STRING_LENGTH - 2 /* quotes */) {
      result = result.substring(0, MAX_STRING_LENGTH - 2 /* quotes */ - 1 /* … */) + "…";
    }
    return result;
  }

  protected static String writeCenteredDotLabel(String data) {
    if (data == null) {
      return "\"\"";
    }
    return "\"" + escapeDotLabel(data, "\\n") + "\"";
  }

  protected static String writeLeftAlignDotLabel(String data) {
    if (data == null) {
      return "\"\"";
    } else if (!data.endsWith("\n")) {
      data = data + "\n";
    }
    return "\"" + escapeDotLabel(data, "\\l") + "\"";
  }

  protected static String escapeDotLabel(String data, String eol) {
    if (data == null) {
      return "";
    }
    return data
      .replace("\\", "\\\\")
      .replace("\r", "\\r")
      .replace("\n", eol)
      .replace("{", "\\{")
      .replace("}", "\\}")
      .replace("\"", "\\\"");
  }

  protected boolean ignoreField(Class<?> objectClass, Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  List<Field> nonStaticFields(Class<?> objectClass) {
    List<Field> fields = new ArrayList<>();
    Class<?> superclass = objectClass.getSuperclass();
    if (superclass != null) {
      fields.addAll(nonStaticFields(superclass));
    }
    for (Field field : objectClass.getDeclaredFields()) {
      if (!ignoreField(objectClass, field)) {
        fields.add(field);
      }
    }
    return fields;
  }

}
