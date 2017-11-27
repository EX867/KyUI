package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class StatusBar extends Button {
  //modifiable values
  boolean error=false;
  public int tabColor1;//normal
  public int tabColor2;//error
  public int edgeSize=8;
  //temp values
  private Rect cacheRect=new Rect();
  public StatusBar(String name) {
    super(name);
    init();
  }
  void init() {
    tabColor1=KyUI.Ref.color(0, 0, 255);
    tabColor2=KyUI.Ref.color(127, 0, 0);
  }
  public void render(PGraphics g) {
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    cacheRect.set(pos.left, pos.top, pos.right, pos.top + edgeSize);
    textOffsetX=(int)(-(pos.right - pos.left) / 2 + padding);
    textOffsetY=edgeSize / 2;
    super.render(g);//FIX
    if (error) {
      g.fill(tabColor2);
    } else {
      g.fill(tabColor1);
    }
    cacheRect.render(g);
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  public void setText(String text_) {
    text=text_;
    invalidate();
  }
  public void setError(boolean v) {
    error=v;
    invalidate();
  }
}
