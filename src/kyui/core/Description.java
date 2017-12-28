package kyui.core;
import kyui.event.EventListener;
import kyui.util.HideInEditor;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
@HideInEditor
public class Description extends Element {
  public String text="";
  public static int textColor=0;
  public static int textSize=15;
  protected Element parent=null;
  public EventListener onShowEvent=(Element e) -> {
    //default action go to down side of parent element.
    float centerX=(parent.pos.left + parent.pos.right) / 2;
    Vector2 size=getPreferredSize();
    float hWidth=size.x / 2;
    e.pos.set(centerX - hWidth, parent.pos.bottom, centerX + hWidth, parent.pos.bottom + size.y);
  };
  Description(String name) {//used in KyUI
    super(name);
  }
  public Description(Element parent_, String text_) {
    super("KyUI:description:element:" + parent_.name);
    parent=parent_;
    bgColor=KyUI.Ref.color(255, 100);
    padding=6;
    text=text_;
  }
  public void onShow() {
    if (onShowEvent != null) {
      onShowEvent.onEvent(this);
    }
  }
  @Override
  public void render(PGraphics g) {
    if (text.length() == 0) {
      return;
    }
    super.render(g);
    g.fill(0);
    g.textSize(Math.max(1, textSize));
    System.out.println("draw text " + getName() + " in " + pos);
    g.text(text, (pos.right + pos.left) / 2, (pos.top + pos.bottom) / 2);
  }
  @Override
  public Vector2 getPreferredSize() {
    KyUI.cacheGraphics.textFont(KyUI.fontMain);
    KyUI.cacheGraphics.textSize(textSize);
    return new Vector2(KyUI.cacheGraphics.textWidth(text) + padding * 2, textSize + padding * 2);
  }
}