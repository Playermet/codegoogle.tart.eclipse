package org.viridia.tart.eclipse.editors;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.viridia.tart.eclipse.Activator;
import org.viridia.tart.eclipse.preferences.TartPrefs;

public class TartSourceViewerConfiguration extends TextSourceViewerConfiguration {
  private TartDoubleClickStrategy doubleClickStrategy;
  private TartScanner tartScanner;
  private TartStyleManager styleManager;

  public TartSourceViewerConfiguration(TartStyleManager styleManager) {
    super(Activator.getDefault().getPreferenceStore());
    this.styleManager = styleManager;
  }

  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] {
        IDocument.DEFAULT_CONTENT_TYPE,
        TartPartitionScanner.TART_MULTI_LINE_COMMENT,
        TartPartitionScanner.TART_SINGLE_LINE_COMMENT,
        TartPartitionScanner.TART_DOC_COMMENT };
  }

  public ITextDoubleClickStrategy getDoubleClickStrategy(
      ISourceViewer sourceViewer, String contentType) {
    if (doubleClickStrategy == null)
      doubleClickStrategy = new TartDoubleClickStrategy();
    return doubleClickStrategy;
  }

  protected TartScanner getTartScanner() {
    if (tartScanner == null) {
      tartScanner = new TartScanner(styleManager);
      tartScanner.setDefaultReturnToken(
          new Token(styleManager.getTextStyle(TartPrefs.TART_DEFAULT)));
    }
    
    return tartScanner;
  }

  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();
    registerScannerForContentType(reconciler, TartPrefs.TART_MULTI_LINE_COMMENT,
        TartPartitionScanner.TART_MULTI_LINE_COMMENT);
    registerScannerForContentType(reconciler, TartPrefs.TART_DOC_COMMENT,
        TartPartitionScanner.TART_DOC_COMMENT);
    registerScannerForContentType(reconciler, TartPrefs.TART_SINGLE_LINE_COMMENT,
        TartPartitionScanner.TART_SINGLE_LINE_COMMENT);
    registerScannerForContentType(reconciler, getTartScanner(), IDocument.DEFAULT_CONTENT_TYPE);
    return reconciler;
  }

  private void registerScannerForContentType(PresentationReconciler reconciler,
      ITokenScanner scanner, String contentType) {
    DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(scanner);
    reconciler.setDamager(damagerRepairer, contentType);
    reconciler.setRepairer(damagerRepairer, contentType);
  }
  
  private void registerScannerForContentType(PresentationReconciler reconciler,
      String styleName, String contentType) {
    TextAttribute textAttribute = styleManager.getTextStyle(styleName);
    NonRuleBasedDamagerRepairer damagerRepairer = new NonRuleBasedDamagerRepairer(textAttribute);
    reconciler.setDamager(damagerRepairer, contentType);
    reconciler.setRepairer(damagerRepairer, contentType);
  }
  
  @Override
  public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
    if (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)) {
      return new IAutoEditStrategy[] { new TartAutoEditStrategy() };
    } else {
      return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
    }
  }

  public int getTabWidth(ISourceViewer sourceViewer) {
    return 2;
  }

  public void handlePropertyChangedEvent(PropertyChangeEvent event) {
    styleManager.adaptToColorChange(event);
  }
}
