package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.ColorExt;
import kyui.util.EditorString;
import kyui.util.Rect;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
public class TextEdit extends Element {//no sliderX for now...
  RangeSlider slider;//if not null, it will work.
  EditorString content;
  EventListener onTextChangeListener;
  ArrayList<Filter> filters;
  class Filter {
    String filter;
    public Filter(String filter_) {//filter is regex.
      filter=filter_;
    }
    String filter(String in) {
      if (in.contains(filter)) {
        return in.replaceAll(filter, "");
      } else {
        return in;
      }
    }
  }
  //modifiable values
  public PFont textFont;
  public String hint="";//this is one-line hint if text is empty.
  public int textSize=20;
  public int lineNumSize=0;
  public int lineNumBgColor;
  public int lineNumColor;
  public int blankLines=5;//blank lines to show below the content.
  public int textColor;
  public int selectionColor;
  //temp values
  int clickLine=0;
  int clickPoint=0;
  float offset=0;
  Rect cacheRect=new Rect();
  protected boolean cursorOn=true;
  int cursorFrame=0;
  public TextEdit(String name) {
    super(name);
    init();
  }
  public TextEdit(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    content=new EditorString();
    filters=new ArrayList<Filter>();
    padding=8;
    bgColor=50;
    textColor=KyUI.Ref.color(200);
    selectionColor=KyUI.Ref.color(50, 50, 205);
    lineNumBgColor=ColorExt.brighter(bgColor, -10);
    lineNumColor=KyUI.Ref.color(255);
    lineNumSize=textSize * 2 + padding * 2;
    textFont=KyUI.fontText;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS) {
      int oldLine=content.line;
      int oldPoint=content.point;
      adjustCursor();//move cursor
      if (KyUI.shiftPressed) {
        if (content.hasSelection()) {
          if (oldLine == content.selEndLine && oldPoint == content.selEndPoint) {//click after old click point.
            clickLine=content.selStartLine;
            clickPoint=content.selStartPoint;
          } else {
            clickLine=content.selEndLine;
            clickPoint=content.selEndPoint;
          }
        } else {
          clickPoint=oldPoint;
          clickLine=oldLine;
        }
        content.select(clickLine, clickPoint, content.line, content.point);
      } else {
        clickPoint=content.point;
        clickLine=content.line;
        content.resetSelection();
      }
      invalidate();
      return false;
    } else if (e.getAction() == MouseEvent.DRAG) {//logic is moved to update()
      if (pressedL) {
        requestFocus();
        return false;
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        skipRelease=true;//because release action releases focus automatically.
        return false;
      }
    } else if (e.getAction() == MouseEvent.WHEEL) {
      offset+=e.getCount() * 25;//FIX>>temp value.
      if (offset < 0) {
        offset=0;
      }
      updateSlider();
      invalidate();
      return false;
    }
    return true;
  }
  void adjustCursor() {
    content.setCursorLine(Math.max(Math.min(offsetToLine(offset - padding + KyUI.mouseGlobal.y - pos.top), content.lines() - 1), 0));
    PGraphics cg=KyUI.cacheGraphics;
    cg.textFont(textFont);
    cg.textSize(textSize);//just using function...
    float mouseX=/*offsetX+*/KyUI.mouseGlobal.x - pos.left - padding - lineNumSize;
    String line=content.getLine(content.line);
    int point=0;//mid
    int low=1;
    int high=line.length() - 1;
    if (line.length() == 0 || mouseX <= cg.textWidth(line.charAt(0)) / 2) {
      content.setCursorPoint(0);
    } else if (mouseX >= cg.textWidth(line) - cg.textWidth(line.charAt(line.length() - 1)) / 2) {
      content.setCursorPoint(line.length());
    } else {
      point=(low + high) / 2;
      while (low < high) {
        float width1=cg.textWidth(line.substring(0, point - 1)) + cg.textWidth(line.charAt(point - 1)) / 2;
        float width2=cg.textWidth(line.substring(0, point)) + cg.textWidth(line.charAt(point)) / 2;
        if (mouseX < width1) {
          high=point - 1;
          point=(low + high) / 2;
        } else if (mouseX > width2) {
          low=point + 1;
          point=(low + high) / 2;
        } else {
          break;
        }
      }
      //if (low < high) {FIX this problem... but it works well!
      //  System.out.print("error : ");
      //}
      content.setCursorPoint(point);
    }
    cg.textFont(KyUI.fontMain);
  }
  @Override
  public void keyTyped(KeyEvent e) {
    if (KyUI.focus == this) {
      //System.out.println(e.getKey() + " (" + (int)e.getKey() + ")  - " + e.getKeyCode() + " : " + KyUI.frameCount);
      if (e.getKey() == KyUI.Ref.CODED) {
        if (e.getKeyCode() == KyUI.Ref.LEFT) {
          if (KyUI.shiftPressed) content.selectionLeft(KyUI.ctrlPressed);
          else content.cursorLeft(KyUI.ctrlPressed, false);
          moveToCursor();
          mouseEventPassed();
        } else if (e.getKeyCode() == KyUI.Ref.RIGHT) {
          if (KyUI.shiftPressed) content.selectionRight(KyUI.ctrlPressed);
          else content.cursorRight(KyUI.ctrlPressed, false);
          moveToCursor();
          mouseEventPassed();
        } else if (e.getKeyCode() == KyUI.Ref.UP) {
          if (KyUI.shiftPressed) content.selectionUp(KyUI.ctrlPressed);
          else content.cursorUp(KyUI.ctrlPressed, false);
          moveToCursor();
          mouseEventPassed();
        } else if (e.getKeyCode() == KyUI.Ref.DOWN) {
          if (KyUI.shiftPressed) content.selectionDown(KyUI.ctrlPressed);
          else content.cursorDown(KyUI.ctrlPressed, false);
          moveToCursor();
          mouseEventPassed();
        }
        return;
      }
      boolean text=!(KyUI.ctrlPressed || KyUI.altPressed);
      //no have to text things
      if (e.getKey() == KyUI.Ref.BACKSPACE) {
        if (content.hasSelection()) {
          content.deleteSelection();
        } else {
          content.deleteBefore(KyUI.ctrlPressed);
        }
        moveToCursor();
        mouseEventPassed();
        textChange();
      } else if (e.getKey() == KyUI.Ref.DELETE) {
        if (content.hasSelection()) {
          content.deleteSelection();
        } else {
          content.deleteAfter(KyUI.ctrlPressed);
        }
        moveToCursor();
        mouseEventPassed();
        textChange();
      } else if (text) {//and then text things.
        if (e.getKey() == '\n') {
          if (content.hasSelection()) {
            content.deleteSelection();
          }
          content.insert(e.getKey() + "");
          content.line++;
          content.point=0;
          moveToCursor();
        } else {
          if (content.hasSelection()) {
            content.deleteSelection();
          }
          content.insert(e.getKey() + "");
          content.cursorRight(false, false);
          moveToCursor();
        }
        mouseEventPassed();
        textChange();
      }
    }
  }
  private void textChange() {
    if (onTextChangeListener != null) {
      onTextChangeListener.onEvent();
    }
  }
  private void mouseEventPassed() {
    cursorOn=true;
    cursorFrame=20;
    invalidate();
  }
  public void updateSlider() {
    if (slider == null) return;
    slider.setLength(getTotalSize(), pos.bottom - pos.top);
    slider.setOffset(getTotalSize(), offset);
  }
  int getTotalSize() {//FIX>>
    return (content.lines() + blankLines) * textSize + padding * 2;
  }
  public void insert(int point_, int line_, String text) {
    content.insert(point_, line_, text);
    moveTo(line_);
    updateSlider();
  }
  public void addLine(int line_, String text) {
    content.addLine(line_, text);
    moveTo(line_);
    updateSlider();
  }
  public void setTextChangeListener(EventListener onTextChangeListener) {
    this.onTextChangeListener=onTextChangeListener;
  }
  public void moveTo(int line) {
    int start=offsetToLine(offset - padding);
    int end=offsetToLine(offset + pos.bottom - pos.top - padding);
    if (line <= start) {
      offset=textSize * line;
    } else if (line >= end) {
      offset=textSize * (line + 2) - pos.bottom + pos.top;
    }
    if (offset < 0) {
      offset=0;
    }
    updateSlider();
  }
  void moveToCursor() {
    moveTo(content.line);
  }
  int offsetToLine(float offset_) {
    return (int)offset_ / textSize;
  }
  @Override
  public void update() {
    if (KyUI.focus == this && KyUI.mouseState == KyUI.STATE_PRESSED) {
      if (pressedL) {
        int start=offsetToLine(offset - padding);
        int end=offsetToLine(offset + pos.bottom - pos.top - padding);
        adjustCursor();
        if (content.line >= end) {
          content.setCursorLine(end);
        } else if (content.line <= start) {
          content.setCursorLine(start);
        }
        content.select(clickLine, clickPoint, content.line, content.point);
        moveToCursor();
        cursorOn=true;
        cursorFrame=20;
        invalidate();
      }
    } else if (cursorFrame == 0) {
      if (cursorOn) {
        cursorOn=false;
      } else {
        cursorOn=true;
      }
      invalidate();
      cursorFrame=20;
    } else {
      cursorFrame--;
    }
  }
  @Override
  public void render(PGraphics g) {//no draw slider...
    //draw basic form
    g.fill(bgColor);
    pos.render(g);
    g.fill(lineNumBgColor);
    cacheRect.set(pos.left, pos.top, pos.left + lineNumSize, pos.bottom);
    cacheRect.render(g);
    cacheRect.set(pos.left, pos.top, pos.right, pos.bottom);
    KyUI.clipRect(g, cacheRect);
    //setup text
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    g.textFont(textFont);
    g.textSize(textSize);
    g.textLeading(textSize / 2);
    //iterate lines
    int start=offsetToLine(offset - padding);
    int end=offsetToLine(offset + pos.bottom - pos.top - padding);
    g.fill(selectionColor);
    if (content.hasSelection()) {
      for (int a=Math.max(0, start - 1); a < content.lines() && a < end + 1; a++) {
        //draw selection
        String selectionPart=content.getSelectionPart(a);
        if (!selectionPart.isEmpty()) {
          if (selectionPart.charAt(selectionPart.length() - 1) == '\n') {
            selectionPart=selectionPart.substring(0, selectionPart.length() - 1);
            g.rect(pos.left + g.textWidth(content.getSelectionPartBefore(a)) + lineNumSize + padding, pos.top + a * textSize - offset + padding, pos.right - padding, pos.top + (a + 1) * textSize - offset + padding);
          } else {
            float selectionBefore=g.textWidth(content.getSelectionPartBefore(a));
            g.rect(pos.left + selectionBefore + lineNumSize + padding, pos.top + a * textSize - offset + padding, pos.left + selectionBefore + g.textWidth(selectionPart) + lineNumSize + padding, pos.top + (a + 1) * textSize - offset + padding);
          }
        }
      }
    }
    g.fill(textColor);
    for (int a=Math.max(0, start - 1); a < content.lines() && a < end + 1; a++) {
      String line=content.getLine(a);
      g.text(line, pos.left + lineNumSize + padding, pos.top + (a + 0.5F) * textSize - offset + padding);
    }
    //draw text (no comment in normal textEditor implementation
    if (KyUI.focus == this) {
      if (cursorOn) {
        if (start <= content.line && content.line <= end) {
          float cursorOffsetX=g.textWidth("|") / 2;
          String line=content.getLine(content.line);
          g.text("|", pos.left + g.textWidth(line.substring(0, content.point)) + lineNumSize + padding - cursorOffsetX, pos.top + (content.line + 0.5F) * textSize - offset + padding);
        }
      }
    }
    g.textAlign(KyUI.Ref.RIGHT, KyUI.Ref.CENTER);
    g.textFont(KyUI.fontMain);
    g.textSize(textSize);
    g.textLeading(textSize / 2);
    for (int a=Math.max(0, start - 1); a < end + 1; a++) {
      if (a < content.lines()) {
        g.fill(lineNumColor);
      } else {
        g.fill(ColorExt.brighter(lineNumColor, -150));
      }
      g.text(a + "", pos.left + lineNumSize - padding, pos.top + (a + 0.5F) * textSize - offset + padding);
    }
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  @Override
  public void overlay(PGraphics g) {
    KyUI.removeClip(g);
  }
  public void setText(String text) {
    content.setText(text);
  }
}