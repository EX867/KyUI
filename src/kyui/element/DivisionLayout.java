package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.editor.Attribute;
import kyui.util.Rect;
public class DivisionLayout extends Element {
  public static enum Behavior {
    FIXED, PROPORTIONAL
  }
  //add child in up->down or left->right order...
  @Attribute(layout=Attribute.SELF)
  public Behavior mode=Behavior.FIXED;//proportional or fixed
  @Attribute(layout=Attribute.SELF)
  public float value=40;//first's size
  @Attribute(layout=Attribute.SELF)
  public boolean inverse=false;
  @Attribute(layout=Attribute.SELF)
  public Attributes.Rotation rotation=Attributes.Rotation.LEFT;
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
    if (rotation.ordinal() % 2 == 0) {
      return pos.bottom - pos.top;
    } else {
      return pos.right - pos.left;
    }
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
      if (inverse) {
        Element t=e1;
        e1=e2;
        e2=t;
      }
      if (rotation == Attributes.Rotation.RIGHT || rotation == Attributes.Rotation.DOWN) {
        if (mode == Behavior.PROPORTIONAL) {
          first=(1 - value);
        } else if (mode == Behavior.FIXED) {
          first=getSize() - value;
        }
      }
      if (mode == Behavior.PROPORTIONAL) {
        first=getSize() * first;
      }
      if (rotation.ordinal() % 2 == 1) {//horizontal
        e1.setPosition(new Rect(pos.left + e1.margin + padding, pos.top + e1.margin + padding, pos.left + first - e1.margin - padding / 2, pos.bottom - e1.margin - padding));
        e2.setPosition(new Rect(pos.left + first + e2.margin + padding / 2, pos.top + e2.margin + padding, pos.right - e2.margin - padding, pos.bottom - e2.margin - padding));
      } else {//vertical
        e1.setPosition(new Rect(pos.left + e1.margin + padding, pos.top + e1.margin + padding, pos.right - e1.margin - padding, pos.top + first - e1.margin - padding / 3));
        e2.setPosition(new Rect(pos.left + e2.margin + padding, pos.top + first + e2.margin + padding / 2, pos.right - e2.margin - padding, pos.bottom - e2.margin - padding));
      }
    } else if (size == 1) {
      Element e=children.get(0);
      if (!e.isEnabled()) {
        e=children.get(1);
      }
      e.setPosition(new Rect(pos.left + e.margin + padding, pos.top + e.margin + padding, pos.right - e.margin - padding, pos.bottom - e.margin - padding));
    }
  }
}
