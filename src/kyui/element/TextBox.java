package kyui.element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.ColorExt;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class TextBox extends TextEdit implements DataTransferable {
  EventListener dataChangeListener;
  //modifiable values
  @Attribute
  public String title="";
  public int valueI=0;
  public float valueF=0;
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute(type=Attribute.COLOR)
  public int errorColor;
  @Attribute
  public String rightText="";
  @Attribute
  public int rightTextSize=15;
  @Attribute
  public boolean error=false;
  //...?
  public int strokeWeight=6;
  public boolean transferLineFeedEscape=false;
  //
  public enum NumberType {
    NONE, INTEGER, FLOAT
  }
  @Attribute(setter="setNumberOnly")
  NumberType numberOnly=NumberType.INTEGER;
  Filter numberFilter;
  Filter integerFilter;
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
    numberFilter=new Filter("[^-0-9\\.\b\u007F\uFFFF\u0025\u0026\u0027\u0028]");
    integerFilter=new Filter("\\.");
    filters.add(numberFilter);
    filters.add(integerFilter);
    numberFilter.condition=false;
    fgColor=50;
    textColor=50;
    hintColor=KyUI.Ref.color(50, 50, 50, 150);
    bgColor=KyUI.Ref.color(127);
    padding=textSize;
    lineNumSize=0;
    errorColor=KyUI.Ref.color(255, 0, 0);
    textFont=KyUI.fontMain;
  }
  public TextBox setNumberOnly(NumberType v) {//default true...
    numberFilter.condition=false;
    integerFilter.condition=false;
    if (v != NumberType.NONE) {
      numberFilter.condition=true;
    }
    if (v == NumberType.INTEGER) {
      integerFilter.condition=true;
    }
    numberOnly=v;
    setText(content.toString());
    return this;
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
    //String before=content.toString();
    super.keyTyped(e);
    String str=content.toString();
    setValue(str);
    if (changed) {
      if (onTextChangeListener != null) {
        onTextChangeListener.onEvent(this);
      }
      if (dataChangeListener != null) {
        dataChangeListener.onEvent(this);
      }
    }
    changed=false;
  }
  @Override
  protected void textChange() {
    changed=true;
  }
  private static boolean isInt(String str) {//^-?\\d+$
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
    return str.matches("[+-]?([0-9]*[.])?[0-9]+");
  }
  @Override
  public void moveTo(int line) {//do nothing!
  }
  @Override
  public void render(PGraphics g) {
    g.textFont(textFont);
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
    float centerY=(pos.top + pos.bottom) / 2;
    g.noStroke();
    g.textAlign(PApplet.LEFT, PApplet.CENTER);
    if (!title.equals("")) {
      g.textSize(Math.max(1, textSize * 3 / 4));
      g.fill(fgColor);
      offsetY=(textSize / 4);
      g.text(title + "/ ", pos.left + textSize / 2, pos.top + textSize * 3 / 4);
    } else {
      offsetY=0;
    }
    g.textSize(Math.max(1, textSize));
    //g.textLeading(textSize / 2);
    if (content.hasSelection()) {
      g.fill(selectionColor);
      String selectionPart=content.getSelectionPart(0);
      float selectionBefore=g.textWidth(content.getSelectionPartBefore(0));
      g.rect(pos.left + selectionBefore + lineNumSize + padding, centerY - textSize / 2 + offsetY, pos.left + selectionBefore + g.textWidth(selectionPart) + lineNumSize + padding, centerY + textSize / 2 + offsetY);
    }
    //g.textFont(textFont);
    if (content.empty()) {
      g.fill(hintColor);
      g.text(hint, pos.left + padding, centerY + offsetY);
    } else {
      g.fill(textColor);
      g.text(content.getLine(0), pos.left + padding, centerY + offsetY);
    }
    if (KyUI.focus == this) {
      if (cursorOn) {
        g.fill(textColor);
        float cursorOffsetX=g.textWidth("|") / 2;
        String line=content.getLine(content.line);
        g.text("|", pos.left + g.textWidth(line.substring(0, content.point)) + padding - cursorOffsetX, centerY + offsetY - 3);
      }
    }
    if (!rightText.isEmpty()) {
      g.fill(textColor);
      g.textAlign(PApplet.RIGHT, PApplet.CENTER);
      g.textSize(Math.max(1, rightTextSize));
      g.text(rightText, pos.right - textSize, centerY + offsetY);
    }
    //g.textFont(KyUI.fontMain);
    g.noStroke();
    g.textFont(KyUI.fontMain);
    g.textAlign(PApplet.CENTER, PApplet.CENTER);
  }
  @Override
  public void setText(String text) {//no textChangeListener...because this is called from outside. not changed internally.
    super.setText(text);
    setValue(text);
  }
  void setValue(String text) {
    if (text.equals("")) {
      valueI=0;
      valueF=0;
    } else {
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
  @Override
  public Object get() {
    if (numberOnly == NumberType.INTEGER) {
      return valueI;
    } else if (numberOnly == NumberType.FLOAT) {
      return valueF;
    } else {
      if (transferLineFeedEscape) {
        return content.toString().replaceAll("\\\\n", "\n");
      }
      return content.toString();
    }
  }
  @Override
  public void set(Object value) {
    setText(value.toString());
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}
