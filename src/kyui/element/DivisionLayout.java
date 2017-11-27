package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.util.Rect;
public class DivisionLayout extends Element {
  public float ratio=0.5F;//first's size
  public boolean inverse=false;
  public int direction=Attributes.HORIZONTAL;
  public DivisionLayout(String name) {
    super(name);
    children_max=2;
  }
  public DivisionLayout(String name, Rect pos_) {
    super(name);
    pos=pos_;
    children_max=2;
  }
  @Override
  public void onLayout() {
    int size=0;
    for (Element child : children) {
      if (child.isEnabled()) {
        size++;
      }
    }
    if (size == 2) {
      Element e1=children.get(0);
      Element e2=children.get(1);
      float ratio_=ratio;
      if (inverse) {
        Element t=e1;
        e1=e2;
        e2=t;
        ratio_=1 - ratio;
      }
      if (direction == Attributes.HORIZONTAL) {
        int first=(int)((pos.right - pos.left) * ratio_);
        e1.setPosition(new Rect(pos.left + e1.margin, pos.top + e1.margin, pos.left + first - e1.margin, pos.bottom - e1.margin));
        e2.setPosition(new Rect(pos.left + first + e2.margin, pos.top + e2.margin, pos.right - e2.margin, pos.bottom - e2.margin));
      } else if (direction == Attributes.VERTICAL) {
        int first=(int)((pos.bottom - pos.top) * ratio_);
        e1.setPosition(new Rect(pos.left + e1.margin, pos.top + e1.margin, pos.right - e1.margin, pos.top + first - e1.margin));
        e2.setPosition(new Rect(pos.left + e2.margin, pos.top + first + e2.margin, pos.right - e2.margin, pos.bottom - e2.margin));
      }
    } else if (size == 1) {
      Element e=children.get(0);
      e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin, pos.bottom - e.margin));
    }
  }
}
