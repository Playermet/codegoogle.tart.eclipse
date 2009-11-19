package org.viridia.tart.eclipse.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

public class TartAutoEditStrategy extends DefaultIndentLineAutoEditStrategy {
  public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
    if (cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text)) {
      smartIndentAfterNewLine(doc, cmd);
    } else if ("}".equals(cmd.text)) {
      smartInsertAfterBracket(doc, cmd);
    }    
  }

  private boolean endsWithDelimiter(IDocument d, String txt) {
    return TextUtilities.endsWith(d.getLegalLineDelimiters(), txt) > -1;
  }

  protected void smartIndentAfterNewLine(IDocument doc, DocumentCommand cmd) {
    int docLength = doc.getLength();
    if (cmd.offset == -1 || docLength == 0) {
      return;
    }

    try {
      int insertPos = (cmd.offset == docLength ? cmd.offset - 1 : cmd.offset);
      int line = doc.getLineOfOffset(insertPos);

      StringBuffer buf = new StringBuffer(cmd.text);
      if (cmd.offset < docLength && doc.getChar(cmd.offset) == '}') {
        int indLine = findMatchingOpenBracket(doc, line, cmd.offset, 0);
        if (indLine == -1) { indLine = line; }
        buf.append(getIndentOfLine(doc, indLine));
      } else {
        int start = doc.getLineOffset(line);
        int whiteend = findEndOfWhiteSpace(doc, start, cmd.offset);
        buf.append(doc.get(start, whiteend - start));
        if (getNestingDelta(doc, start, cmd.offset, true) > 0) {
          buf.append("  ");
        }
      }

      cmd.text = buf.toString();
    } catch (BadLocationException excp) {
    }
  }

  protected void smartInsertAfterBracket(IDocument doc, DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }

    try {
      int pos = (cmd.offset == doc.getLength() ? cmd.offset - 1 : cmd.offset);
      int line = doc.getLineOfOffset(pos);
      int start = doc.getLineOffset(line);
      int whiteend = findEndOfWhiteSpace(doc, start, cmd.offset);

      // shift only when line does not contain any text up to the closing bracket
      if (whiteend == cmd.offset) {
        // evaluate the line with the opening bracket that matches out closing  bracket
        int indLine = findMatchingOpenBracket(doc, line, cmd.offset, 1);
        if (indLine != -1 && indLine != line) {
          // take the indent of the found line
          StringBuffer replaceText = new StringBuffer(getIndentOfLine(doc, indLine));
          // add the rest of the current line including the just added close bracket
          replaceText.append(doc.get(whiteend, cmd.offset - whiteend));
          replaceText.append(cmd.text);
          // modify document command
          cmd.length = cmd.offset - start;
          cmd.offset = start;
          cmd.text = replaceText.toString();
        }
      }
    } catch (BadLocationException excp) {
    }
  }
  
  protected int findMatchingOpenBracket(IDocument doc, int line, int lineEnd,
      int closingBracketIncrease) throws BadLocationException {

    int lineStart = doc.getLineOffset(line);
    int brackcount = getNestingDelta(doc, lineStart, lineEnd, false) - closingBracketIncrease;

    // sum up the brackets counts of each line (closing brackets count negative,
    // opening positive) until we find a line the brings the count to zero
    while (brackcount < 0) {
      line--;
      if (line < 0) {
        return -1;
      }
      lineStart = doc.getLineOffset(line);
      lineEnd = lineStart + doc.getLineLength(line) - 1;
      brackcount += getNestingDelta(doc, lineStart, lineEnd, false);
    }

    return line;
  }

  /**
   * Return the difference in nesting level from the start of a range of text to the end of
   * a range of text.
   * 
   * @param doc The document
   * @param lineStart The start of the range of text
   * @param lineEnd The end of the range of text
   * @param skipLeadingClose If true, skip leading close brackets
   * @return The change in nesting level over the span of text
   * @throws BadLocationException
   */
  private int getNestingDelta(IDocument doc, int lineStart, int lineEnd, boolean skipLeadingClose)
      throws BadLocationException {

    int pos = lineStart;
    int bracketcount = 0;
    ITypedRegion partition = doc.getPartition(pos);
    int partitionEnd = partition.getOffset() + partition.getLength();
    if (!partition.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
      // If the partition isn't the kind we're interested in, then just skip
      // over it.
      pos = partitionEnd;
    }

    while (pos < lineEnd) {
      // If we cross a partition boundary, get the new paritition.
      if (pos >= partitionEnd) {
        partition = doc.getPartition(pos);
        partitionEnd = partition.getOffset() + partition.getLength();
        if (!partition.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
          // If the partition isn't the kind we're interested in, then just skip
          // over it.
          pos = partitionEnd;
          continue;
        }
      }

      char ch = doc.getChar(pos++);
      switch (ch) {
        case '{':
          bracketcount++;
          skipLeadingClose = false;
          break;

        case '}':
          if (!skipLeadingClose) {
            bracketcount--;
          }
          break;

        case '"':
        case '\'':
          pos = getStringEnd(doc, pos, lineEnd, ch);
          break;

        default:
      }
    }
    return bracketcount;
  }

  private int getStringEnd(IDocument doc, int pos, int lineEnd, char quote)
      throws BadLocationException {
    while (pos < lineEnd) {
      char ch = doc.getChar(pos++);
      if (ch == '\\') {
        // ignore escaped characters
        pos++;
      } else if (ch == quote) {
        return pos;
      }
    }
    return lineEnd;
  }

  protected String getIndentOfLine(IDocument doc, int line) throws BadLocationException {
    if (line > -1) {
      int start = doc.getLineOffset(line);
      int end = start + doc.getLineLength(line) - 1;
      int whiteend = findEndOfWhiteSpace(doc, start, end);
      return doc.get(start, whiteend - start);
    }

    return "";
  }
}
