package kyui.element;
import kyui.core.KyUI;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class TextBox extends TextEdit {
  //modifiable values
  public String title="";
  public String hint="";
  public int value;
  public int fgColor;
  public int errorColor;
  public String rightText="";
  public boolean error=false;
  //...?
  public int strokeWeight=6;
  //
  Filter numberFilter;
  //temp values
  boolean changed=false;
  public TextBox(String name) {
    super(name);
    init();
  }
  public TextBox(String name, Rect pos_) {
    super(name, pos_);
    init();
  }
  public TextBox(String name, Rect pos_, String title_, String hint_) {
    super(name, pos_);
    init();
    title=title_;
    hint=hint_;
  }
  private void init() {
    numberFilter=new Filter("[^0-9\b\u007F\uFFFF\u0025\u0026\u0027\u0028]");
    filters.add(numberFilter);
    fgColor=50;
    textColor=50;
    bgColor=KyUI.Ref.color(127);
    padding=textSize;
    lineNumSize=0;
    errorColor=KyUI.Ref.color(255, 0, 0);
  }
  public void setNumberOnly(boolean v) {//default true...
    numberFilter.condition=v;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.WHEEL) {
      return true;
    }
    return super.mouseEvent(e, index);
  }
  @Override
  public void keyEvent(KeyEvent e) {
    super.keyEvent(e);
    String str=content.toString();
    if (isInt(str)) {
      value=Integer.parseInt(str);
    } else {
      value=0;
    }
    if (changed && onTextChangeListener != null) {
      onTextChangeListener.onEvent(this);
    }
    changed=false;
  }
  @Override
  protected void textChange() {
    changed=true;
  }
  private static boolean isInt(String str) {
    if (str.equals("")) return false;
    if (str.length() > 9) return false;
    if (str.equals("-")) return false;
    // just int or float is needed!
    int a=0;
    if (str.charAt(0) == '-') a=1;
    while (a < str.length()) {
      if (!('0' <= str.charAt(a) && str.charAt(a) <= '9')) return false;
      a=a + 1;
    }
    return true;
  }
  @Override
  public void render(PGraphics g) {
    //draw basic form
    g.strokeWeight(strokeWeight);
    cacheRect.set(pos.left + strokeWeight / 2, pos.top + strokeWeight / 2, pos.right - strokeWeight / 2, pos.bottom - strokeWeight / 2);
    if (entered) {
      if (pressedL) {
        g.fill(ColorExt.brighter(bgColor, 20));
        g.stroke(ColorExt.brighter(fgColor, 20));
      } else {
        g.fill(ColorExt.brighter(bgColor, 10));
        g.stroke(ColorExt.brighter(fgColor, 10));
      }
    } else {
      if (KyUI.focus == this) {
        g.fill(ColorExt.brighter(bgColor, 10));
        g.stroke(ColorExt.brighter(fgColor, 10));
      } else {
        g.fill(bgColor);
        g.stroke(fgColor);
      }
    }
    cacheRect.render(g);
    if (error) {
      g.stroke(errorColor);
      g.noFill();
      g.strokeWeight(1);
      cacheRect.render(g);
    }
    KyUI.clipRect(g, cacheRect);
    //
    float centerY=(pos.top + pos.bottom) / 2;
    g.noStroke();
    g.textAlign(PApplet.LEFT, PApplet.CENTER);
    if (title.equals("") == false) {
      g.textSize(Math.max(1, textSize * 3 / 4));
      g.fill(fgColor);
      offset=textSize / 3;
      g.text(title + "/ ", pos.left + textSize - 5, (pos.top + pos.bottom) / 2 - textSize * 2 / 3);
    }
    g.textSize(textSize);
    if (content.hasSelection()) {
      g.fill(selectionColor);
      String selectionPart=content.getSelectionPart(0);
      float selectionBefore=g.textWidth(content.getSelectionPartBefore(0));
      g.rect(pos.left + selectionBefore + lineNumSize + padding, offset + padding, pos.left + selectionBefore + g.textWidth(selectionPart) + lineNumSize + padding, offset + textSize + padding);
    }
    //g.textFont(textFont);
    if (content.empty()) {
      g.fill(ColorExt.brighter(textColor, -60));
      g.text(hint, pos.left + padding, centerY + offset);
    } else {
      g.fill(textColor);
      g.text(content.getLine(0), pos.left + textSize, centerY + offset);
    }
    if (KyUI.focus == this) {
      if (cursorOn) {
        float cursorOffsetX=g.textWidth("|") / 2;
        String line=content.getLine(content.line);
        g.text("|", pos.left + g.textWidth(line.substring(0, content.point)) + padding - cursorOffsetX, centerY + offset);
      }
    }
    if (!rightText.isEmpty()) {
      g.textAlign(PApplet.RIGHT, PApplet.CENTER);
      g.text(rightText, pos.right - textSize, centerY);
    }
    //g.textFont(KyUI.fontMain);
    g.noStroke();
    g.textAlign(PApplet.CENTER, PApplet.CENTER);
  }
}
