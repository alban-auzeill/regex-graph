package com.auzeill.regex.graph;

import com.auzeill.regex.graph.GraphContext.Edge;
import com.auzeill.regex.graph.GraphContext.Node;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.LiteralTreeImpl;
import org.sonar.java.regex.RegexParseResult;
import org.sonar.java.regex.RegexParser;
import org.sonar.java.regex.SyntaxError;
import org.sonar.java.regex.ast.AtomicGroupTree;
import org.sonar.java.regex.ast.BackReferenceTree;
import org.sonar.java.regex.ast.BoundaryTree;
import org.sonar.java.regex.ast.CapturingGroupTree;
import org.sonar.java.regex.ast.CharacterClassIntersectionTree;
import org.sonar.java.regex.ast.CharacterClassTree;
import org.sonar.java.regex.ast.CharacterClassUnionTree;
import org.sonar.java.regex.ast.CharacterRangeTree;
import org.sonar.java.regex.ast.DisjunctionTree;
import org.sonar.java.regex.ast.DotTree;
import org.sonar.java.regex.ast.EscapedCharacterClassTree;
import org.sonar.java.regex.ast.FlagSet;
import org.sonar.java.regex.ast.IndexRange;
import org.sonar.java.regex.ast.JavaCharacter;
import org.sonar.java.regex.ast.LookAroundTree;
import org.sonar.java.regex.ast.MiscEscapeSequenceTree;
import org.sonar.java.regex.ast.NonCapturingGroupTree;
import org.sonar.java.regex.ast.PlainCharacterTree;
import org.sonar.java.regex.ast.Quantifier;
import org.sonar.java.regex.ast.RegexBaseVisitor;
import org.sonar.java.regex.ast.RegexSource;
import org.sonar.java.regex.ast.RegexSyntaxElement;
import org.sonar.java.regex.ast.RegexToken;
import org.sonar.java.regex.ast.RegexTree;
import org.sonar.java.regex.ast.RepetitionTree;
import org.sonar.java.regex.ast.SequenceTree;
import org.sonar.java.regex.ast.UnicodeCodePointTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

public class RegexTreeGraph extends GraphWriter {

  private final RegexParseResult result;
  private final boolean includeTrees;
  private final boolean includeStates;

  public RegexTreeGraph(RegexParseResult result, boolean includeTrees, boolean includeStates) {
    this.result = result;
    this.includeTrees = includeTrees;
    this.includeStates = includeStates;
  }

  public static String transform(String stringLiteral, boolean includeTrees, boolean includeStates) {
    InternalSyntaxToken token = new InternalSyntaxToken(1, 1, stringLiteral, Collections.emptyList(), false);
    LiteralTree literalTree = new LiteralTreeImpl(Kind.STRING_LITERAL, token);
    RegexSource source = new RegexSource(Collections.singletonList(literalTree));
    RegexParser parser = new RegexParser(source, new FlagSet());
    RegexParseResult result = parser.parse();
    RegexTreeGraph writer = new RegexTreeGraph(result, includeTrees, includeStates);
    return writer.graph(result.getResult());
  }

  @Override
  void writeNodesStyle(StringBuilder out, String nodeType) {
    String shape = "box";
    String style = "rounded,filled";
    String color = "LightGray";
    String fillColor = "Beige";
    String fixedSize="false";
    String width="0.75";
    switch (nodeType) {
      case "error":
        color = "Red";
        fillColor = "Orange";
        break;
      case "tree-and-state":
        color = "Blue";
        fillColor = "Beige";
        break;
      case "state":
        color = "DodgerBlue";
        fillColor = "LightSkyBlue";
        break;
      case "start":
        shape = "circle";
        color = "DodgerBlue";
        fillColor = "DodgerBlue";
        fixedSize="true";
        width="0.20";
        break;
      case "end":
        shape = "doublecircle";
        color = "DodgerBlue";
        fillColor = "DodgerBlue";
        fixedSize="true";
        width="0.12";
        break;
    }
    out.append(INDENTATION).append("node [fontname=\"Monospace\", fontsize= \"9\", shape=\"").append(shape)
      .append("\", style=\"").append(style).append("\", color=\"").append(color).append("\", fillcolor=\"")
      .append(fillColor).append("\", fixedsize=\"").append(fixedSize).append("\", width=\"")
      .append(width).append("\"]").append(NL);
  }

  @Override
  void writeEdgesStyle(StringBuilder out, String edgeType) {
    String arrowTail = "none";
    String arrowHead = "vee";
    String style = "solid";
    String color = "SlateGray";
    switch (edgeType) {
      case "successor":
        style = "bold";
        color = "DodgerBlue";
        break;
      case "possessive-successor":
        arrowTail = "tee";
        style = "bold";
        color = "DodgerBlue";
        break;
      case "negation-successor":
        arrowTail = "odot";
        style = "bold";
        color = "DodgerBlue";
        break;
      case "continuation":
        style = "dashed";
        color = "DodgerBlue";
        break;
      case "back-reference":
        style = "dashed";
        color = "Red";
        break;
    }
    out.append(INDENTATION).append("edge [fontname=\"Monospace\", fontsize=\"9\", style=\"").append(style)
      .append("\", color=\"").append(color).append("\", fontcolor=\"").append(color)
      .append("\", arrowhead=\"").append(arrowHead).append("\", arrowtail=\"").append(arrowTail)
      .append("\", dir=\"both\"]").append(NL);
  }

  @Override
  void extendsContext(Object object, GraphContext context) {
    if (includeStates) {
      new AutomatonStateMetadataVisitor(context).visit(result);
    }
    for (SyntaxError syntaxError : result.getSyntaxErrors()) {
      context.add(new Node(
        context.createObjectReference(syntaxError),
        "SyntaxError:\n" + syntaxError.getMessage(),
        "error"));
    }
    if (!includeTrees) {
      context.edges().removeIf(edge -> edge.type.equals("default"));
      context.nodes().removeIf(node -> node.type.equals("default"));
    }
  }

  static class AutomatonStateMetadataVisitor extends RegexBaseVisitor {
    private final GraphContext context;
    Map<String, List<String>> successorMap = new HashMap<>();
    Map<String, String> continuationMap = new HashMap<>();
    Map<String, String> continuationHeadType = new HashMap<>();
    List<CapturingGroupTree> capturingGroups = new ArrayList<>();
    List<BackReferenceTree> backReferenceTrees = new ArrayList<>();

    public AutomatonStateMetadataVisitor(GraphContext context) {
      this.context = context;
    }

    @Override
    public void visit(RegexParseResult regexParseResult) {
      if (!context.nodes().isEmpty()) {
        RegexTree firstNode = regexParseResult.getResult();
        String endOfRegexReference = context.createReference();
        setSuccessor(firstNode, endOfRegexReference);
        setContinuation(firstNode, endOfRegexReference);

        super.visit(regexParseResult);

        linkBackReferenceToCapturingGroup();

        context.add(new Node(endOfRegexReference, "EndOfRegex:" + endOfRegexReference + "\n  continuation: null\n", "state"));

        successorMap.forEach(this::createSuccessorEdges);
        continuationMap.forEach(this::createContinuationEdge);

        context.add(new Node("end", "", "end"));
        context.add(new Edge(endOfRegexReference, "end", "", "successor"));
        context.add(new Node("start", "", "start"));
        context.add(new Edge("start", context.getNodeReference(firstNode), "", "successor"));
      }
    }

    private void linkBackReferenceToCapturingGroup() {
      for (BackReferenceTree backReferenceTree : backReferenceTrees) {
        CapturingGroupTree capturingGroup = findMatchingGroup(backReferenceTree);
        if (capturingGroup != null) {
          String sourceReference = context.getNodeReference(backReferenceTree);
          String targetReference = context.getNodeReference(capturingGroup);
          context.add(new Edge(sourceReference, targetReference, "group", "back-reference"));
        }
      }
    }

    private CapturingGroupTree findMatchingGroup(BackReferenceTree backReferenceTree) {
      for (CapturingGroupTree capturingGroup : capturingGroups) {
        if (matches(backReferenceTree, capturingGroup)) {
          return capturingGroup;
        }
      }
      return null;
    }

    private boolean matches(BackReferenceTree backReferenceTree, CapturingGroupTree capturingGroup) {
      if (backReferenceTree.isNumerical()) {
        return capturingGroup.getGroupNumber() == backReferenceTree.groupNumber();
      } else {
        return capturingGroup.getName().filter(name -> name.equals(backReferenceTree.groupName())).isPresent();
      }
    }

    void markVisited(RegexTree regexTree) {
      Node node = context.getNode(context.getNodeReference(regexTree));
      if (node != null) {
        node.type = "tree-and-state";
      }
    }

    void insertSuccessor(RegexTree existingNode, RegexTree insertedNode) {
      setSuccessorReferences(insertedNode, getSuccessors(existingNode));
      setSuccessor(existingNode, context.getNodeReference(insertedNode));
    }

    void setSuccessor(RegexTree regexTree, String successor) {
      setSuccessorReferences(regexTree, Collections.singletonList(successor));
    }

    void setSuccessors(RegexTree regexTree, List<RegexTree> successors) {
      setSuccessorReferences(regexTree, successors.stream().map(context::getNodeReference).collect(Collectors.toList()));
    }

    void setSuccessorReferences(RegexTree regexTree, List<String> successors) {
      successorMap.put(context.getNodeReference(regexTree), successors);
    }

    List<String> getSuccessors(RegexTree regexTree) {
      return successorMap.get(context.getNodeReference(regexTree));
    }

    void copyContinuation(RegexTree definedNode, RegexTree newNode) {
      setContinuation(newNode, getContinuation(definedNode));
    }

    void setContinuation(RegexTree regexTree, String continuation) {
      continuationMap.put(context.getNodeReference(regexTree), continuation);
    }

    String getContinuation(RegexTree regexTree) {
      return continuationMap.get(context.getNodeReference(regexTree));
    }

    void createSuccessorEdges(String from, List<String> toList) {
      if (toList != null) {
        for (int i = 0; i < toList.size(); i++) {
          String label = toList.size() == 1 ? "" : String.valueOf(i + 1);
          createEdge(from, toList.get(i), label, "successor");
        }
      }
    }

    void createContinuationEdge(String from, String to) {
      createEdge(from, to, "", "continuation");
    }

    void createEdge(String from, String to, String label, String type) {
      if (from != null && to != null) {
        if (type.equals("successor")) {
          type = continuationHeadType.getOrDefault(to, "successor");
        }
        context.add(new Edge(from, to, label, type));
      }
    }

    @Override
    public void visitSequence(SequenceTree tree) {
      markVisited(tree);
      RegexTree previousNode = tree;
      for (RegexTree item : tree.getItems()) {
        insertSuccessor(previousNode, item);
        copyContinuation(previousNode, item);
        if (previousNode != tree) {
          setContinuation(previousNode, context.getNodeReference(item));
        }
        previousNode = item;
      }
      super.visitSequence(tree);
    }

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      markVisited(tree);
      List<RegexTree> alternatives = tree.getAlternatives();
      if (!alternatives.isEmpty()) {
        List<String> successors = getSuccessors(tree);
        for (RegexTree alternative : alternatives) {
          setSuccessorReferences(alternative, successors);
          copyContinuation(tree, alternative);
        }
        setSuccessors(tree, alternatives);
      }
      super.visitDisjunction(tree);
    }

    @Override
    public void visitCapturingGroup(CapturingGroupTree tree) {
      markVisited(tree);
      capturingGroups.add(tree);
      if (tree.getElement() != null) {
        insertSuccessor(tree, tree.getElement());
        copyContinuation(tree, tree.getElement());
      }
      super.visitCapturingGroup(tree);
    }

    @Override
    protected void doVisitNonCapturingGroup(NonCapturingGroupTree tree) {
      markVisited(tree);
      if (tree.getElement() != null) {
        insertSuccessor(tree, tree.getElement());
        copyContinuation(tree, tree.getElement());
      }
      super.doVisitNonCapturingGroup(tree);
    }

    @Override
    public void visitAtomicGroup(AtomicGroupTree tree) {
      markVisited(tree);
      if (tree.getElement() != null) {
        insertSuccessor(tree, tree.getElement());
        copyContinuation(tree, tree.getElement());
      }
      super.visitAtomicGroup(tree);
    }

    @Override
    public void visitLookAround(LookAroundTree tree) {
      markVisited(tree);
      if (tree.getElement() != null) {
        String treeReference = context.getNodeReference(tree);
        String elementReference = context.getNodeReference(tree.getElement());
        String endNodeReference = context.createReference();
        context.add(new Node(endNodeReference, "EndOfLookAround:" + endNodeReference + "\n  continuation: null\n", "state"));
        context.add(new Edge(endNodeReference, treeReference, "back", "back-reference"));
        if (tree.getPolarity() == LookAroundTree.Polarity.NEGATIVE) {
          String negationNode = context.createReference();
          context.add(new Node(negationNode, "Negation:" + negationNode, "state"));
          continuationHeadType.put(negationNode, "negation-successor");
          successorMap.put(treeReference, Collections.singletonList(negationNode));
          successorMap.put(negationNode, Collections.singletonList(elementReference));
          continuationMap.put(negationNode, elementReference);
        } else {
          successorMap.put(treeReference, Collections.singletonList(elementReference));
        }
        successorMap.put(elementReference, Collections.singletonList(endNodeReference));
        continuationMap.put(elementReference, endNodeReference);
      }
      super.visitLookAround(tree);
    }

    @Override
    public void visitRepetition(RepetitionTree tree) {
      markVisited(tree);
      if (tree.getElement() != null) {
        String treeReference = context.getNodeReference(tree);
        String elementReference = context.getNodeReference(tree.getElement());
        String endNodeReference = context.createReference();
        context.add(new Node(endNodeReference, "Branch:" + endNodeReference, "state"));
        List<String> endNodeSuccessors = new ArrayList<>();
        if (tree.getQuantifier().getMinimumRepetitions() > 0) {
          endNodeSuccessors.addAll(getSuccessors(tree));
        }
        Quantifier.Modifier modifier = tree.getQuantifier().getModifier();
        if (modifier == Quantifier.Modifier.RELUCTANT) {
          endNodeSuccessors.add(treeReference);
        } else if (modifier == Quantifier.Modifier.POSSESSIVE) {
          continuationHeadType.put(endNodeReference, "possessive-successor");
          endNodeSuccessors.add(0, treeReference);
        } else { // GREEDY
          endNodeSuccessors.add(0, treeReference);
        }
        if (tree.getQuantifier().getMinimumRepetitions() == 0) {
          if (modifier == Quantifier.Modifier.RELUCTANT) {
            successorMap.put(treeReference, Arrays.asList(getContinuation(tree), elementReference));
          } else {
            successorMap.put(treeReference, Arrays.asList(elementReference, getContinuation(tree)));
          }
        } else {
          successorMap.put(treeReference, Collections.singletonList(elementReference));
        }
        successorMap.put(elementReference, Collections.singletonList(endNodeReference));
        successorMap.put(endNodeReference, endNodeSuccessors);
        continuationMap.put(elementReference, endNodeReference);
        continuationMap.put(endNodeReference, getContinuation(tree));
      }
      super.visitRepetition(tree);
    }

    @Override
    public void visitBackReference(BackReferenceTree tree) {
      markVisited(tree);
      backReferenceTrees.add(tree);
    }

    @Override
    public void visitCharacterClass(CharacterClassTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitPlainCharacter(PlainCharacterTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitUnicodeCodePoint(UnicodeCodePointTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitCharacterRange(CharacterRangeTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitCharacterClassUnion(CharacterClassUnionTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitCharacterClassIntersection(CharacterClassIntersectionTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitDot(DotTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitEscapedCharacterClass(EscapedCharacterClassTree tree) {
      markVisited(tree);
    }

    @Override
    public void visitBoundary(BoundaryTree boundaryTree) {
      markVisited(boundaryTree);
    }

    @Override
    public void visitMiscEscapeSequence(MiscEscapeSequenceTree tree) {
      markVisited(tree);
    }
  }

  @Override
  String nodeType(Object object) {
    return "default";
  }

  @Override
  boolean isNode(Object object) {
    return object instanceof org.sonar.java.regex.ast.RegexSyntaxElement || super.isNode(object);
  }

  @Override
  String writeClassName(Object object) {
    return super.writeClassName(object)
      .replaceFirst("^org\\.sonar\\.java\\.regex\\.ast\\.([^.]+)$", "$1");
  }

  @Override
  Object getObjectFieldValue(Object object, Field field) throws IllegalAccessException {
    Object value = super.getObjectFieldValue(object, field);
    if (field.getDeclaringClass().equals(RegexSyntaxElement.class) && field.getName().equals("range")) {
      return directValue((RegexSyntaxElement) object);
    }
    return value;
  }

  @Override
  Object filterValue(Object value) {
    if (value.getClass().equals(IndexRange.class)) {
      IndexRange range = (IndexRange) value;
      return new DirectValue("{" + range.getBeginningOffset() + ", " + range.getEndingOffset() + "}");
    } else if (value.getClass().equals(JavaCharacter.class)) {
      JavaCharacter javaCharacter = (JavaCharacter) value;
      String escape = javaCharacter.isEscapeSequence() ? ", isEscapeSequence=true" : "";
      return new DirectValue("{'" + escapeJavaString(String.valueOf(javaCharacter.getCharacter()), true) + "'" + escape + "}");
    } else if (value.getClass().equals(RegexToken.class)) {
      return directValue((RegexToken) value);
    }
    return super.filterValue(value);
  }

  DirectValue directValue(RegexSyntaxElement element) {
    return new DirectValue("{" + element.getRange().getBeginningOffset() + ", " + element.getRange().getEndingOffset() +
      ", \"" + element.getText() + "\"}");
  }

  @Override
  protected boolean ignoreField(Class<?> objectClass, Field field) {
    if (super.ignoreField(objectClass, field)) {
      return true;
    }
    return objectClass.equals(RegexSyntaxElement.class) && field.getName().equals("source");
  }

}
