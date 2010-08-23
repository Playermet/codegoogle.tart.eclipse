package org.viridia.tart.eclipse.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.viridia.tart.eclipse.preferences.TartPrefs;

public class TartScanner extends RuleBasedScanner {
  private final TartStyleManager styles;
  
  // Statement keywords
  static final public String[] STATEMENT_KEYWORDS = { "if", "else", "repeat",
      "for", "while", "return", "yield", "throw", "break", "continue",
      "where", "switch", "case", "classify", "as", "try", "catch",
      "finally", "using", "import" };

  // Declaration keywords
  static final public String[] DECL_KEYWORDS = { "namespace", "class",
      "struct", "interface", "protocol", "enum", "let", "var", "def", "undef",
      "override", "fn", "get", "set", "macro", "typealias", "friend" };

  // Operator keywords
  static final public String[] OPERATOR_KEYWORDS = { "and", "or",
      "not", "is", "in", "isa" };
  
  // Built-in type names
  static final public String[] BUILTIN_TYPES = { "bool", "char", "byte",
      "short", "int", "long", "ubyte", "suhort", "uint", "ulong", "float",
      "double", "void" };

  // Access 
  static final public String[] VISIBILITY = { "public", "private",
      "protected", "internal" };

  // Modifiers 
  static final public String[] DECL_MODIFIERS = { "const", "constable",
      "mutable", "readonly", "static", "abstract", "final", "optional" };

  // Special symbols
  static final public String[] SPECIAL_KEYWORDS = { "self", "super", "true",
      "false", "null", "typecast" };
  
  // Detector for keywords
  private static class KeywordDetector implements IWordDetector {
    public boolean isWordPart(final char c) {
      return Character.isLetter(c);
    }

    public boolean isWordStart(final char c) {
      return Character.isLetter(c);
    }
  };

  private static class IdentDetector implements IWordDetector {
    public boolean isWordPart(final char c) {
      return Character.isJavaIdentifierPart(c);
    }

    public boolean isWordStart(final char c) {
      return Character.isJavaIdentifierStart(c);
    }
  };

  // Rule for numeric constants.
  static public class NumberRule implements IRule {
    public final IToken token;
    
    public NumberRule(IToken token) {
      this.token = token;
    }

    public IToken evaluate(ICharacterScanner scanner) {
      int first = scanner.read();
      if (Character.isDigit(first)) {
        int c = scanner.read();
        
        // Check for hex char.
        if (first == 0 && (c == 'x' || c == 'X')) {
          c = scanner.read();
          if (!isHexDigit(c)) {
            // A token that begins with '0x' followed by a non-digit is an error.
            scanner.unread();
            return Token.UNDEFINED;
          }
            
          do {
            c = scanner.read();
          } while (isHexDigit(c) || c == '_');
          
          scanner.unread();
          return Token.UNDEFINED;
        }
        
        while (Character.isDigit(c) || c == '_') {
          c = scanner.read();
        }
        
        if (c == '.') {
          c = scanner.read();
          while (Character.isDigit(c) || c == '_') {
            c = scanner.read();
          }
        }
        
        scanner.unread();
        return token;
      }
      
      scanner.unread();
      return Token.UNDEFINED;
    }
    
    private static boolean isHexDigit(int c) {
      return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
  }
  
  private static class OperatorRule implements IRule {
    private final IToken token;
    
    public OperatorRule(TartStyleManager styles, IToken token) {
      this.token = token;
    }
    
    public IToken evaluate(ICharacterScanner scanner) {
      int c = scanner.read();
      switch (c) {
        case ':':
          c = scanner.read();
          if (c == ':') { return token; }
          scanner.unread();
          return token;

        case '+':
          scanner.read();
          if (c == '+') { return token; }
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '-':
          scanner.read();
          if (c == '-') { return token; }
          if (c == '=') { return token; }
          if (c == '>') { return token; }
          scanner.unread();
          return token;

        case '*':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '%':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '^':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '|':
          scanner.read();
          if (c == '=') { return token; }
          if (c == '|') { return token; }
          scanner.unread();
          return token;

        case '&':
          scanner.read();
          if (c == '=') { return token; }
          if (c == '&') { return token; }
          scanner.unread();
          return token;

        case '~':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '>':
          scanner.read();
          if (c == '=') {
            scanner.read();
            if (c == '?') { return token; }
            scanner.unread();
            return token;
          }
          if (c == '?') { return token; }
          if (c == '>') {
            scanner.read();
            if (c == '=') { return token; }
            scanner.unread();
            return token;
          }
          scanner.unread();
          return token;

        case '<':
          scanner.read();
          if (c == '=') {
            scanner.read();
            if (c == '?') { return token; }
            scanner.unread();
            return token;
          }
          if (c == '?') { return token; }
          if (c == '<') {
            scanner.read();
            if (c == '=') { return token; }
            scanner.unread();
            return token;
          }
          scanner.unread();
          return token;

        case '=':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '!':
          scanner.read();
          if (c == '=') { return token; }
          scanner.unread();
          return token;

        case '{':
        case '}':
        case '[':
        case ']':
        case '(':
        case ')':
          return token;

        case ';':
        case ',':
        case '?':
        case '$':
          return token;
      }

      scanner.unread();
      return Token.UNDEFINED;
    }
  }

  private static class AttributeRule implements IRule {
    private final IToken token;

    public AttributeRule(TartStyleManager styles, IToken token) {
      this.token = token;
    }

    public IToken evaluate(ICharacterScanner scanner) {
      int c = scanner.read();
      if (c == '@') {
        c = scanner.read();
        while (Character.isJavaIdentifierPart(c) || c == '.') {
          c = scanner.read();
        }
        scanner.unread();
        return token;
      }
      scanner.unread();
      return Token.UNDEFINED;
    }
  }
	    
  public TartScanner(TartStyleManager styles) {
    this.styles = styles;
    defineRules();
  }
  
  private void defineRules() {
    setRules(new IRule[] {
        defineWhitespaceRule(),
        defineNumberRule(),
        defineCharLiteralRule(),
        defineStringLiteralRule(),
        defineOperatorRule(),
        defineAttributeRule(),
        defineKeywordRule(),
        defineIdentRule() });
  }
  
  private IRule defineWhitespaceRule() {
    return new WhitespaceRule(new IWhitespaceDetector() {
      public boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
      }});
  }
  
  private IRule defineNumberRule() {
    return new NumberRule(styles.getToken(TartPrefs.TART_NUMBER));
  }

  private IRule defineCharLiteralRule() {
    return new SingleLineRule("'", "'", styles.getToken(TartPrefs.TART_STRING), '\\');
  }

  private IRule defineStringLiteralRule() {
    return new SingleLineRule("\"", "\"", styles.getToken(TartPrefs.TART_STRING), '\\');
  }

  private IRule defineOperatorRule() {
    return new OperatorRule(styles, styles.getToken(TartPrefs.TART_OPERATOR));
  }

  private IRule defineAttributeRule() {
    return new AttributeRule(styles, styles.getToken(TartPrefs.TART_ATTRIBUTE));
  }

  private IRule defineKeywordRule() {
    // Detector for keywords
    IWordDetector keywordDetector = new KeywordDetector();
    WordRule keywordRule = new WordRule(keywordDetector);
    
    addWords(STATEMENT_KEYWORDS, keywordRule, styles.getToken(TartPrefs.TART_STMT_KEYWORD));
    addWords(DECL_KEYWORDS, keywordRule, styles.getToken(TartPrefs.TART_DECL_KEYWORD));
    addWords(OPERATOR_KEYWORDS, keywordRule, styles.getToken(TartPrefs.TART_OPERATOR));
    addWords(VISIBILITY, keywordRule, styles.getToken(TartPrefs.TART_DECL_VISIBILITY));
    addWords(DECL_MODIFIERS, keywordRule, styles.getToken(TartPrefs.TART_DECL_MODIFIER));
    addWords(BUILTIN_TYPES, keywordRule, styles.getToken(TartPrefs.TART_BUILTIN_TYPENAME));
    addWords(SPECIAL_KEYWORDS, keywordRule, styles.getToken(TartPrefs.TART_BUILTIN_SYMBOL));
    return keywordRule;
  }
  
  private void addWords(String[] words, WordRule rule, IToken token) {
    for (String word : words) {
      rule.addWord(word, token);
    }
  }

  IRule defineIdentRule() {
    IToken ident = new Token(styles.getTextStyle(TartPrefs.TART_IDENT));
    return new WordRule(new IdentDetector(), ident);
  }
}
