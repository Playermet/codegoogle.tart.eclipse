package org.viridia.tart.eclipse.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.viridia.tart.eclipse.Activator;
import org.viridia.tart.eclipse.preferences.TartPrefs.SyntaxElement;

public class SyntaxColoringPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {
  
  private Tree syntaxColorList;
  private ColorSelector colorSelector;
  private Button boldStyle;
  private Button italicStyle;
  
  private static class StylePreference {
    private RGB color;
    private boolean bold;
    private boolean italic;
    private final String styleName;
    
    public StylePreference(String styleName) {
      this.styleName = styleName;
    }
    
    public void loadDefault(IPreferenceStore store) {
      color = PreferenceConverter.getDefaultColor(store, styleName + "_COLOR");
      bold = store.getDefaultBoolean(styleName + "_BOLD");
      italic = store.getDefaultBoolean(styleName + "_ITALIC");
    }

    public void load(IPreferenceStore store) {
      color = PreferenceConverter.getColor(store, styleName + "_COLOR");
      bold = store.getBoolean(styleName + "_BOLD");
      italic = store.getBoolean(styleName + "_ITALIC");
    }

    public void store(IPreferenceStore store) {
      PreferenceConverter.setValue(store, styleName + "_COLOR", color);
      store.setValue(styleName + "_BOLD", bold);
      store.setValue(styleName + "_ITALIC", italic);
    }
    
    public int getStyle() {
      return (bold ? SWT.BOLD : 0) | (italic ? SWT.ITALIC : 0); 
    }

    public boolean isBold() { return bold; }
    public void setBold(boolean bold) {
      this.bold = bold;
    }

    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) {
      this.italic = italic;
    }

    public RGB getColor() { return color; }
    public void setColor(RGB color) { this.color = color; }
  }
  
  public SyntaxColoringPreferencePage() {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription("Syntax Coloring");
  }

  protected Control createContents(Composite parent) {
    IPreferenceStore store = getPreferenceStore();
    Composite top = new Composite(parent, SWT.LEFT);    

    RowLayout rowLayout = new RowLayout();
    rowLayout.wrap = false;
    rowLayout.pack = false;
    rowLayout.justify = true;
    rowLayout.type = SWT.HORIZONTAL;
    rowLayout.marginLeft = 0;
    rowLayout.marginTop = 0;
    rowLayout.marginRight = 0;
    rowLayout.marginBottom = 0;
    rowLayout.spacing = 10;
    //shell.setLayout(rowLayout);
    
    // List of syntax colors
    syntaxColorList = new Tree(parent, SWT.SINGLE | SWT.BORDER);
    TreeColumn col = new TreeColumn(syntaxColorList, SWT.LEFT);
    col.setWidth(200);
    syntaxColorList.setItemCount(TartPrefs.SYNTAX_ELEMENTS.length);
    for (int i = 0; i < TartPrefs.SYNTAX_ELEMENTS.length; ++i) {
      TreeItem ti = syntaxColorList.getItem(i);
      SyntaxElement element = TartPrefs.SYNTAX_ELEMENTS[i];
      StylePreference stylePref = new StylePreference(element.getName());
      stylePref.load(store);

      ti.setText(element.getCaption());
      ti.setData(stylePref);
      updateTreeItemStyle(ti, stylePref);
    }
    
    syntaxColorList.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent ev) {
        TreeItem item = getSelectedItem();
        if (item != null) {
          selectStyle((StylePreference) item.getData());
        }
      }
      
      public void widgetDefaultSelected(SelectionEvent ev) {}
    });

    colorSelector = new ColorSelector(parent);
    colorSelector.addListener(new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        TreeItem item = getSelectedItem();
        if (item != null) {
          StylePreference stylePref = (StylePreference) item.getData();
          stylePref.setColor(colorSelector.getColorValue());
          updateTreeItemStyle(item, stylePref);
        }
      }
    });

    boldStyle = new Button(parent, SWT.CHECK);
    boldStyle.setText("Bold");
    boldStyle.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent ev) {
        TreeItem item = getSelectedItem();
        if (item != null) {
          StylePreference stylePref = (StylePreference) item.getData();
          stylePref.setBold(boldStyle.getSelection());
          updateTreeItemStyle(item, stylePref);
        }
      }
      
      public void widgetDefaultSelected(SelectionEvent ev) {}
    });

    italicStyle = new Button(parent, SWT.CHECK);
    italicStyle.setText("Italic");
    italicStyle.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent ev) {
        TreeItem item = getSelectedItem();
        if (item != null) {
          StylePreference stylePref = (StylePreference) item.getData();
          stylePref.setItalic(italicStyle.getSelection());
          updateTreeItemStyle(item, stylePref);
        }
      }
      
      public void widgetDefaultSelected(SelectionEvent ev) {}
    });
    
    syntaxColorList.select(syntaxColorList.getItem(0));
    selectStyle((StylePreference) syntaxColorList.getItem(0).getData());

    return top;
  }
  
  private TreeItem getSelectedItem() {
    if (syntaxColorList.getSelectionCount() > 0) {
      return syntaxColorList.getSelection()[0];
    }
    
    return null;
  }
  
  private void selectStyle(StylePreference stylePref) {
    colorSelector.setColorValue(stylePref.getColor());
    boldStyle.setSelection(stylePref.isBold());
    italicStyle.setSelection(stylePref.isItalic());
  }
  
  private void updateTreeItemStyle(TreeItem item, StylePreference stylePref) {
    Display display = Display.getCurrent();
    Font defaultFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
    item.setForeground(new Color(display, stylePref.getColor()));
    Font font = new Font(display, defaultFont.getFontData()[0].getName(), 11, stylePref.getStyle());
    item.setFont(font);
  }

  protected void performDefaults() {
    super.performDefaults();
    IPreferenceStore store = getPreferenceStore();
    int numTreeElements = syntaxColorList.getItemCount();
    for (int i = 0; i < numTreeElements; ++i) {
      TreeItem ti = syntaxColorList.getItem(i);
      StylePreference stylePref = (StylePreference) ti.getData();
      stylePref.loadDefault(store);
      updateTreeItemStyle(ti, stylePref);
    }
  }

  public boolean performOk() {
    IPreferenceStore store = getPreferenceStore();
    int numTreeElements = syntaxColorList.getItemCount();
    for (int i = 0; i < numTreeElements; ++i) {
      TreeItem ti = syntaxColorList.getItem(i);
      StylePreference stylePref = (StylePreference) ti.getData();
      stylePref.store(store);
    }

    return super.performOk();
  }

  public void performApply() {
    IPreferenceStore store = getPreferenceStore();
    int numTreeElements = syntaxColorList.getItemCount();
    for (int i = 0; i < numTreeElements; ++i) {
      TreeItem ti = syntaxColorList.getItem(i);
      StylePreference stylePref = (StylePreference) ti.getData();
      stylePref.store(store);
    }

    super.performApply();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }
}
