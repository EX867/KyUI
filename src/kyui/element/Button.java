package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Button extends Element {
  protected MouseEventListener pressListener;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int textColor;
  @Attribute
  public int textSize=15;
  @Attribute
  public String text="Button";
  @Attribute(layout=Attribute.SELF)
  public Attributes.Rotation rotation=Attributes.Rotation.UP;
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
    bgColor=KyUI.Ref.color(50);
    textColor=KyUI.Ref.color(255);
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
      g.fill(getDrawBgColor(g));
      pos.render(g);
    }
    drawContent(g, textColor);
  }
  protected int getDrawBgColor(PGraphics g) {
    if (pressedL) {
      return (ColorExt.brighter(bgColor, 40));
    } else if (entered) {
      return (ColorExt.brighter(bgColor, 20));
    } else {
      return (bgColor);
    }
  }
  protected void drawContent(PGraphics g, int textC) {
    g.fill(textC);
    g.textSize(Math.max(1,textSize));
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    for (int a=1; a <= rotation.ordinal(); a++) {
      g.rotate(KyUI.Ref.radians(90));
    }
    g.translate(textOffsetX, textOffsetY);
    //float hwidth=getSizeX() / 2;
    //float hheight=getSizeY() / 2;
    if (!text.isEmpty()) {
      g.text(text, 0, 0);
    }
    g.popMatrix();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.RELEASE) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        if (pressedL) {
          onPress();
          if (pressListener != null) {
            if (!pressListener.onEvent(e, index)) return false;
          }
        }
      }
      return false;
    }
    return true;
  }
  public void onPress() {
  }
  private float getSizeX() {
    if (rotation.ordinal() % 2 == 0) {
      return pos.right - pos.left;
    } else {
      return pos.bottom - pos.top;
    }
  }
  private float getSizeY() {
    if (rotation.ordinal() % 2 == 0) {
      return pos.bottom - pos.top;
    } else {
      return pos.right - pos.left;
    }
  }
  @Override
  public Vector2 getPreferredSize() {
    if (rotation == Attributes.Rotation.UP || rotation == Attributes.Rotation.DOWN) {
      return new Vector2(KyUI.Ref.textWidth(text) + padding * 2, textSize + padding * 2);
    } else {
      return new Vector2(textSize + padding * 2, KyUI.Ref.textWidth(text) + padding * 2);
    }
  }
}
