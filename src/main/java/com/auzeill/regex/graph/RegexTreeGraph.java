package com.auzeill.regex.graph;

import java.lang.reflect.Field;
import java.util.Collections;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.LiteralTreeImpl;
import org.sonar.java.regex.RegexParseResult;
import org.sonar.java.regex.RegexParser;
import org.sonar.java.regex.ast.FlagSet;
import org.sonar.java.regex.ast.IndexRange;
import org.sonar.java.regex.ast.JavaCharacter;
import org.sonar.java.regex.ast.RegexSource;
import org.sonar.java.regex.ast.RegexSyntaxElement;
import org.sonar.java.regex.ast.RegexToken;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

public class RegexTreeGraph extends GraphWriter {

  public static String transform(String stringLiteral) {
    InternalSyntaxToken token = new InternalSyntaxToken(1, 1, stringLiteral, Collections.emptyList(), false);
    LiteralTree literalTree = new LiteralTreeImpl(Kind.STRING_LITERAL, token);
    RegexSource source = new RegexSource(Collections.singletonList(literalTree));
    RegexParser parser = new RegexParser(source, new FlagSet());
    RegexParseResult result = parser.parse();
    if (!result.getSyntaxErrors().isEmpty()) {
      throw new IllegalArgumentException("SyntaxError: " + result.getSyntaxErrors().get(0).getMessage());
    }
    RegexTreeGraph writer = new RegexTreeGraph();
    return writer.graph(result.getResult());
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
