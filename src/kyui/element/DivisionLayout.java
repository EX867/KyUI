package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.util.Rect;
public class DivisionLayout extends Element {
  //add child in up->down or left->right order...
  public int mode=Attributes.FIXED;//proportional or fixed
  public float value=0.5F;//first's size
  public int rotation=Attributes.ROTATE_LEFT;
  public DivisionLayout(String name) {
    super(name);
    children_max=2;
  }
  public DivisionLayout(String name, Rect pos_) {
    super(name);
    pos=pos_;
    children_max=2;
  }
  private float getSize() {
    if (rotation % 2 == 1) {
      return pos.bottom - pos.top;
    } else if (rotation % 2 == 0) {
      return pos.right - pos.left;
    }
    return 1;
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
      float first=value;
      if (rotation == Attributes.ROTATE_RIGHT || rotation == Attributes.ROTATE_DOWN) {
        //        Element t=e1;
        //        e1=e2;
        //        e2=t;
        if (mode == Attributes.PROPORTIONAL) {
          first=(1 - value);
        } else if (mode == Attributes.FIXED) {
          first=getSize() - value;
        }
      }
      if (mode == Attributes.PROPORTIONAL) {
        first=getSize() * first;
      }
      if (rotation % 2 == 0) {//horizontal
        e1.setPosition(new Rect(pos.left + e1.margin, pos.top + e1.margin, pos.left + first - e1.margin, pos.bottom - e1.margin));
        e2.setPosition(new Rect(pos.left + first + e2.margin, pos.top + e2.margin, pos.right - e2.margin, pos.bottom - e2.margin));
      } else {//vertical
        e1.setPosition(new Rect(pos.left + e1.margin, pos.top + e1.margin, pos.right - e1.margin, pos.top + first - e1.margin));
        e2.setPosition(new Rect(pos.left + e2.margin, pos.top + first + e2.margin, pos.right - e2.margin, pos.bottom - e2.margin));
      }
    } else if (size == 1) {
      Element e=children.get(0);
      e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin, pos.bottom - e.margin));
    }
  }
}
