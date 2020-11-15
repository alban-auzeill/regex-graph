package com.auzeill.regex.graph;

import com.auzeill.regex.graph.GraphContext.Edge;
import com.auzeill.regex.graph.GraphContext.Node;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.LiteralTreeImpl;
import org.sonar.java.regex.RegexParseResult;
import org.sonar.java.regex.RegexParser;
import org.sonar.java.regex.SyntaxError;
import org.sonar.java.regex.ast.AbstractRegexSyntaxElement;
import org.sonar.java.regex.ast.AtomicGroupTree;
import org.sonar.java.regex.ast.AutomatonState;
import org.sonar.java.regex.ast.BackReferenceTree;
import org.sonar.java.regex.ast.BoundaryTree;
import org.sonar.java.regex.ast.BranchState;
import org.sonar.java.regex.ast.CapturingGroupTree;
import org.sonar.java.regex.ast.CharacterClassIntersectionTree;
import org.sonar.java.regex.ast.CharacterClassTree;
import org.sonar.java.regex.ast.CharacterClassUnionTree;
import org.sonar.java.regex.ast.CharacterRangeTree;
import org.sonar.java.regex.ast.DisjunctionTree;
import org.sonar.java.regex.ast.DotTree;
import org.sonar.java.regex.ast.EndOfLookaroundState;
import org.sonar.java.regex.ast.EscapedCharacterClassTree;
import org.sonar.java.regex.ast.FlagSet;
import org.sonar.java.regex.ast.IndexRange;
import org.sonar.java.regex.ast.JavaCharacter;
import org.sonar.java.regex.ast.LookAroundTree;
import org.sonar.java.regex.ast.MiscEscapeSequenceTree;
import org.sonar.java.regex.ast.NegationState;
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
      RegexSource source = new RegexSource(Collections.singletonList(literalTree));
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
      case "successor":
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
        arrowTail = "odot";
        style = "bold";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "continuation":
        style = "dashed";
        color = "DodgerBlue";
        fontColor = "MediumBlue";
        break;
      case "back-reference":
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

      context.add(new Edge("1", "2", "", "", "transparent"));
      context.add(new Edge("2", "3", "AST hierarchy  ", "", "default"));
      context.add(new Edge("3", "4", "", "", "transparent"));
      context.add(new Edge("4", "5", "", "", "transparent"));
      context.add(new Edge("5", "6", "", "", "transparent"));
      context.add(new Edge("6", "7", "successor ", "", "successor"));
      context.add(new Edge("7", "8", "continuation ", "", "continuation"));
      context.add(new Edge("8", "9", "negation ", "", "negation-successor"));
      context.add(new Edge("9", "10", "backtracking   ", "", "backtracking-successor"));
      context.add(new Edge("10", "11", "reference  ", "", "back-reference"));
    } else {
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
        context.setNodeReference(regexParseResult.getFinalState(), "EndOfRegex");
        context.setNodeReference(regexParseResult.getStartState(), "StartState");

        RegexTree firstNode = regexParseResult.getResult();
        setContinuationAndSuccessor(regexParseResult.getStartState());
        setContinuationAndSuccessor(firstNode);
        setContinuationAndSuccessor(regexParseResult.getFinalState());

        super.visit(regexParseResult);

        //linkBackReferenceToCapturingGroup();

        context.add(new Node("EndOfRegex", "EndOfRegex\n\n\n\n", "end"));
        context.add(new Node("StartState", "StartState\n\n\n\n", "start"));

        successorMap.forEach(this::createSuccessorEdges);
        continuationMap.forEach(this::createContinuationEdge);
      }
    }

    private void setContinuationAndSuccessor(AutomatonState initialState) {
      Deque<AutomatonState> inProgress = new LinkedList<>();
      inProgress.push(initialState);
      while (!inProgress.isEmpty()) {
        AutomatonState state = inProgress.pop();
        String stateReference = context.getNodeReference(state);
        if (stateReference == null) {
          throw new IllegalStateException("Not yet supported: " + state.getClass());
        }
        setContinuationType(state);
        if (state.continuation() != null) {
          String continuationReference = getOrCreateStateReference(state.continuation(), inProgress);
          continuationMap.put(stateReference, continuationReference);
        }
        List<? extends AutomatonState> successors = state.successors();
        // TODO why EndOfLookaroundState.successors() is not null!
        if (!successors.isEmpty() && !(state instanceof EndOfLookaroundState)) {
          List<String> successorReferences = new ArrayList<>();
          for (AutomatonState successor : successors) {
            String successorReference = getOrCreateStateReference(successor, inProgress);
            successorReferences.add(successorReference);
          }
          successorMap.put(stateReference, successorReferences);
        }
      }
    }

    // TODO fix this
    private LookAroundTree getParent(EndOfLookaroundState state) {
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

    private RegexTree getParent(BranchState state) {
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

    private String getOrCreateStateReference(AutomatonState state, Deque<AutomatonState> inProgress) {
      String reference = context.getNodeReference(state);
      if (reference == null) {
        if (state instanceof BranchState) {
          reference = createNewNodeReference(state);
          RegexTree parent = getParent((BranchState) state);
          String name = (parent instanceof RepetitionTree &&
            ((RepetitionTree)parent).getQuantifier().getModifier() == Quantifier.Modifier.POSSESSIVE) ? "Commit:" :  "Branch:";
          context.add(new Node(reference, name + reference, "state"));
          String parentReference = context.getNodeReference(parent);
          context.add(new Edge(reference, parentReference, "parent", "back-reference"));
          inProgress.push(state);
        } else if (state instanceof EndOfLookaroundState) {
          reference = createNewNodeReference(state);
          context.add(new Node(reference, "EndOfLookAround:" + reference + "\n  continuation: null\n", "state"));
          LookAroundTree parent = getParent((EndOfLookaroundState) state);
          String parentReference = context.getNodeReference(parent);
          context.add(new Edge(reference, parentReference, "parent", "back-reference"));
          inProgress.push(state);
        } else if (state instanceof NegationState) {

          // ensure the EndOfLookaroundState is registered before the NegationState
          // EndOfLookaroundState is the continuation.continuation of NegationState
          getOrCreateStateReference(state.continuation().continuation(), inProgress);

          reference = createNewNodeReference(state);
          context.add(new Node(reference, "Negation:" + reference, "state"));
          inProgress.push(state);
        } else {
          throw new IllegalStateException("AutomatonState not yet supported: " + state.getClass());
        }
      }
      return reference;
    }

    private String createNewNodeReference(AutomatonState state) {
      String reference = context.createObjectReference(state);
      context.setNodeReference(state, reference);
      return reference;
    }


    void setContinuationType(AutomatonState state) {
      String reference = context.getNodeReference(state);
      switch (state.incomingTransitionType()) {
        case LOOKAROUND_BACKTRACKING:
          continuationHeadType.put(reference, "backtracking-successor");
          break;
        case NEGATION:
          continuationHeadType.put(reference, "negation-successor");
          break;
        case BACK_REFERENCE:
          continuationHeadType.put(reference, "back-reference");
          break;
      }
    }

    void markAsTreeAndStateNode(RegexTree tree) {
      Node node = context.getNode(context.getNodeReference(tree));
      if (node != null) {
        node.type = "tree-and-state";
      }
      setContinuationType(tree);
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
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.visitSequence(tree);
    }

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.visitDisjunction(tree);
    }

    @Override
    public void visitCapturingGroup(CapturingGroupTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      capturingGroups.add(tree);
      super.visitCapturingGroup(tree);
    }

    @Override
    protected void doVisitNonCapturingGroup(NonCapturingGroupTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.doVisitNonCapturingGroup(tree);
    }

    @Override
    public void visitAtomicGroup(AtomicGroupTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.visitAtomicGroup(tree);
    }

    @Override
    public void visitLookAround(LookAroundTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.visitLookAround(tree);
    }

    class State {
      final String reference;
      final RegexTree tree;
      List<String> successors;
      String continuation;
      String backReference;

      public State(String reference, RegexTree tree) {
        this.reference = reference;
        this.tree = tree;
        this.successors = Collections.emptyList();
        this.continuation = null;
        this.backReference = null;
      }
    }

    State createCommit(String rollbackReference, List<String> successors, String continuation) {
      State commit = new State(context.createReference(), null);
      context.add(new Node(commit.reference, "Commit:" + commit.reference, "state"));
      context.add(new Edge(commit.reference, rollbackReference, "rollback", "back-reference"));
      successorMap.put(commit.reference, successors);
      continuationMap.put(commit.reference, continuation);
      return commit;
    }

    @Override
    public void visitRepetition(RepetitionTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      super.visitRepetition(tree);
    }

    public <T> List<T> concat(List<T> list, T elem) {
      List<T> result = new ArrayList<>(list);
      result.add(elem);
      return result;
    }

    public <T> List<T> concat(T elem, List<T> list) {
      List<T> result = new ArrayList<>();
      result.add(elem);
      result.addAll(list);
      return result;
    }

    @Override
    public void visitBackReference(BackReferenceTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
      backReferenceTrees.add(tree);
    }

    @Override
    public void visitCharacterClass(CharacterClassTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
    }

    @Override
    public void visitPlainCharacter(PlainCharacterTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
    }

    @Override
    public void visitUnicodeCodePoint(UnicodeCodePointTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
    }

    @Override
    public void visitCharacterRange(CharacterRangeTree tree) {
    }

    @Override
    public void visitCharacterClassUnion(CharacterClassUnionTree tree) {
    }

    @Override
    public void visitCharacterClassIntersection(CharacterClassIntersectionTree tree) {
    }

    @Override
    public void visitDot(DotTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
    }

    @Override
    public void visitEscapedCharacterClass(EscapedCharacterClassTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
    }

    @Override
    public void visitBoundary(BoundaryTree boundaryTree) {
    }

    @Override
    public void visitMiscEscapeSequence(MiscEscapeSequenceTree tree) {
      markAsTreeAndStateNode(tree);
      setContinuationAndSuccessor(tree);
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
    return (field.getDeclaringClass().equals(AbstractRegexSyntaxElement.class) && field.getName().equals("source")) ||
      (field.getDeclaringClass().equals(LookAroundTree.class) && field.getName().equals("successors")) ||
      (field.getDeclaringClass().equals(RegexTree.class) && field.getName().equals("continuation"));
  }

}
