package org.viridia.tart.eclipse.editors;

import java.text.BreakIterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextNavigationAction;
import org.viridia.tart.eclipse.Activator;
import org.viridia.tart.eclipse.preferences.TartPrefs;

public class TartSourceEditor extends TextEditor {
  private final TartColorManager colorManager;
  private final TartStyleManager styleManager;

  /**
   * Preference key for print margin ruler color.
   */
  private static final String PRINT_MARGIN_COLOR =
      AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;

  public TartSourceEditor() {
    super();
    colorManager = new TartColorManager();
    styleManager = new TartStyleManager(colorManager);
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setSourceViewerConfiguration(new TartSourceViewerConfiguration(styleManager));
    setDocumentProvider(new TartDocumentProvider());
    setRangeIndicator(new DefaultRangeIndicator()); // enables standard
  }

  @Override
  protected ISourceViewer createSourceViewer(Composite parent,
      IVerticalRuler ruler, int styles) {
    ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);
    if (viewer instanceof ISourceViewerExtension) {
      ((ISourceViewerExtension) viewer).showAnnotationsOverview(true);
    }
    return viewer;
  }

  @Override
  protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
    super.configureSourceViewerDecorationSupport(support);
	support.setMarginPainterPreferenceKeys(
	    TartPrefs.TART_PRINT_MARGIN,
	    TartPrefs.TART_PRINT_MARGIN_COLOR,
		TartPrefs.TART_PRINT_MARGIN_COLUMN);
  }

  @Override
  protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
    String property = event.getProperty();
    if (property.startsWith("TART_")) {
      SourceViewerConfiguration configuration = getSourceViewerConfiguration();
      if (configuration instanceof TartSourceViewerConfiguration) {
        ((TartSourceViewerConfiguration) configuration).handlePropertyChangedEvent(event);
      }
    }

    super.handlePreferenceStoreChanged(event);
  }

  @Override
  protected boolean affectsTextPresentation(PropertyChangeEvent event) {
    String property = event.getProperty();
    if (property.startsWith("TART_")) {
      return true;
    }

    return super.affectsTextPresentation(event);
  }

  @Override
  protected void createNavigationActions() {
    super.createNavigationActions();

    final StyledText textWidget = getSourceViewer().getTextWidget();

    IAction action = new NavigatePreviousSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
    setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

    action = new NavigateNextSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
    setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

    action = new SelectPreviousSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
    setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT, SWT.NULL);

    action = new SelectNextSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
    setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT, SWT.NULL);

    action = new DeletePreviousSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
    setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.BS, SWT.NULL);
    markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, true);

    action = new DeleteNextSubWordAction();
    action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
    setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
    textWidget.setKeyBinding(SWT.CTRL | SWT.DEL, SWT.NULL);
    markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, true);
  }

  @Override
  public void dispose() {
    //Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    colorManager.dispose();
    super.dispose();
  }

  @Override
  protected boolean isTabsToSpacesConversionEnabled() {
    return true;
  }

  @Override
  public void doSave(IProgressMonitor progressMonitor) {
    if (getPreferenceStore().getBoolean(TartPrefs.REMOVE_TRAILING_WHITESPACE)) {
      removeTrailingWhitespace();
    }
    if (getPreferenceStore().getBoolean(TartPrefs.ENSURE_NEWLINE_AT_EOF)) {
      // TODO: Implement
    }
    super.doSave(progressMonitor);
  }

  public void removeTrailingWhitespace() {
    if (isEditable()) {
      IFindReplaceTarget findReplaceTarget =
          (IFindReplaceTarget) getAdapter(IFindReplaceTarget.class);
      if (findReplaceTarget instanceof IFindReplaceTargetExtension3) {
        IFindReplaceTargetExtension3 findReplaceTargetExtension3 =
            (IFindReplaceTargetExtension3) findReplaceTarget;
        try {
          IFindReplaceTargetExtension findReplaceTargetExtension =
              (IFindReplaceTargetExtension) findReplaceTarget;
          rememberSelection();
          findReplaceTargetExtension.beginSession();
          int offset = 0;
          while (offset != -1) {
            offset = findReplaceTargetExtension3.findAndSelect(
                offset, "[\\t ]+$", true, false, false, true);
            if (offset == -1) {
              break;
            }
            findReplaceTargetExtension3.replaceSelection("", false);
          }
        } finally {
          IFindReplaceTargetExtension findReplaceTargetExtension =
              (IFindReplaceTargetExtension) findReplaceTarget;
          findReplaceTargetExtension.endSession();
          restoreSelection();
        }
      }
    }
  }

  //public void propertyChange(PropertyChangeEvent event) {
  //  setSourceViewerConfiguration(new TartSourceViewerConfiguration(styleManager));
  //}

  private static boolean isTartWordBreak(char prevCh, char nextCh) {
    if (nextCh == '\n') {
      // Always a word break before a LF unless it's a CRLF.
      return prevCh != '\r';
    } else if (prevCh == '\n' || nextCh == '\r') {
      // Always a word break after an LF or before a CR.
      return true;
    } else if (Character.isJavaIdentifierPart(prevCh)) {
      if (Character.isJavaIdentifierPart(nextCh)) {
        // CamelCase word break.
        return Character.isLowerCase(prevCh) && Character.isUpperCase(nextCh);
      } else if (Character.isWhitespace(nextCh)) {
        // WS after ident is not a word break.
        return false;
      } else {
        // Anything else after ident is a word break.
        return true;
      }
    } else if (Character.isWhitespace(prevCh)) {
      // WS followed by non-WS is a word break.
      return !Character.isWhitespace(nextCh);
    } else {
      // Punctuation followed by punctuation or WS is not a word break,
      // followed by anything else is.
      return Character.isJavaIdentifierPart(nextCh);
    }
  }

  /**
   * Text navigation action to navigate to the next sub-word.
   */
  protected abstract class NextSubWordAction extends TextNavigationAction {

    /**
     * Creates a new next sub-word action.
     *
     * @param code Action code for the default operation. Must be an action code from
     *      @see org.eclipse.swt.custom.ST.
     */
    protected NextSubWordAction(int code) {
      super(getSourceViewer().getTextWidget(), code);
    }

    @Override
    public void run() {
      // Check whether we are in a Tart code partition and the preference is enabled
      //final IPreferenceStore store = getPreferenceStore();
      //if (!store.getBoolean(TartPrefs.EDITOR_SUB_WORD_NAVIGATION)) {
      //  super.run();
      //  return;
      //}

      final ISourceViewer viewer = getSourceViewer();
      int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
      if (position == -1) {
        return;
      }

      int next = findNextPosition(position);
      if (next != BreakIterator.DONE) {
        setCaretPosition(next);
        getTextWidget().showSelection();
        fireSelectionChanged();
      }
    }

    /**
     * Finds the next position after the given position.
     *
     * @param position the current position
     * @return the next position
     */
    protected int findNextPosition(int position) {
      final ISourceViewer viewer = getSourceViewer();
      final IDocument document = viewer.getDocument();
      int length = document.getLength();
      if (position >= length - 1) {
        return length;
      }

      try {
        char ch = document.getChar(position);
        while (++position < length) {
          char nextCh = document.getChar(position);
          if (isTartWordBreak(ch, nextCh)) {
            break;
          }

          ch = nextCh;
        }

      } catch (BadLocationException e) {
        return length;
      }

      return position;
    }

    /**
     * Sets the caret position to the sub-word boundary given with
     * <code>position</code>.
     *
     * @param position Position where the action should move the caret
     */
    protected abstract void setCaretPosition(int position);
  }

  /**
   * Text navigation action to navigate to the next sub-word.
   */
  protected class NavigateNextSubWordAction extends NextSubWordAction {

    public NavigateNextSubWordAction() {
      super(ST.WORD_NEXT);
    }

    @Override
    protected void setCaretPosition(final int position) {
      getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
    }
  }

  /**
   * Text operation action to delete the next sub-word.
   */
  protected class DeleteNextSubWordAction extends NextSubWordAction implements IUpdate {

    public DeleteNextSubWordAction() {
      super(ST.DELETE_WORD_NEXT);
    }

    @Override
    protected void setCaretPosition(final int position) {
      if (!validateEditorInputState()) {
        return;
      }

      final ISourceViewer viewer = getSourceViewer();
      final int caret, length;
      final Point selection = viewer.getSelectedRange();
      if (selection.y != 0) {
        caret = selection.x;
        length = selection.y;
      } else {
        caret = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
        length = position - caret;
      }

      try {
        viewer.getDocument().replace(caret, length, "");
      } catch (BadLocationException exception) {
        // Should not happen
      }
    }

    //protected int findNextPosition(int position) {
    //  return fIterator.following(position);
    //}

    public void update() {
      setEnabled(isEditorInputModifiable());
    }
  }

  /**
   * Text operation action to select the next sub-word.
   */
  protected class SelectNextSubWordAction extends NextSubWordAction {

    public SelectNextSubWordAction() {
      super(ST.SELECT_WORD_NEXT);
    }

    @Override
    protected void setCaretPosition(final int position) {
      final ISourceViewer viewer = getSourceViewer();
      final StyledText text = viewer.getTextWidget();
      if (text != null && !text.isDisposed()) {
        final Point selection = text.getSelection();
        final int caret = text.getCaretOffset();
        final int offset = modelOffset2WidgetOffset(viewer, position);

        if (caret == selection.x) {
          text.setSelectionRange(selection.y, offset - selection.y);
        } else {
          text.setSelectionRange(selection.x, offset - selection.x);
        }
      }
    }
  }

  /**
   * Text navigation action to navigate to the previous sub-word.
   */
  protected abstract class PreviousSubWordAction extends TextNavigationAction {

    /**
     * Creates a new previous sub-word action.
     *
     * @param code Action code for the default operation. Must be an action code from
     *      @see org.eclipse.swt.custom.ST.
     */
    protected PreviousSubWordAction(final int code) {
      super(getSourceViewer().getTextWidget(), code);
    }

    @Override
    public void run() {
      // Check whether we are in a Tart code partition and the preference is enabled
      /*final IPreferenceStore store = getPreferenceStore();
      if (!store.getBoolean(TartPrefs.EDITOR_SUB_WORD_NAVIGATION)) {
        super.run();
        return;
      }*/

      final ISourceViewer viewer = getSourceViewer();
      int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
      if (position == -1) {
        return;
      }

      int previous = findPreviousPosition(position);
      if (previous != BreakIterator.DONE) {
        setCaretPosition(previous);
        getTextWidget().showSelection();
        fireSelectionChanged();
      }
    }

    /**
     * Finds the previous position before the given position.
     *
     * @param position the current position
     * @return the previous position
     */
    protected int findPreviousPosition(int position) {
      ISourceViewer viewer = getSourceViewer();
      final IDocument document = viewer.getDocument();
      --position;
      if (position <= 1) {
        return 0;
      }

      try {
        char ch = document.getChar(position);
        while (position > 1) {
          char prevCh = document.getChar(position - 1);
          if (isTartWordBreak(prevCh, ch)) {
            break;
          }

          ch = prevCh;
          --position;
        }
      } catch (BadLocationException e) {
        return 0;
      }

      return position;
    }

    /**
     * Sets the caret position to the sub-word boundary given with
     * <code>position</code>.
     *
     * @param position Position where the action should move the caret
     */
    protected abstract void setCaretPosition(int position);
  }

  /**
   * Text navigation action to navigate to the previous sub-word.
   */
  protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {

    public NavigatePreviousSubWordAction() {
      super(ST.WORD_PREVIOUS);
    }

    @Override
    protected void setCaretPosition(final int position) {
      getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
    }
  }

  /**
   * Text operation action to delete the previous sub-word.
   */
  protected class DeletePreviousSubWordAction extends PreviousSubWordAction implements IUpdate {

    public DeletePreviousSubWordAction() {
      super(ST.DELETE_WORD_PREVIOUS);
    }

    @Override
    protected void setCaretPosition(int position) {
      if (!validateEditorInputState())
        return;

      final int length;
      final ISourceViewer viewer = getSourceViewer();
      Point selection = viewer.getSelectedRange();
      if (selection.y != 0) {
        position = selection.x;
        length = selection.y;
      } else {
        length = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset())
            - position;
      }

      try {
        viewer.getDocument().replace(position, length, "");
      } catch (BadLocationException exception) {
        // Should not happen
      }
    }

    /*protected int findPreviousPosition(int position) {
      return fIterator.preceding(position);
    }*/

    public void update() {
      setEnabled(isEditorInputModifiable());
    }
  }

  /**
   * Text operation action to select the previous sub-word.
   */
  protected class SelectPreviousSubWordAction extends PreviousSubWordAction {

    public SelectPreviousSubWordAction() {
      super(ST.SELECT_WORD_PREVIOUS);
    }

    @Override
    protected void setCaretPosition(final int position) {
      final ISourceViewer viewer = getSourceViewer();
      final StyledText text = viewer.getTextWidget();
      if (text != null && !text.isDisposed()) {
        final Point selection = text.getSelection();
        final int caret = text.getCaretOffset();
        final int offset = modelOffset2WidgetOffset(viewer, position);

        if (caret == selection.x) {
          text.setSelectionRange(selection.y, offset - selection.y);
        } else {
          text.setSelectionRange(selection.x, offset - selection.x);
        }
      }
    }
  }
}
