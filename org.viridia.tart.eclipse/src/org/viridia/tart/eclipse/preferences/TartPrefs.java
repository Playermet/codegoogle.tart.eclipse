package org.viridia.tart.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.viridia.tart.eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class TartPrefs extends AbstractPreferenceInitializer {

  /** Represents a syntax coloring style definition. */
  public static class SyntaxElement {
    private final String name;
    private final String caption;
    private final RGB defaultColor;
    private final int defaultStyle;

    public SyntaxElement(String name, String caption, RGB defaultColor, int defaultStyle) {
      this.name = name;
      this.caption = caption;
      this.defaultColor = defaultColor;
      this.defaultStyle = defaultStyle;
    }

    public String getName() {
      return name;
    }

    public String getCaption() {
      return caption;
    }

    public RGB getDefaultColor() {
      return defaultColor;
    }

    public int getDefaultStyle() {
      return defaultStyle;
    }
  }

  // text style preferences variables
  public static final String TART_SINGLE_LINE_COMMENT = "TART_SINGLE_LINE_COMMENT";
  public static final String TART_MULTI_LINE_COMMENT = "TART_MULTI_LINE_COMMENT";
  public static final String TART_DOC_COMMENT = "TART_DOC_COMMENT";
  public static final String TART_STMT_KEYWORD = "TART_STMT_KEYWORD";
  public static final String TART_DECL_KEYWORD = "TART_DECL_KEYWORD";
  public static final String TART_DECL_VISIBILITY = "TART_DECL_VISIBILITY";
  public static final String TART_DECL_MODIFIER = "TART_MODIFIER";
  public static final String TART_BUILTIN_TYPENAME = "TART_BUILTIN_TYPENAME";
  public static final String TART_BUILTIN_SYMBOL = "TART_BUILTIN_SYMBOL";
  public static final String TART_STRING = "TART_STRING";
  public static final String TART_NUMBER = "TART_NUMBER";
  public static final String TART_IDENT = "TART_IDENT";
  public static final String TART_OPERATOR = "TART_OPERATOR";
  public static final String TART_OPERATOR_KEYWORD = "TART_OPERATOR_KEYWORD";
  public static final String TART_BRACE = "TART_BRACE";
  public static final String TART_ATTRIBUTE = "TART_ATTRIBUTE";
  public static final String TART_DEFAULT = "TART_DEFAULT";

  // Print margin
  /** @since 0.1 */
  public static final String TART_PRINT_MARGIN = "tartPrintMargin";
  /** @since 0.1 */
  public static final String TART_PRINT_MARGIN_COLOR = "tartPrintMarginColor";
  /** @since 0.1 */
  public static final String TART_PRINT_MARGIN_COLUMN = "tartPrintMarginColumn";

  // Other preferences
  public static final String REMOVE_TRAILING_WHITESPACE = "removeTrailingWhitespace";
  public static final String ENSURE_NEWLINE_AT_EOF = "ensureNewlineAtEOF";

  public static final String EDITOR_SUB_WORD_NAVIGATION = "EDITOR_SUB_WORD_NAVIGATION";

  public static SyntaxElement[] SYNTAX_ELEMENTS = new SyntaxElement[] {
    new SyntaxElement(TART_DEFAULT, "Default Text Style", new RGB(0, 128, 0), 0),
    new SyntaxElement(TART_SINGLE_LINE_COMMENT, "Single line comment", new RGB(64, 128, 64), SWT.ITALIC),
    new SyntaxElement(TART_MULTI_LINE_COMMENT, "Multi-line comment", new RGB(64, 128, 64), SWT.BOLD),
    new SyntaxElement(TART_DOC_COMMENT, "Doc comment", new RGB(0, 128, 0), SWT.ITALIC),
    new SyntaxElement(TART_STMT_KEYWORD, "Statement keyword", new RGB(0, 0, 128), SWT.BOLD),
    new SyntaxElement(TART_DECL_KEYWORD, "Declaration keyword", new RGB(0, 0, 128), SWT.BOLD),
    new SyntaxElement(TART_DECL_VISIBILITY, "Declaration visibility", new RGB(0, 0, 128), SWT.BOLD),
    new SyntaxElement(TART_DECL_MODIFIER, "Declaration modifier", new RGB(0, 0, 128), SWT.BOLD),
    new SyntaxElement(TART_BUILTIN_TYPENAME, "Built-in type name", new RGB(64, 0, 64), SWT.BOLD),
    new SyntaxElement(TART_BUILTIN_SYMBOL, "Built-in variable name", new RGB(128, 0, 192), SWT.BOLD),
    new SyntaxElement(TART_STRING, "String literal", new RGB(0, 128, 0), 0),
    new SyntaxElement(TART_NUMBER, "Numeric constant", new RGB(64, 0, 128), 0),
    new SyntaxElement(TART_IDENT, "Identifier", new RGB(0, 0, 0), 0),
    new SyntaxElement(TART_OPERATOR, "Operator", new RGB(0, 0, 0), 0),
    new SyntaxElement(TART_OPERATOR_KEYWORD, "Operator Keyword", new RGB(0, 0, 128), SWT.BOLD),
    new SyntaxElement(TART_BRACE, "Braces", new RGB(0, 128, 0), 0),
    new SyntaxElement(TART_ATTRIBUTE, "Attributes", new RGB(64, 128, 0), 0)
  };

  /**
   * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
   * initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    for (SyntaxElement token : SYNTAX_ELEMENTS) {
      PreferenceConverter.setDefault(store, token.getName() + "_COLOR", token.getDefaultColor());
      store.setDefault(token.getName() + "_BOLD", (token.getDefaultStyle() & SWT.BOLD) != 0);
      store.setDefault(token.getName() + "_ITALIC", (token.getDefaultStyle() & SWT.ITALIC) != 0);
    }

    store.setDefault(EDITOR_SUB_WORD_NAVIGATION, true);
    store.setDefault(REMOVE_TRAILING_WHITESPACE, true);
    store.setDefault(ENSURE_NEWLINE_AT_EOF, true);
    store.setDefault(TART_PRINT_MARGIN, true);
    PreferenceConverter.setDefault(store, TART_PRINT_MARGIN_COLOR, new RGB(200, 200, 200));
    store.setDefault(TART_PRINT_MARGIN_COLUMN, 100);
  }
}
