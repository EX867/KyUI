package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.ColorExt;
import kyui.util.EditorString;
import kyui.util.Rect;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
public class TextEdit extends Element {//no sliderX for now...
  protected RangeSlider slider;//if not null, it will work.
  @Attribute(getter="getText", setter="setText")
  private String text;//no use...! just meaning for string type one attribute...
  protected EditorString content;//real valueI for text.
  public EventListener onTextChangeListener;
  public EventListener onCursorChangeListener;
  public ArrayList<Filter> filters;
  public class Filter {
    String filter;
    public boolean condition=true;
    public Filter(String filter_) {//filter is regex.
      filter=filter_;
    }
    public Filter(String filter_, boolean condition_) {
      filter=filter_;
      condition=condition_;
    }
    String filter(String in) {
      if (condition == false) {
        return in;
      }
      return in.replaceAll(filter, "");
    }
  }
  //modifiable values
  @Attribute
  public PFont textFont;
  @Attribute
  public String hint="";//this is one-line hint if text is empty.
  @Attribute
  public int textSize=20;
  @Attribute
  public int lineNumSize=0;
  @Attribute(type=Attribute.COLOR)
  public int lineNumBgColor;
  @Attribute(type=Attribute.COLOR)
  public int lineNumColor;
  @Attribute
  public int blankLines=6;//blank lines to show below the content.
  @Attribute(type=Attribute.COLOR)
  public int textColor;
  @Attribute(type=Attribute.COLOR)
  public int hintColor;
  @Attribute(type=Attribute.COLOR)
  public int selectionColor;
  //temp values
  protected int clickLine=0;
  protected int clickPoint=0;
  protected float offsetY=0;
  protected float offsetX=0;
  protected Rect cacheRect=new Rect();
  protected boolean cursorOn=true;
  protected int cursorFrame=0;
  protected char pkey=0;
  public TextEdit(String name) {
    super(name);
    content=new EditorString();
    init();
  }
  public TextEdit(String name, Rect pos_) {
    super(name);
    content=new EditorString();
    pos=pos_;
    init();
  }
  public TextEdit(String name, EditorString content_) {
    super(name);
    content=content_;
    init();
  }
  private void init() {
    clipping=true;
    filters=new ArrayList<Filter>();
    padding=8;
    bgColor=50;
    textColor=KyUI.Ref.color(200);
    selectionColor=KyUI.Ref.color(50, 50, 205);
    lineNumBgColor=ColorExt.brighter(bgColor, -10);
    lineNumColor=KyUI.Ref.color(255);
    hintColor=KyUI.Ref.color(200, 140);
    lineNumSize=textSize * 2 + padding * 2;
    textFont=KyUI.fontText;
  }
  public int getCursorLine() {
    return content.line;
  }
  public int getCursorPoint() {
    return content.point;
  }
  public void setCursorLine(int v) {
    content.line=v;
  }
  public void setCursorPoint(int v) {
    content.point=v;
  }
  public void setSelection(int sl, int sp, int el, int ep) {
    content.selStartLine=sl;
    content.selStartPoint=sp;
    content.selEndLine=el;
    content.selEndPoint=ep;
    content.fixSelection();
  }
  public EditorString getContent() {//use only when needed...
    return content;
  }
  public String getLine(int line) {
    return content.getLine(line);
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
      if (onCursorChangeListener != null) {
        onCursorChangeListener.onEvent(this);
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
      if (entered) {
        offsetY+=e.getCount() * KyUI.WHEEL_COUNT;
        if (offsetY > textSize * (content.lines() + blankLines) + pos.top - pos.bottom) {
          offsetY=textSize * (content.lines() + blankLines) + pos.top - pos.bottom;
        }
        if (offsetY < 0) {
          offsetY=0;
        }
        updateSlider();
        invalidate();
        return false;
      }
    }
    return true;
  }
  protected void adjustCursor() {
    content.setCursorLine(Math.max(Math.min(offsetToLine(offsetY - padding + KyUI.mouseGlobal.getLast().y - pos.top), content.lines() - 1), 0));
    PGraphics cg=KyUI.cacheGraphics;
    cg.textFont(textFont);
    cg.textSize(Math.max(1, textSize * transformsAcc.getLast().scale));//just using function...
    float mouseX=/*offsetX+*/KyUI.mouseGlobal.getLast().x - pos.left - padding - lineNumSize;
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
  public void onLayout() {
    updateSlider();
  }
  public void recordHistory() {
    //override this
  }
  public boolean isRecordPoint(char c) {
    return c == ' ' || c == '\n';
    //override this
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
      for (Filter filter : filters) {
        if (filter.filter(e.getKey() + "").isEmpty()) {//filter filters the key char. no mouseEvent and listener event.
          return;
        }
      }
      boolean text=!(KyUI.ctrlPressed || KyUI.altPressed);
      //no have to text things
      if (e.getKey() == KyUI.Ref.BACKSPACE) {
        if (content.hasSelection()) {
          content.deleteSelection();
          recordHistory();
        } else {
          String delete=content.deleteBefore(KyUI.ctrlPressed);
          if (KyUI.ctrlPressed || pkey == 'a' || (delete.length() > 0 && isRecordPoint(delete.charAt(0)))) {
            recordHistory();
          }
        }
        moveToCursor();
        mouseEventPassed();
        textChange();
        pkey='\b';
      } else if (e.getKey() == KyUI.Ref.DELETE) {
        if (content.hasSelection()) {
          content.deleteSelection();
          recordHistory();
        } else {
          String delete=content.deleteAfter(KyUI.ctrlPressed);
          if (KyUI.ctrlPressed || pkey == 'a' || (delete.length() > 0 && isRecordPoint(delete.charAt(0)))) {
            recordHistory();
          }
        }
        moveToCursor();
        mouseEventPassed();
        textChange();
        pkey=KyUI.Ref.DELETE;
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
        if (pkey == '\b' || pkey == KyUI.Ref.DELETE || isRecordPoint(e.getKey())) {
          recordHistory();
        }
        pkey='a';//...
      }
      //pkey=e.getKey();
    }
  }
  protected void textChange() {
    if (onTextChangeListener != null) {
      onTextChangeListener.onEvent(this);
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
    slider.setOffset(getTotalSize(), offsetY);
    slider.invalidate();
  }
  public void setSlider(RangeSlider slider_) {
    slider=slider_;
    updateSlider();
    slider.setAdjustListener((Element e) -> {
      offsetY=slider.getOffset(getTotalSize());
      invalidate();
    });
  }
  protected int getTotalSize() {
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
    int start=offsetToLine(offsetY - padding);
    int end=offsetToLine(offsetY + pos.bottom - pos.top - padding);
    if (line <= start) {
      offsetY=textSize * line;
    } else if (line >= end) {
      offsetY=textSize * (line + 2) - pos.bottom + pos.top;
    }
    if (offsetY < 0) {
      offsetY=0;
    }
    updateSlider();
  }
  public void moveToCursor() {
    moveTo(content.line);
  }
  protected int offsetToLine(float offset_) {
    return (int)offset_ / Math.max(1, textSize);
  }
  @Override
  public void update() {
    if (KyUI.focus == this && KyUI.mouseState == KyUI.STATE_PRESSED) {
      if (pressedL) {
        int start=offsetToLine(offsetY - padding);
        int end=offsetToLine(offsetY + pos.bottom - pos.top - padding);
        adjustCursor();
        if (content.line >= end) {
          content.setCursorLine(end);
        } else if (content.line <= start) {
          content.setCursorLine(start);
        }
        content.select(clickLine, clickPoint, content.line, content.point);
        moveToCursor();
        if (onCursorChangeListener != null) {
          onCursorChangeListener.onEvent(this);
        }
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
  public void render(PGraphics g) {
    //no draw slider...
    //draw basic form
    g.fill(bgColor);
    pos.render(g);
    g.fill(lineNumBgColor);
    cacheRect.set(pos.left, pos.top, pos.left + lineNumSize, pos.bottom);
    cacheRect.render(g);
    cacheRect.set(pos.left, pos.top, pos.right, pos.bottom);
    //setup text
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    g.textFont(textFont);
    g.textSize(Math.max(1, textSize));
    g.textLeading(textSize / 2);
    //iterate lines
    int start=offsetToLine(offsetY - padding);
    int end=offsetToLine(offsetY + pos.bottom - pos.top - padding);
    g.fill(selectionColor);
    if (content.hasSelection()) {
      for (int a=Math.max(0, start - 1); a < content.lines() && a < end + 1; a++) {
        //draw selection
        String selectionPart=content.getSelectionPart(a);
        if (!selectionPart.isEmpty()) {
          if (selectionPart.charAt(selectionPart.length() - 1) == '\n') {
            selectionPart=selectionPart.substring(0, selectionPart.length() - 1);
            g.rect(pos.left + g.textWidth(content.getSelectionPartBefore(a)) + lineNumSize + padding, pos.top + a * textSize - offsetY + padding, pos.right - padding, pos.top + (a + 1) * textSize - offsetY + padding);
          } else {
            float selectionBefore=g.textWidth(content.getSelectionPartBefore(a));
            g.rect(pos.left + selectionBefore + lineNumSize + padding, pos.top + a * textSize - offsetY + padding, pos.left + selectionBefore + g.textWidth(selectionPart) + lineNumSize + padding, pos.top + (a + 1) * textSize - offsetY + padding);
          }
        }
      }
    }
    if (content.empty()) {
      g.fill(hintColor);
      g.text(hint, pos.left + lineNumSize + padding, pos.top + 0.5F * textSize - offsetY + padding);
    } else {
      g.fill(textColor);
      for (int a=Math.max(0, start - 1); a < content.lines() && a < end + 1; a++) {
        String line=content.getLine(a);
        g.text(line, pos.left + lineNumSize + padding, pos.top + (a + 0.5F) * textSize - offsetY + padding);
      }
    }
    //draw text (no comment in normal textEditor implementation
    if (KyUI.focus == this) {
      if (cursorOn) {
        if (start <= content.line && content.line <= end) {
          g.fill(textColor);
          float cursorOffsetX=g.textWidth("|") / 2;
          String line=content.getLine(content.line);
          if (line.length() >= content.point) {//?????????????????
            g.text("|", pos.left + g.textWidth(line.substring(0, content.point)) + lineNumSize + padding - cursorOffsetX, pos.top + (content.line + 0.5F) * textSize - offsetY + padding);
          }
        }
      }
    }
    g.textAlign(KyUI.Ref.RIGHT, KyUI.Ref.CENTER);
    g.textFont(KyUI.fontMain);
    g.textSize(Math.max(1, textSize));
    g.textLeading(textSize / 2);
    for (int a=Math.max(0, start - 1); a < end + 1; a++) {
      if (a < content.lines()) {
        g.fill(lineNumColor);
      } else {
        g.fill(ColorExt.brighter(lineNumColor, -150));
      }
      g.text(a + "", pos.left + lineNumSize - padding, pos.top + (a + 0.5F) * textSize - offsetY + padding);
    }
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  public void setText(String text) {
    for (Filter filter : filters) {
      text=filter.filter(text);
    }
    content.setText(text);
    content.fixSelection();
    invalidate();
  }
  public String getText() {
    return content.toString();
  }
}