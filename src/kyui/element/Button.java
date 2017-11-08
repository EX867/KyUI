package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Button extends Element {
  protected boolean pressed;//this parameter indicates this element have been pressed.
  protected MouseEventListener pressListener;
  //modifiable values
  public int textColor;
  public int textSize;
  public String text="Button";
  public int rotation=Attributes.ROTATE_NONE;
  //in-class values
  protected int textOffsetX=0;
  protected int textOffsetY=0;
  public Button(String name) {
    super(name);
    init();
  }
  public Button(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    bgColor=50;
    textColor=255;
    textSize=15;
    padding=10;
  }
  public void setPressListener(MouseEventListener el) {
    pressListener=el;
  }
  public MouseEventListener getPressListener() {
    return pressListener;
  }
  @Override
  public void render(PGraphics g) {
    if (bgColor != 0) {
      setDrawBgColor(g);
      pos.render(g);
    }
    g.fill(textColor);
    g.textSize(textSize);
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    for (int a=1; a < rotation; a++) {
      g.rotate(KyUI.Ref.radians(90));
    }
    drawContent(g);
    g.popMatrix();
  }
  protected void setDrawBgColor(PGraphics g) {
    if (pressed) {
      g.fill(ColorExt.brighter(bgColor, 40));
    } else if (entered) {
      g.fill(ColorExt.brighter(bgColor, 20));
    } else {
      g.fill(bgColor);
    }
  }
  protected void drawContent(PGraphics g) {
    g.text(text, textOffsetX, textOffsetY);
  }
  @Override
  public boolean mouseEvent(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      pressed=true;
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressed) {
        pressed=false;
        if (pressListener != null) {
          if (!pressListener.onEvent(e)) return false;
        }
        onPress();
        invalidate();
      }
      return false;
    }
    return true;
  }
  public void onPress() {
  }
  @Override
  public void mouseExited() {
    pressed=false;
    super.mouseExited();
  }
  @Override
  public Vector2 getPreferredSize() {
    if (rotation == Attributes.ROTATE_NONE || rotation == Attributes.ROTATE_DOWN) {
      return new Vector2(KyUI.Ref.textWidth(text) + padding * 2, textSize + padding * 2);
    } else {
      return new Vector2(textSize + padding * 2, KyUI.Ref.textWidth(text) + padding * 2);
    }
  }
}
