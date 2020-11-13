package com.auzeill.regex.automaton;

import java.util.Collections;
import java.util.List;
import org.sonar.java.regex.ast.RegexSyntaxElement;

public class AutomatonState {

  enum StateType {
    // RegexTree states
    EPSILON("Epsilon"),
    CHARACTER("Character"),
    BACKREFERENCE("Backreference"),
    LOOK_AROUND_BACKTRACKING("LookAroundBacktracking"),
    // Non RegexTree additional states
    START_STATE("StartState"),
    BRANCH("Branch"),
    POSSESSIVE_BRANCH("PossessiveBranch"),
    END_OF_REGEX("EndOfRegex"),
    END_OF_LOOK_AROUND("EndOfLookAround"),
    END_OF_LOOK_AROUND_BACKTRACKING("EndOfLookAroundBacktracking"),
    NEGATION("Negation");
    private final String value;
    StateType(String value) {
      this.value = value;
    }
    @Override
    public String toString() {
      return value;
    }
  }

  private final StateType stateType;
  private final RegexSyntaxElement tree;
  private List<AutomatonState> successors;
  private AutomatonState continuation;
  private AutomatonState reference;

  public AutomatonState(StateType stateType, RegexSyntaxElement tree) {
    this(stateType, tree, Collections.emptyList(), null);
  }

  public AutomatonState(StateType stateType, RegexSyntaxElement tree, List<AutomatonState> successors, AutomatonState continuation) {
    this.stateType = stateType;
    this.tree = tree;
    this.successors = successors;
    this.continuation = continuation;
  }

  public void setContinuation(AutomatonState continuation) {
    this.continuation = continuation;
  }

  public void setSuccessors(List<AutomatonState> successors) {
    this.successors = successors;
  }

  public void setSuccessor(AutomatonState successor) {
    this.successors = Collections.singletonList(successor);
  }

  public void setReference(AutomatonState reference) {
    this.reference = reference;
  }

  public StateType stateType() {
    return stateType;
  }

  public RegexSyntaxElement tree() {
    return tree;
  }

  public AutomatonState continuation() {
    return continuation;
  }

  public List<AutomatonState> successors() {
    return successors;
  }

  public AutomatonState reference() {
    return reference;
  }

  public String referenceName() {
    if (stateType == StateType.END_OF_LOOK_AROUND) {
      return "parent";
    } else if (stateType == StateType.POSSESSIVE_BRANCH) {
      return "possessive";
    } else if (stateType == StateType.BACKREFERENCE) {
      return "reference";
    } else {
      return "";
    }
  }
}
