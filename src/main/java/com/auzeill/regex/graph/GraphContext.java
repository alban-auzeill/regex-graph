package com.auzeill.regex.graph;

import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphContext {

  private final Map<Object, String> objectReferences = new IdentityHashMap<>();
  private final Map<Object, String> nodeReferences = new IdentityHashMap<>();
  private final List<Node> nodes = new ArrayList<>();
  private final List<Edge> edges = new ArrayList<>();
  private final Deque<Object> nodeStack = new LinkedList<>();
  private final Deque<String> fieldStack = new LinkedList<>();

  public Deque<Object> nodeStack() {
    return nodeStack;
  }

  public void pushEmptyFieldName() {
    fieldStack.push(null);
  }

  public String pushFieldName(String separator, String name) {
    String parentField = fieldStack.peek();
    String currentField = parentField == null ? name : parentField + separator + name;
    fieldStack.push(currentField);
    return currentField;
  }

  public void popFieldName() {
    fieldStack.pop();
  }

  public String peekFieldName() {
    return fieldStack.peek();
  }

  public void add(Node node) {
    nodes.add(node);
  }

  public void add(Edge edge) {
    edges.add(edge);
  }

  public List<Node> nodes() {
    return nodes;
  }

  public List<Edge> edges() {
    return edges;
  }

  public int referenceCount() {
    return objectReferences.size();
  }

  public String getObjectReference(Object object) {
    return objectReferences.get(object);
  }

  public String createObjectReference(Object object) {
    String reference = String.valueOf(referenceCount() + 1);
    objectReferences.put(object, reference);
    return reference;
  }

  public String getNodeReference(Object object) {
    return nodeReferences.get(object);
  }

  public void setNodeReference(Object object, String reference) {
    nodeReferences.put(object, reference);
  }

  public static class Node {
    public final String name;
    public String label;
    public final String color;

    public Node(String name, String label, String color) {
      this.name = name;
      this.label = label;
      this.color = color;
    }
  }

  public static class Edge {
    public final String source;
    public final String target;
    public final String label;
    public final String color;

    public Edge(String source, String target, String label, String color) {
      this.source = source;
      this.target = target;
      this.label = label;
      this.color = color;
    }
  }
}
