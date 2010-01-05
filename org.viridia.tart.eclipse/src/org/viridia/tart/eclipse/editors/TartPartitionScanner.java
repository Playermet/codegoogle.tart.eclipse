package org.viridia.tart.eclipse.editors;

import org.eclipse.jface.text.rules.*;

public class TartPartitionScanner extends RuleBasedPartitionScanner {
  public final static String TART_MULTI_LINE_COMMENT = "__tart_single_line_comment";
  public final static String TART_SINGLE_LINE_COMMENT = "__tart_multi_line_comment";
  public final static String TART_DOC_COMMENT = "__tart_doc_comment";

  private static IToken multiLineComment = new Token(TART_MULTI_LINE_COMMENT);
  private static IToken singleLineComment = new Token(TART_SINGLE_LINE_COMMENT);
  private static IToken docComment = new Token(TART_DOC_COMMENT);

  public TartPartitionScanner() {
    setPredicateRules(new IPredicateRule[] {
        new MultiLineRule("/**", "*/", docComment),
        new MultiLineRule("/*", "*/", multiLineComment),
        new EndOfLineRule("///", docComment),
        new EndOfLineRule("//", singleLineComment),
    });
  }
}
