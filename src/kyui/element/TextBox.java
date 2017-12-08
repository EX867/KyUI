package kyui.element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class TextBox extends TextEdit {
  //modifiable values
  @Attribute
  public String title="";
  @Attribute
  public String hint="";
  public int valueI=0;
  public float valueF=0;
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute(type=Attribute.COLOR)
  public int errorColor;
  @Attribute
  public String rightText="";
  @Attribute
  public boolean error=false;
  //...?
  public int strokeWeight=6;
  //
  public enum NumberType {
    NONE, INTEGER, FLOAT
  }
  @Attribute(setter="setNumberOnly")
  NumberType numberOnly=NumberType.INTEGER;
  //Filter numberFilter;
  //temp values
  boolean changed=false;
  public TextBox(String name) {
    super(name);
    init();
  }
  public TextBox(String name, String title_, String hint_) {
    super(name);
    init();
    title=title_;
    hint=hint_;
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
    filters.add(new Filter("\n"));
    //numberFilter=new Filter("[^0-9\b\u007F\uFFFF\u0025\u0026\u0027\u0028]");
    //filters.add(numberFilter);
    fgColor=50;
    textColor=50;
    bgColor=KyUI.Ref.color(127);
    padding=textSize;
    lineNumSize=0;
    errorColor=KyUI.Ref.color(255, 0, 0);
    clipping=true;
  }
  public void setNumberOnly(NumberType v) {//default true...
    //numberFilter.condition=v;
    numberOnly=v;
    setText(content.toString());
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.WHEEL) {
      return true;
    }
    return super.mouseEvent(e, index);
  }
  @Override
  public void keyTyped(KeyEvent e) {
    String before=content.toString();
    super.keyTyped(e);
    String str=content.toString();
    if (numberOnly == NumberType.INTEGER) {
      if (isInt(str)) {
        valueI=Integer.parseInt(str);
      } else {
        content.setText(before);
      }
    } else if (numberOnly == NumberType.FLOAT) {
      if (isFloat(str)) {
        valueF=Float.parseFloat(str);
      } else {
        content.setText(before);
      }
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
  private static boolean isFloat(String str) {//https://stackoverflow.com/questions/43156077/how-to-check-a-string-is-float-or-int
    if (str.equals("")) return false;
    if (str.length() > 9) return false;
    if (str.equals("-")) return false;
    return str.matches("\\d*\\.?\\d*");//FIX>>minus...
  }
  @Override
  public void moveTo(int line) {//do nothing!
  }
  @Override
  public void render(PGraphics g) {
    //draw basic form
    g.strokeWeight(strokeWeight);
    clipRect.set(pos.left + strokeWeight / 2, pos.top + strokeWeight / 2, pos.right - strokeWeight / 2, pos.bottom - strokeWeight / 2);
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
    clipRect.render(g);
    if (error) {
      g.stroke(errorColor);
      g.noFill();
      g.strokeWeight(1);
      clipRect.render(g);
    }
    KyUI.clipRect(g, clipRect);
    float centerY=(pos.top + pos.bottom) / 2;
    g.noStroke();
    g.textAlign(PApplet.LEFT, PApplet.CENTER);
    if (!title.equals("")) {
      g.textSize(Math.max(1, textSize * 3 / 4));
      g.fill(fgColor);
      offset=(textSize / 3);
      g.text(title + "/ ", pos.left + textSize / 2, pos.top + textSize * 3 / 4);
    } else {
      offset=0;
    }
    g.textSize(textSize);
    if (content.hasSelection()) {
      g.fill(selectionColor);
      String selectionPart=content.getSelectionPart(0);
      float selectionBefore=g.textWidth(content.getSelectionPartBefore(0));
      g.rect(pos.left + selectionBefore + lineNumSize + padding, centerY - textSize / 2 + offset, pos.left + selectionBefore + g.textWidth(selectionPart) + lineNumSize + padding, centerY + textSize / 2 + offset);
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
        g.text("|", pos.left + g.textWidth(line.substring(0, content.point)) + padding - cursorOffsetX, centerY + offset - 3);
      }
    }
    if (!rightText.isEmpty()) {
      g.textAlign(PApplet.RIGHT, PApplet.CENTER);
      g.text(rightText, pos.right - textSize, centerY + offset);
    }
    //g.textFont(KyUI.fontMain);
    g.noStroke();
    g.textAlign(PApplet.CENTER, PApplet.CENTER);
    KyUI.removeClip(g);
  }
  @Override
  public void setText(String text) {
    super.setText(text);
    //ADD>>filter text!!!
    if (numberOnly == NumberType.INTEGER) {
      if (isInt(text)) {
        valueI=Integer.parseInt(text);
      }
    } else if (numberOnly == NumberType.FLOAT) {
      if (isFloat(text)) {
        valueF=Float.parseFloat(text);
      }
    }
  }
}
