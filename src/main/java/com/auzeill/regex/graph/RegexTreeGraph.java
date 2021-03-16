package com.auzeill.regex.graph;

import com.auzeill.regex.graph.GraphContext.Edge;
import com.auzeill.regex.graph.GraphContext.Node;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.LiteralTreeImpl;
import org.sonar.java.regex.JavaRegexSource;
import org.sonar.java.regex.RegexParseResult;
import org.sonar.java.regex.RegexParser;
import org.sonar.java.regex.SyntaxError;
import org.sonar.java.regex.ast.AbstractRegexSyntaxElement;
import org.sonar.java.regex.ast.AutomatonState;
import org.sonar.java.regex.ast.BackReferenceTree;
import org.sonar.java.regex.ast.BranchState;
import org.sonar.java.regex.ast.CapturingGroupTree;
import org.sonar.java.regex.ast.EndOfCapturingGroupState;
import org.sonar.java.regex.ast.EndOfLookaroundState;
import org.sonar.java.regex.ast.EndOfRepetitionState;
import org.sonar.java.regex.ast.FinalState;
import org.sonar.java.regex.ast.FlagSet;
import org.sonar.java.regex.ast.IndexRange;
import org.sonar.java.regex.ast.LookAroundTree;
import org.sonar.java.regex.ast.NegationState;
import org.sonar.java.regex.RegexSource;
import org.sonar.java.regex.ast.RegexSyntaxElement;
import org.sonar.java.regex.ast.RegexToken;
import org.sonar.java.regex.ast.RegexTree;
import org.sonar.java.regex.ast.RepetitionTree;
import org.sonar.java.regex.ast.SourceCharacter;
import org.sonar.java.regex.ast.StartOfLookBehindState;
import org.sonar.java.regex.ast.StartState;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

public class RegexTreeGraph extends GraphWriter {

  private final RegexParseResult result;
  private final boolean includeTrees;
  private final boolean includeStates;
  private final boolean onlyLegend;

  public RegexTreeGraph(RegexParseResult result, boolean includeTrees, boolean includeStates, boolean onlyLegend) {
    this.result = result;
    this.includeTrees = includeTrees;
    this.includeStates = includeStates;
    this.onlyLegend = onlyLegend;
  }

  public static String transform(String stringLiteral, boolean includeTrees, boolean includeStates, boolean onlyLegend) {
    RegexParseResult result = null;
    if (!onlyLegend) {
      InternalSyntaxToken token = new InternalSyntaxToken(1, 1, stringLiteral, Collections.emptyList(), false);
      LiteralTree literalTree = new LiteralTreeImpl(Kind.STRING_LITERAL, token);
      RegexSource source = new JavaRegexSource(Collections.singletonList(literalTree));
      RegexParser parser = new RegexParser(source, new FlagSet());
      result = parser.parse();
    }
    RegexTreeGraph writer = new RegexTreeGraph(result, includeTrees, includeStates, onlyLegend);
    String title = stringLiteral;
    if (includeTrees && includeStates) {
      title += " AST & States";
    } else if (includeTrees) {
      title += " AST";
    } else if (includeStates) {
      title += " States";
    }
    return writer.graph(result != null ? result.getResult() : null, title);
  }

  @Override
  void writeGraphStyle(StringBuilder out, String title) {
    out.append(INDENTATION).append("rankdir=LR;").append(NL);
    if (onlyLegend) {
      out.append(INDENTATION).append("graph [fontname=\"Monospace\", fontsize=\"11\", pad=\"0.01\", nodesep=\"0.01\", ranksep=\"0.01\"]").append(NL);
    } else {
      out.append(INDENTATION).append("graph [fontname=\"Monospace\", fontsize=\"13\", pad=\"0.3\"]").append(NL);
      if (title != null) {
        out.append(INDENTATION).append("labelloc=\"t\";").append(NL);
        out.append(INDENTATION).append("label=").append(writeCenteredDotLabel(title)).append(";").append(NL);
      }
    }
  }

  @Override
  void writeNodesStyle(StringBuilder out, String nodeType) {
    String shape = "box";
    String style = "rounded,filled";
    String color = "LightGray";
    String fillColor = "Beige";
    String fixedSize = "false";
    String width = "0.75";
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
        color = "#7070E0";
        fillColor = "#7070E0";
        fixedSize = "true";
        width = "0.20";
        break;
      case "end":
        shape = "doublecircle";
        color = "#7070E0";
        fillColor = "#7070E0";
        fixedSize = "true";
        width = "0.12";
        break;
      case "plaintext":
        shape = "plaintext";
        style = "none";
        fillColor = "none";
        fixedSize = "true";
        width = "0.12";
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
    String fontColor = "DarkSlateGray";
    switch (edgeType) {
      case "epsilon-successor":
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "character-successor":
        arrowHead = "dotvee";
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "backtracking-successor":
        arrowHead = "icurvevee";
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "negation-successor":
        arrowHead = "odiamondvee";
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "backreference-successor":
        arrowHead = "boxvee";
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "continuation":
        style = "dashed";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "reference":
        style = "dashed";
        color = "Red";
        fontColor = "Firebrick";
        break;
      case "transparent":
        style = "invis";
        color = "transparent";
        break;
    }
    out.append(INDENTATION).append("edge [fontname=\"Monospace\", fontsize=\"9\", style=\"").append(style)
      .append("\", color=\"").append(color).append("\", fontcolor=\"").append(fontColor)
      .append("\", arrowhead=\"").append(arrowHead).append("\", arrowtail=\"").append(arrowTail)
      .append("\", dir=\"both\"]").append(NL);
  }

  @Override
  void extendsContext(Object object, GraphContext context) {
    if (onlyLegend) {
      context.add(new Node("1", "AST", "default"));
      context.add(new Node("2", "", "plaintext"));
      context.add(new Node("3", "", "plaintext"));
      context.add(new Node("4", "AST & State", "tree-and-state"));
      context.add(new Node("5", "State", "state"));
      context.add(new Node("6", "", "plaintext"));
      context.add(new Node("7", "", "plaintext"));
      context.add(new Node("8", "", "plaintext"));
      context.add(new Node("9", "", "plaintext"));
      context.add(new Node("10", "", "plaintext"));
      context.add(new Node("11", "", "plaintext"));
      context.add(new Node("12", "", "plaintext"));
      context.add(new Node("13", "", "plaintext"));

      context.add(new Edge("1", "2", "", "", "transparent"));
      context.add(new Edge("2", "3", "AST hierarchy  ", "", "default"));
      context.add(new Edge("3", "4", "", "", "transparent"));
      context.add(new Edge("4", "5", "", "", "transparent"));
      context.add(new Edge("5", "6", "", "", "transparent"));
      context.add(new Edge("6", "7", "epsilon\nsuccessor ", "", "epsilon-successor"));
      context.add(new Edge("7", "8", "character\nsuccessor ", "", "character-successor"));
      context.add(new Edge("8", "9", "backreference\nsuccessor ", "", "backreference-successor"));
      context.add(new Edge("9", "10", "negation\nsuccessor ", "", "negation-successor"));
      context.add(new Edge("10", "11", "backtracking\nsuccessor ", "", "backtracking-successor"));
      context.add(new Edge("11", "12", "continuation ", "", "continuation"));
      context.add(new Edge("12", "13", "reference  ", "", "reference"));
    } else {
      if (includeStates) {
        includeStates(result);
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
  }

  public void includeStates(RegexParseResult result) {
    if (!context.nodes().isEmpty()) {
      Set<AutomatonState> processedStates = new LinkedHashSet<>();
      Deque<AutomatonState> processingQueue = new LinkedList<>();
      processingQueue.addLast(result.getStartState());
      while (!processingQueue.isEmpty()) {
        AutomatonState state = processingQueue.removeFirst();
        if (state != null && processedStates.add(state)) {
          createState(state);
          state.successors().forEach(processingQueue::addLast);
          processingQueue.addLast(state.continuation());
        }
      }
      if (!processedStates.contains(result.getFinalState())) {
        throw new IllegalStateException("FinalState is not reachable from StartState");
      }
      for (AutomatonState state : processedStates) {
        List<? extends AutomatonState> successors = state.successors();
        for (int i = 0; i < successors.size(); i++) {
          String label = successors.size() == 1 ? "" : String.valueOf(i + 1);
          createSuccessorEdge(state, successors.get(i), label);
        }
        if (state.continuation() != null) {
          createContinuationEdge(state, state.continuation());
        }
        createParentEdge(state, processedStates);
      }
    }
  }

  private void createState(AutomatonState state) {
    String reference = context.getNodeReference(state);
    Node node = context.getNode(reference);
    if (node != null) {
      node.type = "tree-and-state";
      return;
    }
    if (reference != null) {
      throw new IllegalStateException("Unexpected node reference without node");
    }
    String label;
    String type = "state";
    if (state instanceof StartState) {
      reference = "StartState";
      label = "StartState\n\n\n\n";
      type = "start";
    } else if (state instanceof FinalState) {
      reference = "EndOfRegex";
      label = "EndOfRegex\n\n\n\n";
      type = "end";
    } else if (state instanceof BranchState) {
      reference = createNewNodeReference(state);
      label = "Branch:" + reference;
    } else if (state instanceof EndOfLookaroundState) {
      reference = createNewNodeReference(state);
      label = "EndOfLookAround:" + reference + (state.continuation() == null ? "\n  continuation: null" : "") + "\n";
    } else if (state instanceof EndOfCapturingGroupState) {
      reference = createNewNodeReference(state);
      label = "EndOfCapturingGroup:" + reference;
    } else if (state instanceof EndOfRepetitionState) {
      reference = createNewNodeReference(state);
      label = "EndOfRepetitionState:" + reference;
    } else if (state instanceof NegationState) {
      reference = createNewNodeReference(state);
      label = "Negation:" + reference;
    } else if (state instanceof StartOfLookBehindState) {
      reference = createNewNodeReference(state);
      label = "StartOfLookBehindState:" + reference;
    } else {
      throw new IllegalStateException("Unsupported AutomatonState " + state.getClass());
    }
    context.add(new Node(reference, label, type));
    context.setNodeReference(state, reference);
  }

  private void createSuccessorEdge(AutomatonState from, AutomatonState to, String label) {
    createEdge(from, to, label, successorType(to));
  }

  private void createContinuationEdge(AutomatonState from, AutomatonState to) {
    createEdge(from, to, "", "continuation");
  }

  private void createEdge(AutomatonState from, AutomatonState to, String label, String type) {
    String fromReference = context.getNodeReference(from);
    if (fromReference == null) {
      throw new IllegalStateException("Missing reference for " + from.getClass());
    }
    String toReference = context.getNodeReference(to);
    if (toReference == null) {
      throw new IllegalStateException("Missing reference for " + to.getClass());
    }
    context.add(new Edge(fromReference, toReference, label, type));
  }

  private void createParentEdge(AutomatonState state, Set<AutomatonState> allStates) {
    if (state instanceof BranchState) {
      createEdge(state, getParent((BranchState) state), "parent", "reference");
    } else if (state instanceof EndOfLookaroundState) {
      createEdge(state, getParent((EndOfLookaroundState) state), "parent", "reference");
    } else if (state instanceof EndOfCapturingGroupState) {
      createEdge(state, ((EndOfCapturingGroupState) state).group(), "group", "reference");
    } else if (state instanceof EndOfRepetitionState) {
      createEdge(state, getParent((EndOfRepetitionState) state), "parent", "reference");
    } else if (state instanceof BackReferenceTree) {
      findMatchingGroup((BackReferenceTree) state, allStates)
        .ifPresent(capturingGroup -> createEdge(state, capturingGroup, "reference", "reference"));
    }
  }

  private String createNewNodeReference(AutomatonState state) {
    String reference = context.createObjectReference(state);
    context.setNodeReference(state, reference);
    return reference;
  }

  private static String successorType(AutomatonState to) {
    switch (to.incomingTransitionType()) {
      case EPSILON:
        return "epsilon-successor";
      case CHARACTER:
        return "character-successor";
      case BACK_REFERENCE:
        return "backreference-successor";
      case LOOKAROUND_BACKTRACKING:
        return "backtracking-successor";
      case NEGATION:
        return "negation-successor";
      default:
        throw new IllegalStateException("Unsupported TransitionType: " + to.incomingTransitionType());
    }
  }

  private Optional<CapturingGroupTree> findMatchingGroup(BackReferenceTree backReference, Set<AutomatonState> allStates) {
    return allStates.stream()
      .filter(CapturingGroupTree.class::isInstance)
      .map(CapturingGroupTree.class::cast)
      .filter(group -> matches(backReference, group))
      .findFirst();
  }

  private static boolean matches(BackReferenceTree backReferenceTree, CapturingGroupTree capturingGroup) {
    if (backReferenceTree.isNumerical()) {
      return capturingGroup.getGroupNumber() == backReferenceTree.groupNumber();
    } else {
      return capturingGroup.getName().filter(name -> name.equals(backReferenceTree.groupName())).isPresent();
    }
  }

  // TODO fix this
  private static LookAroundTree getParent(EndOfLookaroundState state) {
    try {
      Field field = EndOfLookaroundState.class.getDeclaredField("parent");
      if (!field.canAccess(state)) {
        field.setAccessible(true);
      }
      return (LookAroundTree) field.get(state);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }

  private static RepetitionTree getParent(EndOfRepetitionState state) {
    try {
      Field field = EndOfRepetitionState.class.getDeclaredField("parent");
      if (!field.canAccess(state)) {
        field.setAccessible(true);
      }
      return (RepetitionTree) field.get(state);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }

  // TODO fix this
  private static RegexTree getParent(BranchState state) {
    try {
      Field field = BranchState.class.getDeclaredField("parent");
      if (!field.canAccess(state)) {
        field.setAccessible(true);
      }
      return (RegexTree) field.get(state);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
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
    if (field.getDeclaringClass().equals(AbstractRegexSyntaxElement.class) && field.getName().equals("range")) {
      return directValue((RegexSyntaxElement) object);
    }
    return super.getObjectFieldValue(object, field);
  }

  @Override
  Object filterValue(Object value) {
    if (value.getClass().equals(IndexRange.class)) {
      IndexRange range = (IndexRange) value;
      return new DirectValue("{" + range.getBeginningOffset() + ", " + range.getEndingOffset() + "}");
    } else if (value.getClass().equals(FlagSet.class)) {
      int mask = ((FlagSet) value).getMask();
      return mask == 0 ? null : new DirectValue(Integer.toString(mask));
    } else if (value.getClass().equals(SourceCharacter.class)) {
      SourceCharacter javaCharacter = (SourceCharacter) value;
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
    return (field.getDeclaringClass().equals(AbstractRegexSyntaxElement.class) && field.getName().equals("source")) ||
      (field.getDeclaringClass().equals(LookAroundTree.class) && field.getName().equals("successors")) ||
      (field.getDeclaringClass().equals(RegexTree.class) && field.getName().equals("continuation")) ||
      (field.getDeclaringClass().equals(LookAroundTree.class) && field.getName().equals("inner"));
  }

}
