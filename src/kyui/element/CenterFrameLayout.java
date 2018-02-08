package kyui.element;
import kyui.core.Element;
import kyui.util.Rect;
public class CenterFrameLayout extends Element {
  public CenterFrameLayout(String name_) {
    super(name_);
  }
  @Override
  public void onLayout() {
    for (Element e : children) {
      float hw=(e.pos.right - e.pos.left) / 2;
      float hh=(e.pos.bottom - e.pos.top) / 2;
      float cx=(pos.right + pos.left) / 2;
      float cy=(pos.bottom + pos.top) / 2;
      e.setPosition(new Rect(cx - hw, cy - hh, cx + hw, cy + hh));
    }
  }
}
