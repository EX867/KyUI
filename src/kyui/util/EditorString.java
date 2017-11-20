package kyui.util;
import kyui.core.KyUI;
import processing.core.PApplet;

import java.util.ArrayList;
public class EditorString {//editorString is based on line.
  // commandScript needs many overrides... because that is quite different from ordinary text editor.
  //this class is originally not for script editors...
  ArrayList<String> l;
  public int point=0;
  public int line=0;
  public int selStartLine;
  public int selStartPoint;
  public int selEndLine;
  public int selEndPoint;
  int maxpoint=0;
  public EditorString() {
    l=new ArrayList<String>();
    clear();
  }
  public void clear() {
    line=0;
    point=0;
    l.clear();
    addLine(0, "");
  }// - override needed in commandScript
  @Override
  public String toString() {
    if (lines() == 0) return "";
    StringBuilder builder=new StringBuilder();
    builder.append(getLine(0));
    for (int a=1; a < lines(); a++) {
      builder.append("\n").append(getLine(a));
    }
    return builder.toString();
  }
  public ArrayList<String> getRaw() {
    return l;
  }
  public void setRaw(ArrayList<String> l_) {
    l=l_;
  }
  public boolean empty() {
    if (lines() == 0) return true;
    if (lines() == 1 && getLine(0).equals("")) return true;
    return false;
  }
  public int lines() {
    return l.size();
  }
  public String getLine(int line_) {
    return l.get(line_);
  }
  //===Edit===// - override needed in commandScript
  public void addLine(String text) {
    addLine(lines(), text);
  }
  public void addLine(int line_, String text) {
    if (text == null) return;
    l.add(line_, text);
  }
  public void deleteLine(int line_) {
    l.remove(line_);
  }
  public void setLine(int line_, String text) {
    if (text == null) return;
    l.set(line_, text);
  }
  //===Edit complicated===// - override needed in commandScript.
  public void insert(String text) {
    insert(line, point, text);
  }
  public void insert(int line_, int point_, String text) {
    if (text.equals("")) return;
    String[] lines=PApplet.split(text, "\n");
    String endText=getLine(line_).substring(point_, getLine(line_).length());
    String temp=getLine(line_).substring(0, point_) + lines[0];
    setLine(line_, temp);
    for (int a=1; a < lines.length; a++) {
      line_++;
      addLine(line_, lines[a]);
    }
    if (!endText.isEmpty()) {
      setLine(line_, getLine(line_) + endText);
    }
  }
  public void delete(int startLine, int startPoint, int endLine, int endPoint) {
    if ((startLine < endLine || (startLine == endLine && startPoint < endPoint)) == false) return;
    boolean reread=false;
    String endText=getLine(endLine).substring(endPoint, getLine(endLine).length());
    setLine(startLine, getLine(startLine).substring(0, startPoint));
    for (int a=startLine + 1; a <= endLine; a++) {
      deleteLine(startLine + 1);
    }
    setLine(startLine, getLine(startLine) + endText);
    maxpoint=point;
  }
  public void deleteBefore(boolean word) {//if word, ctrl. - fix needed
    maxpoint=point;
    if ((point == 0 && line == 0) || empty()) return;
    if (point == 0) {
      if (line == 0) return;
      cursorLeft(false, false);
      setLine(line, getLine(line) + getLine(line + 1));
      deleteLine(line + 1);
    } else if (word) {
      boolean isSpace=false;
      String before=getLine(line);
      if (isSpaceChar(getLine(line).charAt(point - 1))) isSpace=true;
      while (getLine(line).length() > 0 && point > 0) {
        if (((isSpace && isSpaceChar(getLine(line).charAt(point - 1)) == false)) || (!isSpace && isSpaceChar(getLine(line).charAt(point - 1)))) break;
        l.set(line, getLine(line).substring(0, point - 1) + getLine(line).substring(Math.min(point, getLine(line).length()), getLine(line).length()));
        point--;
      }
    } else {
      String before=getLine(line);
      l.set(line, getLine(line).substring(0, point - 1) + getLine(line).substring(point, getLine(line).length()));
      cursorLeft(false, false);
      maxpoint=point;
    }
    maxpoint=point;
  }
  public void deleteAfter(boolean word) {// - fix needed
    maxpoint=point;
    if (empty()) return;
    if (point == getLine(line).length()) {
      if (line == lines() - 1) return;
      setLine(line, getLine(line) + getLine(line + 1));
      deleteLine(line + 1);
    } else if (word) {
      boolean isSpace=false;
      String before=getLine(line);
      if (isSpaceChar(getLine(line).charAt(point))) isSpace=true;
      while (getLine(line).length() > 0 && point < getLine(line).length()) {
        if (((isSpace && isSpaceChar(getLine(line).charAt(point)) == false)) || (!isSpace && isSpaceChar(getLine(line).charAt(point)))) break;
        l.set(line, getLine(line).substring(0, point) + getLine(line).substring(Math.min(point + 1, getLine(line).length()), getLine(line).length()));
      }
    } else {
      String before=getLine(line);
      l.set(line, getLine(line).substring(0, point) + getLine(line).substring(Math.min(point + 1, getLine(line).length()), getLine(line).length()));
    }
    maxpoint=point;
  }
  public void setText(String text) {
    clear();
    insert(0, 0, text);
  }
  //===Cursor movements===// - override needed in commandScript
  public void setCursor(int line_, int point_) {
    line=line_;
    point=point_;
    if (line >= lines()) line=lines() - 1;
    if (point > getLine(line).length()) point=getLine(line).length();
  }
  public void setCursorLine(int line_) {
    line=line_;
    if (line >= lines()) line=lines() - 1;
  }
  public void setCursorPoint(int point_) {
    point=point_;
    if (point > getLine(line).length()) point=getLine(line).length();
  }
  //currently, word is seperated by space.
  public void cursorLeft(boolean word, boolean select) {
    if (word && point != 0) {
      if (getLine(line).length() > 0 && point > 0) {
        boolean isSpace=false;
        if (isSpaceChar(getLine(line).charAt(point - 1))) isSpace=true;
        while (getLine(line).length() > 0 && point > 0) {
          if (((isSpace && isSpaceChar(getLine(line).charAt(point - 1)) == false)) || (!isSpace && isSpaceChar(getLine(line).charAt(point - 1)))) break;
          point--;
        }
      }
      if (select == false) resetSelection();
    } else {
      if (point == 0) {
        if (line != 0) {
          line--;
          point=getLine(line).length();
          if (select == false) resetSelection();
        }
      } else {
        point--;
        if (select == false) resetSelection();
      }
    }
    maxpoint=point;
  }
  public void cursorRight(boolean word, boolean select) {
    if (word && point != getLine(line).length()) {
      if (getLine(line).length() > 0 && point < getLine(line).length()) {
        boolean isSpace=false;
        if (isSpaceChar(getLine(line).charAt(point))) isSpace=true;
        while (getLine(line).length() > 0 && point < getLine(line).length()) {
          if (((isSpace && isSpaceChar(getLine(line).charAt(point)) == false)) || (isSpace == false && isSpaceChar(getLine(line).charAt(point)))) break;
          point++;
        }
      }
      if (select == false) resetSelection();
    } else {
      if (point == getLine(line).length()) {
        if (line < lines() - 1) {
          line++;
          point=0;
          if (select == false) resetSelection();
        }
      } else {
        point++;
        if (select == false) resetSelection();
      }
    }
    maxpoint=point;
  }
  public void cursorUp(boolean word, boolean select) {
    if (line == 0) {
      point=0;
    } else {
      line=line - 1;
      point=Math.min(maxpoint, getLine(line).length());
      if (select == false) resetSelection();
    }
  }
  public void cursorDown(boolean word, boolean select) {
    if (line >= lines() - 1) {
      point=getLine(lines() - 1).length();
    } else {
      line=line + 1;
      point=Math.min(maxpoint, getLine(line).length());
      if (select == false) resetSelection();
    }
  }
  public void selectionLeft(boolean word) {
    if (hasSelection() == false) {
      resetSelection();
    }
    cursorLeft(word, true);
    if (line > selStartLine || (selStartPoint <= point && selStartLine == line)) {
      selEndPoint=point;
      selEndLine=line;
    } else {
      selStartPoint=point;
      selStartLine=line;
    }
    fixSelection();
  }
  public void selectionRight(boolean word) {
    if (hasSelection() == false) {
      resetSelection();
    }
    cursorRight(word, true);
    if (line > selEndLine || (selEndPoint < point && selEndLine == line)) {
      selEndPoint=point;
      selEndLine=line;
    } else {
      selStartPoint=point;
      selStartLine=line;
    }
    fixSelection();
  }
  public void selectionUp(boolean word) {
    if (hasSelection() == false) {
      resetSelection();
    }
    boolean start=false;
    if (selStartLine == line && selStartPoint == point) start=true;
    cursorUp(word, true);
    if (start) {
      selStartPoint=point;
      selStartLine=line;
    } else {
      selEndPoint=point;
      selEndLine=line;
    }
    fixSelection();
  }
  public void selectionDown(boolean word) {
    if (hasSelection() == false) {
      resetSelection();
    }
    boolean start=true;
    if (selEndLine == line && selEndPoint == point) start=false;
    cursorDown(word, true);
    if (start) {
      selStartPoint=point;
      selStartLine=line;
    } else {
      selEndPoint=point;
      selEndLine=line;
    }
    fixSelection();
  }
  //===Selection===//
  public void select(int line1, int point1, int line2, int point2) {
    selStartLine=Math.min(line1, line2);
    selEndLine=Math.max(line1, line2);
    if (line1 == line2) {
      selStartPoint=Math.min(point1, point2);
      selEndPoint=Math.max(point1, point2);
    } else {
      if (line1 < line2) {
        selStartPoint=point1;
        selEndPoint=point2;
      } else {
        selStartPoint=point2;
        selEndPoint=point1;
      }
    }
  }
  public void selectFromCursor(int len) {
    resetSelection();
    int a=0;
    while (a < len) {
      selectionRight(false);
      a=a + 1;
    }
  }
  public void selectAll() {
    selStartLine=0;
    selStartPoint=0;
    selEndLine=lines() - 1;
    selEndPoint=getLine(lines() - 1).length();
  }
  public boolean hasSelection() {
    if (selStartLine == selEndLine && selStartPoint == selEndPoint) return false;
    return true;
  }
  public void resetSelection() {
    selStartLine=line;
    selStartPoint=point;
    selEndLine=selStartLine;
    selEndPoint=selStartPoint;
  }
  public String getSelection() {
    return substring(selStartLine, selStartPoint, selEndLine, selEndPoint);
  }
  public void deleteSelection() {
    maxpoint=point;
    if (hasSelection() == false) return;
    delete(selStartLine, selStartPoint, selEndLine, selEndPoint);
    point=selStartPoint;
    line=selStartLine;
    resetSelection();
  }
  //===Utils===//
  public void setCursorByIndex(int index) {//slow!
    if (lines() == 0) return;
    int sum=0;
    int psum=0;
    int a=0;
    while (a < lines()) {
      sum=sum + getLine(a).length();
      if (index < sum) {
        point=Math.min(getLine(a).length(), index - psum);
        line=a;
        return;
      }
      sum=sum + 1;
      psum=sum;
      a=a + 1;
    }
    line=lines() - 1;
    point=getLine(line).length();
  }
  public int getLineByIndex(int index) {//slow!
    if (lines() == 0) return 0;
    int sum=getLine(0).length();
    if (index <= sum) return 0;
    int a=1;
    while (a < lines()) {
      sum=sum + 1 + getLine(a).length();
      if (index <= sum) return a;
      a=a + 1;
    }
    return lines() - 1;
  }
  public boolean lineEmpty(int line_) {
    if (getLine(line_).equals("")) return true;
    return false;
  }
  public String getSelectionPartBefore(int line_) {
    if (line_ == selStartLine) {
      return getLine(selStartLine).substring(0, selStartPoint);
    }
    return "";
  }
  public String getSelectionPart(int line_) {
    if (line_ == selStartLine) {
      if (selStartLine == selEndLine) {
        return getLine(line_).substring(selStartPoint, selEndPoint);
      }
      return getLine(selStartLine).substring(selStartPoint, getLine(selStartLine).length()) + "\n";
    }
    if (line_ > selStartLine && line_ < selEndLine) {
      return getLine(line_) + "\n";
    }
    if (line_ == selEndLine) return getLine(selEndLine).substring(0, selEndPoint);
    return "";
  }
  public String substring(int startLine, int startPoint, int endLine, int endPoint) {
    fixSelection();
    if (selStartLine == selEndLine) return getLine(selStartLine).substring(selStartPoint, selEndPoint);
    StringBuilder ret=new StringBuilder();
    ret.append(getLine(selStartLine).substring(selStartPoint, getLine(selStartLine).length()));
    for (int a=selStartLine + 1; a < selEndLine; a++) {
      ret=ret.append("\n").append(getLine(a));
    }
    ret=ret.append("\n").append(getLine(selEndLine).substring(0, selEndPoint));
    return ret.toString();
  }
  //===Privates===//
  private boolean isSpaceChar(char in) {
    if (in == ' ' || in == '\t' || in == '\n' || in == '\r') return true;
    return false;
  }
  private void fixSelection() {//swap selection is wrong.
    if (selStartLine > selEndLine || (selStartLine == selEndLine && selStartPoint > selEndPoint)) {
      int temp=selEndLine;
      selEndLine=selStartLine;
      selStartLine=temp;
      temp=selEndPoint;
      selEndPoint=selStartPoint;
      selStartPoint=temp;
    }
    if (selStartLine >= lines()) selStartLine=lines() - 1;
    else if (selStartLine < 0) selStartLine=0;
    if (selEndLine >= lines()) selEndLine=lines() - 1;
    else if (selEndLine < 0) selEndLine=0;
    if (selStartPoint > getLine(selStartLine).length()) selStartPoint=getLine(selStartLine).length();
    else if (selStartPoint < 0) selStartPoint=0;
    if (selEndPoint > getLine(selEndLine).length()) selEndPoint=getLine(selEndLine).length();
    else if (selEndPoint < 0) selEndPoint=0;
  }
}