package com.auzeill.regex.automaton;

import com.auzeill.regex.automaton.AutomatonState.StateType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.sonar.java.regex.RegexParseResult;
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
import org.sonar.java.regex.ast.LookAroundTree;
import org.sonar.java.regex.ast.LookAroundTree.Polarity;
import org.sonar.java.regex.ast.MiscEscapeSequenceTree;
import org.sonar.java.regex.ast.NonCapturingGroupTree;
import org.sonar.java.regex.ast.PlainCharacterTree;
import org.sonar.java.regex.ast.Quantifier;
import org.sonar.java.regex.ast.RegexBaseVisitor;
import org.sonar.java.regex.ast.RegexTree;
import org.sonar.java.regex.ast.RepetitionTree;
import org.sonar.java.regex.ast.SequenceTree;
import org.sonar.java.regex.ast.UnicodeCodePointTree;

import static org.sonar.java.regex.ast.LookAroundTree.Direction.AHEAD;
import static org.sonar.java.regex.ast.LookAroundTree.Direction.BEHIND;

public class AutomatonStateVisitor extends RegexBaseVisitor {

  @FunctionalInterface
  interface CreationContext {
    AutomatonState createState(StateType type, RegexTree tree);
  }

  AutomatonState startState;
  Deque<CreationContext> contextStack = new LinkedList<>();
  List<AutomatonState> capturingGroups = new ArrayList<>();
  List<AutomatonState> backReferences = new ArrayList<>();

  public static AutomatonState toState(RegexParseResult regexParseResult) {
    AutomatonStateVisitor visitor = new AutomatonStateVisitor();
    visitor.visit(regexParseResult);
    return visitor.startState;
  }

  class StartStateContext implements CreationContext {
    public AutomatonState createState(StateType type, RegexTree tree) {
      AutomatonState newState = new AutomatonState(type, tree, startState.successors(), startState.continuation());
      startState.setSuccessor(newState);
      startState.setContinuation(newState);
      return newState;
    }
  }

  @Override
  public void visit(RegexParseResult regexParseResult) {
    AutomatonState endOfRegex = new AutomatonState(StateType.END_OF_REGEX, null, Collections.emptyList(), null);
    startState = new AutomatonState(StateType.START_STATE, null, Collections.singletonList(endOfRegex), endOfRegex);
    executeIn(new StartStateContext(), () -> super.visit(regexParseResult));
    linkBackReferenceToCapturingGroup();
  }

  static class SequenceContext implements CreationContext {
    final AutomatonState sequence;
    AutomatonState lastChild = null;

    public SequenceContext(AutomatonState sequence) {
      this.sequence = sequence;
    }

    public AutomatonState createState(StateType type, RegexTree tree) {
      AutomatonState newState;
      if (lastChild == null) {
        newState = new AutomatonState(type, tree, sequence.successors(), sequence.continuation());
        sequence.setSuccessor(lastChild);
      } else {
        newState = new AutomatonState(type, tree, lastChild.successors(), sequence.continuation());
        lastChild.setSuccessor(newState);
        lastChild.setContinuation(newState);
      }
      lastChild = newState;
      return newState;
    }
  }

  AutomatonState createState(StateType type, RegexTree tree) {
    return contextStack.peek().createState(type, tree);
  }

  void executeIn(CreationContext context, Runnable runnable) {
    contextStack.push(context);
    runnable.run();
    contextStack.pop();
  }

  @Override
  public void visitSequence(SequenceTree tree) {
    AutomatonState sequence = createState(StateType.EPSILON, tree);
    executeIn(new SequenceContext(sequence), () -> super.visitSequence(tree));
  }

  static class DisjunctionContext implements CreationContext {
    final AutomatonState disjunction;
    List<AutomatonState> disjunctionSuccessors;
    final List<AutomatonState> childSuccessors;

    public DisjunctionContext(AutomatonState disjunction) {
      this.disjunction = disjunction;
      this.childSuccessors = disjunction.successors();
    }

    public AutomatonState createState(StateType type, RegexTree tree) {
      AutomatonState newState = new AutomatonState(type, tree, childSuccessors, disjunction.continuation());
      if (disjunctionSuccessors == null) {
        disjunctionSuccessors = new ArrayList<>();
        disjunction.setSuccessors(disjunctionSuccessors);
      }
      disjunctionSuccessors.add(newState);
      return newState;
    }
  }

  @Override
  public void visitDisjunction(DisjunctionTree tree) {
    AutomatonState disjunction = createState(StateType.EPSILON, tree);
    executeIn(new DisjunctionContext(disjunction), () -> super.visitDisjunction(tree));
  }

  @Override
  public void visitCapturingGroup(CapturingGroupTree tree) {
    AutomatonState group = createState(StateType.EPSILON, tree);
    capturingGroups.add(group);
    executeIn(new SequenceContext(group), () -> super.visitCapturingGroup(tree));
  }

  @Override
  protected void doVisitNonCapturingGroup(NonCapturingGroupTree tree) {
    AutomatonState group = createState(StateType.EPSILON, tree);
    executeIn(new SequenceContext(group), () -> super.doVisitNonCapturingGroup(tree));
  }

  @Override
  public void visitAtomicGroup(AtomicGroupTree tree) {
    // TODO create a PossessiveBranch after the element of an AtomicGroup to prevent backtrack
    AutomatonState group = createState(StateType.EPSILON, tree);
    executeIn(new SequenceContext(group), () -> super.visitAtomicGroup(tree));
  }

  static class LookAroundContext implements CreationContext {
    final AutomatonState lookAround;
    final LookAroundTree lookAroundTree;

    public LookAroundContext(AutomatonState lookAround, LookAroundTree lookAroundTree) {
      this.lookAround = lookAround;
      this.lookAroundTree = lookAroundTree;
    }

    public AutomatonState createState(StateType type, RegexTree tree) {
      StateType endOfLookAroundType = lookAroundTree.getDirection() == AHEAD ? StateType.END_OF_LOOK_AROUND_BACKTRACKING : StateType.END_OF_LOOK_AROUND;
      AutomatonState endOfLookAround = new AutomatonState(endOfLookAroundType, null, Collections.emptyList(), lookAround.continuation());
      endOfLookAround.setReference(lookAround);
      AutomatonState newState = new AutomatonState(type, tree, Collections.singletonList(endOfLookAround), endOfLookAround);
      if (lookAroundTree.getPolarity() == Polarity.NEGATIVE) {
        AutomatonState negation = new AutomatonState(StateType.NEGATION, null, Collections.singletonList(newState), newState);
        lookAround.setSuccessor(negation);
      } else {
        lookAround.setSuccessor(newState);
      }
      return newState;
    }
  }

  @Override
  public void visitLookAround(LookAroundTree tree) {
    StateType type = tree.getDirection() == BEHIND ? StateType.LOOK_AROUND_BACKTRACKING : StateType.EPSILON;
    AutomatonState lookAround = createState(type, tree);
    executeIn(new LookAroundContext(lookAround, tree), () -> super.visitLookAround(tree));
  }

  static class RepetitionContext implements CreationContext {
    final AutomatonState repetition;
    final RepetitionTree repetitionTree;

    public RepetitionContext(AutomatonState repetition, RepetitionTree repetitionTree) {
      this.repetition = repetition;
      this.repetitionTree = repetitionTree;
    }

    public AutomatonState createState(StateType type, RegexTree tree) {
      Quantifier quantifier = repetitionTree.getQuantifier();
      Quantifier.Modifier modifier = quantifier.getModifier();
      boolean isOptional = quantifier.getMinimumRepetitions() == 0;
      boolean noLoop = quantifier.getMaximumRepetitions() != null && quantifier.getMaximumRepetitions() <= 1;
      StateType branchType = (modifier == Quantifier.Modifier.POSSESSIVE ? StateType.BRANCH : StateType.POSSESSIVE_BRANCH);
      List<AutomatonState> branchSuccessors;
      if (noLoop) {
        branchSuccessors = repetition.successors();
      } else {
        branchSuccessors = new ArrayList<>();
        if (!isOptional) {
          branchSuccessors.addAll(repetition.successors());
        }
        if (modifier == Quantifier.Modifier.RELUCTANT) {
          branchSuccessors.add(repetition);
        } else if (modifier == Quantifier.Modifier.POSSESSIVE) {
          branchSuccessors.add(0, repetition);
        } else { // GREEDY
          branchSuccessors.add(0, repetition);
        }
      }
      AutomatonState branch = new AutomatonState(branchType, null, branchSuccessors, repetition.continuation());
      AutomatonState newState = new AutomatonState(type, tree, Collections.singletonList(branch), branch);
      if (isOptional) {
        if (modifier == Quantifier.Modifier.RELUCTANT) {
          repetition.setSuccessors(Arrays.asList(repetition.continuation(), newState));
        } else {
          repetition.setSuccessors(Arrays.asList(newState, repetition.continuation()));
        }
      } else {
        repetition.setSuccessors(Collections.singletonList(newState));
      }
      return newState;
    }
  }

  @Override
  public void visitRepetition(RepetitionTree tree) {
    AutomatonState repetition = createState(StateType.EPSILON, tree);
    executeIn(new RepetitionContext(repetition, tree), () -> super.visitRepetition(tree));
  }

  @Override
  public void visitBackReference(BackReferenceTree tree) {
    AutomatonState backReference = createState(StateType.BACKREFERENCE, tree);
    backReferences.add(backReference);
  }

  @Override
  public void visitCharacterClass(CharacterClassTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitPlainCharacter(PlainCharacterTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitUnicodeCodePoint(UnicodeCodePointTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitCharacterRange(CharacterRangeTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitCharacterClassUnion(CharacterClassUnionTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitCharacterClassIntersection(CharacterClassIntersectionTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitDot(DotTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitEscapedCharacterClass(EscapedCharacterClassTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  @Override
  public void visitBoundary(BoundaryTree boundaryTree) {
    // ignore
  }

  @Override
  public void visitMiscEscapeSequence(MiscEscapeSequenceTree tree) {
    createState(StateType.CHARACTER, tree);
  }

  private void linkBackReferenceToCapturingGroup() {
    for (AutomatonState backReferenceTree : backReferences) {
      AutomatonState capturingGroup = findMatchingGroup(backReferenceTree);
      if (capturingGroup != null) {
        backReferenceTree.setReference(capturingGroup);
      }
    }
  }

  private AutomatonState findMatchingGroup(AutomatonState backReferenceTree) {
    for (AutomatonState capturingGroup : capturingGroups) {
      if (matches(backReferenceTree, capturingGroup)) {
        return capturingGroup;
      }
    }
    return null;
  }

  private boolean matches(AutomatonState backReference, AutomatonState capturingGroup) {
    BackReferenceTree backReferenceTree = (BackReferenceTree) backReference.tree();
    CapturingGroupTree capturingGroupTree = (CapturingGroupTree) capturingGroup.tree();
    if (backReferenceTree.isNumerical()) {
      return capturingGroupTree.getGroupNumber() == backReferenceTree.groupNumber();
    } else {
      return capturingGroupTree.getName().filter(name -> name.equals(backReferenceTree.groupName())).isPresent();
    }
  }

}
