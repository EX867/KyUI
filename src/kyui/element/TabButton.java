package kyui.element;
import kyui.core.Attributes;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PGraphics;
public class TabButton extends Button {
  int edgeColor;
  int edgeSize=8;
  private static Rect cacheRect=new Rect(0, 0, 0, 0);
  public TabButton(String name) {
    super(name);
  }
  public TabButton(String name, Rect pos_) {
    super(name, pos_);
  }
  public void render(PGraphics g) {
    textOffset=edgeSize / 2;
    super.render(g);
    if (rotation == Attributes.ROTATE_NONE) {
      cacheRect.set(pos.left, pos.top, pos.right, pos.top + edgeSize);
    } else if (rotation == Attributes.ROTATE_RIGHT) {
      cacheRect.set(pos.right - edgeSize, pos.top, pos.right, pos.bottom);
    } else if (rotation == Attributes.ROTATE_DOWN) {
      cacheRect.set(pos.left, pos.bottom - edgeSize, pos.right, pos.bottom);
    } else if (rotation == Attributes.ROTATE_LEFT) {
      cacheRect.set(pos.left, pos.top, pos.left + edgeSize, pos.bottom);
    }
    g.fill(edgeColor);
    cacheRect.render(g);
  }
}
