package kyui.element;
import kyui.core.Element;
import kyui.util.Rect;
public class FrameLayout extends Element {
  public FrameLayout(String name) {
    super(name);
  }
  public FrameLayout(String name, Rect pos_) {
    super(name);
    pos=pos_;
  }
  @Override
  public void onLayout() {
    for (Element e : children) {
      e.setPosition(new Rect(pos.left + e.margin + padding, pos.top + e.margin + padding, pos.right - e.margin - padding, pos.bottom - e.margin - padding));
    }
  }
}
