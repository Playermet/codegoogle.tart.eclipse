package org.viridia.tart.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class TartPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor showPrintMargin;
  private IntegerFieldEditor printMarginColumn;

  public TartPreferencePage() {
    setDescription("Tart preferenced");
  }

  public void init(IWorkbench workbench) {
  }

  @Override
  protected void createFieldEditors() {
    showPrintMargin = new BooleanFieldEditor(
        TartPrefs.TART_PRINT_MARGIN, "Show print margin", getFieldEditorParent());
    addField(showPrintMargin);

    printMarginColumn = new IntegerFieldEditor(
        TartPrefs.TART_PRINT_MARGIN_COLUMN, "Print margin column:", getFieldEditorParent(), 3);
    printMarginColumn.setValidRange(1, 200);
    addField(printMarginColumn);
  }
}
