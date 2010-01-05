package org.viridia.tart.eclipse.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class TartDocumentProvider extends FileDocumentProvider {
  protected IDocument createDocument(Object element) throws CoreException {
    IDocument document = super.createDocument(element);
    if (document != null) {
      IDocumentPartitioner partitioner = new FastPartitioner(
          new TartPartitionScanner(), new String[] {
              IDocument.DEFAULT_CONTENT_TYPE,
              TartPartitionScanner.TART_MULTI_LINE_COMMENT,
              TartPartitionScanner.TART_SINGLE_LINE_COMMENT,
              TartPartitionScanner.TART_DOC_COMMENT });
      partitioner.connect(document);
      document.setDocumentPartitioner(partitioner);
    }

    return document;
  }
}