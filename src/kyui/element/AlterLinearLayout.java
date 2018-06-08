package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.util.Rect;

import java.util.HashMap;
public class AlterLinearLayout extends Element {
  //unscrollable linear layout with android's layout property : fixed, proportional, static(match parent), dynamic(wrap content)
  //you cannot edit children specific property in kyui.editor.main for now...
  protected HashMap<Element, LayoutData> layoutData = new HashMap<>(11);
  public Attributes.Direction direction = Attributes.Direction.HORIZONTAL;
  public float interval = 0;
  public AlterLinearLayout(String name_) {
    super(name_);
    padding = 0;
    modifyChildrenTask = new ModifyChildrenTask(this) {
      @Override
      public void execute(Object raw) {
        if (raw instanceof AddChildData) {
          AddChildData data = (AddChildData)raw;
          layoutData.put(data.element, new LayoutData(LayoutType.OPPOSITE_RATIO, 1));
        } else if (raw instanceof RemoveChildData) {
          RemoveChildData data = (RemoveChildData)raw;
          layoutData.remove(data.element);
        }
        super.execute(raw);
      }
    };
  }
  @Override
  public void onLayout() {
    float[] size = new float[children.size()];
    float totalSize = 0;
    int childrenCount = 0;
    int staticChildrenCount = 0;
    //get static's size
    float selfSize = getSize();
    float selfOppositeSize = getOppositeSize();
    for (int a = 0; a < children.size(); a++) {
      size[a] = 0;
      if (children.get(a).isEnabled()) {
        LayoutData dat = layoutData.get(children.get(a));
        if (dat.behavior == LayoutType.FIXED) {
          size[a] = dat.value;
        } else if (dat.behavior == LayoutType.PROPORTIONAL) {
          size[a] = selfSize * dat.value;
        } else if (dat.behavior == LayoutType.OPPOSITE_RATIO) {
          size[a] = selfOppositeSize * dat.value;
        } else if (dat.behavior == LayoutType.STATIC) {
          staticChildrenCount++;
        } else if (dat.behavior == LayoutType.DYNAMIC) {
          size[a] = children.get(a).getPreferredSize().x;
        }//no else
        childrenCount++;
        totalSize += size[a];
      }
    }
    totalSize += interval * (childrenCount - 1) + padding * (childrenCount + 1);
    float staticSize = (selfSize - totalSize) / staticChildrenCount;
    for (int a = 0; a < children.size(); a++) {
      if (children.get(a).isEnabled()) {
        LayoutData dat = layoutData.get(children.get(a));
        if (dat.behavior == LayoutType.STATIC) {
          size[a] = staticSize;
        }
      }
    }
    if (direction == Attributes.Direction.HORIZONTAL) {
      float right = pos.left + padding;
      for (int a = 0; a < children.size(); a++) {
        Element c = children.get(a);
        if (c.isEnabled()) {
          c.setPosition(new Rect(right + c.margin, pos.top + padding + c.margin, right + size[a] - c.margin, pos.bottom - padding - c.margin));
          right += size[a] + padding + interval;
        }
      }
    } else {
      float down = pos.top + padding;
      for (int a = 0; a < children.size(); a++) {
        Element c = children.get(a);
        if (c.isEnabled()) {
          c.setPosition(new Rect(pos.left + padding + c.margin, down + c.margin, pos.right - padding - c.margin, down + size[a] - c.margin));
          down += size[a] + padding + interval;
        }
      }
    }
  }
  public float getSize() {
    if (direction == Attributes.Direction.HORIZONTAL) {
      return pos.right - pos.left;
    } else {//vertical
      return pos.bottom - pos.top;
    }
  }
  public float getOppositeSize() {
    if (direction == Attributes.Direction.HORIZONTAL) {
      return pos.bottom - pos.top;
    } else {//vertical
      return pos.right - pos.left;
    }
  }
  public LayoutData get(Element e) {
    return layoutData.get(e);
  }
  public void set(Element e, LayoutType dat, float value) {
    layoutData.get(e).behavior = dat;
    layoutData.get(e).value = value;
  }
  public static class LayoutData {
    public LayoutType behavior;
    public float value;
    public LayoutData(LayoutType behavior_, float value_) {
      behavior = behavior_;
      value = value_;
    }
  }
  public static enum LayoutType {
    FIXED, PROPORTIONAL, OPPOSITE_RATIO, STATIC, DYNAMIC
  }
}
