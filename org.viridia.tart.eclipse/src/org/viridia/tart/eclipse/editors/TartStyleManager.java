package org.viridia.tart.eclipse.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.viridia.tart.eclipse.Activator;

public class TartStyleManager {
  private final TartColorManager colorManager;
  private final IPreferenceStore store;
  
  TartStyleManager(TartColorManager colorManager) {
    this.colorManager = colorManager;
    this.store = Activator.getDefault().getPreferenceStore();
  }
  
  public TextAttribute getTextStyle(String name) {
    Color color = colorManager.getColor(PreferenceConverter.getColor(store, name + "_COLOR"));
    int style = 0;
    if (store.getBoolean(name + "_BOLD")) { style |= SWT.BOLD; }
    if (store.getBoolean(name + "_ITALIC")) { style |= SWT.ITALIC; }
    
    return new TextAttribute(color, null, style);
  }
}
