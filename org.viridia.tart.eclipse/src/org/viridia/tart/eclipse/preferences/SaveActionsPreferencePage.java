package org.viridia.tart.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SaveActionsPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

  private BooleanFieldEditor removeTrailingWhitespace;
  private BooleanFieldEditor ensureNewlineAtEof;

  public SaveActionsPreferencePage() {
    setDescription("Save Actions");
  }

  public void init(IWorkbench workbench) {
  }

  @Override
  protected void createFieldEditors() {
    removeTrailingWhitespace = new BooleanFieldEditor(
        TartPrefs.REMOVE_TRAILING_WHITESPACE, "Remove trailing whitespace", getFieldEditorParent());
    addField(removeTrailingWhitespace);

    ensureNewlineAtEof = new BooleanFieldEditor(
        TartPrefs.ENSURE_NEWLINE_AT_EOF, "Ensure newline at EOF", getFieldEditorParent());
    addField(ensureNewlineAtEof);
  }
}
