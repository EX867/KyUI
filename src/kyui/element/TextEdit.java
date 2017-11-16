package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.ColorExt;
import kyui.util.EditorString;
import kyui.util.Rect;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.EventListener;
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
  public int lineNumSize=50;
  public int lineNumBgColor;
  public int lineNumColor;
  public int blankLines=5;//blank lines to show below the content.
  public int textColor;
  public int selectionColor;
  //temp values
  boolean cursorOn=true;
  int clickLine=0;
  int clickPoint=0;
  float offset=0;
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
    bgColor=KyUI.Ref.color(50);
    textColor=0xFF000000;
    selectionColor=KyUI.Ref.color(0, 0, 255);
    lineNumBgColor=ColorExt.brighter(bgColor, -10);
    lineNumColor=KyUI.Ref.color(255);
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS) {
      adjustCursor();//move cursor
      if (KyUI.shiftPressed) {
        //ADD>>shift selection action
      } else {
        clickPoint=content.point;
        clickLine=content.line;
        content.resetSelection();
      }
    } else if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL) {
        requestFocus();
        adjustCursor();
        content.select(clickLine, clickPoint, content.line, content.point);
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        requestFocus();//because release action releases focus automatically.
      }
    } else if (e.getAction() == MouseEvent.WHEEL) {
      offset+=e.getCount() * textSize / 5;//FIX>>temp value.
      updateSlider();
    }
    return true;
  }
  void adjustCursor() {
    KyUI.cacheGraphics.textFont(textFont);
    KyUI.Ref.textSize(Math.max(1, textSize));//just using function...
    //set line...
    //set point...
    KyUI.cacheGraphics.textFont(KyUI.fontMain);
  }
  @Override
  public void keyEvent(KeyEvent e) {
    if (KyUI.focus == this) {
      if (e.getKeyCode() == KyUI.Ref.CODED) {
        if (e.getKey() == KyUI.Ref.LEFT) {
          if (KyUI.shiftPressed) content.selectionLeft(KyUI.ctrlPressed);
          else content.cursorLeft(KyUI.ctrlPressed, false);
          return;
        } else if (e.getKey() == KyUI.Ref.RIGHT) {
          if (KyUI.shiftPressed) content.selectionRight(KyUI.ctrlPressed);
          else content.cursorRight(KyUI.ctrlPressed, false);
        } else if (e.getKey() == KyUI.Ref.UP) {
          if (KyUI.shiftPressed) content.selectionUp(KyUI.ctrlPressed);
          else content.cursorUp(KyUI.ctrlPressed, false);
        } else if (e.getKey() == KyUI.Ref.DOWN) {
          if (KyUI.shiftPressed) content.selectionDown(KyUI.ctrlPressed);
          else content.cursorDown(KyUI.ctrlPressed, false);
        }
      }
      //process shortcuts and insert!!
    }
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
  public void moveTo(int line) {//ADD>>
    //float Yoffset=textSize/2-(sliderPos-(position.y-size.y+sliderLength))*(max(1, (content.lines()+10)*textSize-size.y*2)/max(1, size.y*2-sliderLength*2));
    int start=0;//=floor(max(0, min(content.lines()-1, (-Yoffset)/textSize)));//Yoffset+a*textSize>-textSize
    int end=0;//=floor((size.y*2-Yoffset)/textSize-3/2);
    if (line < start) {
      offset=-(line - 1 / 2) * textSize;
    } else if (line > end) {
      offset=-(line + 5 / 2) * textSize + pos.bottom - pos.top * 2;
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
    //ADD>>update cursorOn
  }
  @Override
  public void render(PGraphics g) {//no draw slider...
    //draw basic form
    g.fill(bgColor);
    pos.render(g);
    g.fill(lineNumColor);
    pos.render(g);
    //setup text
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    g.textSize(textSize);
    g.textFont(textFont);
    g.textLeading(textSize / 2);
    //iterate lines
    int start=offsetToLine(offset - padding);
    int end=offsetToLine(offset + pos.bottom - pos.top - padding);
    for (int a=Math.max(0, start - 1); a < content.lines() && a < end + 1; a++) {
      //draw selection
      if (content.hasSelection()) {
        g.fill(selectionColor);
        String selectionPart=content.getSelectionPart(a);
        if (!selectionPart.isEmpty()) {
          if (selectionPart.charAt(selectionPart.length() - 1) == '\n') {
            selectionPart=selectionPart.substring(0, selectionPart.length() - 1);
            g.rect(pos.left + g.textWidth(content.getSelectionPartBefore(a)) + lineNumSize + padding, pos.top + a * textSize - offset + padding, pos.right - padding, pos.top + (a + 1) * textSize - offset + padding);
          } else g.rect(pos.left + g.textWidth(content.getSelectionPartBefore(a)) + lineNumSize + padding, pos.top + a * textSize - offset + padding, pos.left + g.textWidth(selectionPart) + lineNumSize + padding, pos.top + (a + 1) * textSize - offset + padding);
        }
      }
      //draw text (no comment in normal textEditor implementation
      String line=content.getLine(a);
      g.fill(textColor);
      g.text(content.getLine(a), pos.left + lineNumSize + padding, pos.top + (a + 0.5F) * textSize - offset + padding);
      if (KyUI.focus == this) {
        if (cursorOn) {
          if (start <= content.line && content.line <= end) {
            g.text("|", pos.left + g.textWidth(content.getLine(content.line).substring(0, content.point)) + lineNumSize + padding, pos.top + content.line * textSize - offset + padding);
          }
        }
      }
      g.textAlign(KyUI.Ref.RIGHT, KyUI.Ref.CENTER);
      g.textFont(KyUI.fontMain);
      g.textSize(textSize);
      g.textLeading(textSize / 2);
      for (a=Math.max(0, start - 1); a < content.lines() + blankLines && a < end + 1; a++) {
        g.fill(lineNumBgColor);
        g.rect();
        g.fill(lineNumColor);
        g.text(a + "", position.x - size.x + textSize * 5 / 2, position.y - size.y + (a + 1) * textSize + Yoffset);
      }
      g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
    }
  }
}