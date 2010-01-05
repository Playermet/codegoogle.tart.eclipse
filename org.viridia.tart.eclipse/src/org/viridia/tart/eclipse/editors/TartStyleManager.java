package org.viridia.tart.eclipse.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.viridia.tart.eclipse.Activator;

public class TartStyleManager {
  private final TartColorManager colorManager;
  private final IPreferenceStore store;
  private final Map<String, Token> tokens = new HashMap<String, Token>();
  
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
  
  public IToken getToken(String styleName) {
    TextAttribute textStyle = getTextStyle(styleName);
    Token token = new Token(textStyle);
    tokens.put(styleName, token);
    return token;
  }
  
  public void adaptToColorChange(PropertyChangeEvent event) {
    for (Map.Entry<String, Token> entry : tokens.entrySet()) {
      TextAttribute textStyle = getTextStyle(entry.getKey());
      Token token = entry.getValue();
      token.setData(textStyle);
    }
  }
}
