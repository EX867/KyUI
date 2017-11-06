package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class LinearLayout extends Element {
  protected float offset=0;
  //protected modifiable values
  protected int mode=Attributes.DYNAMIC;
  protected int direction=Attributes.HORIZONTAL;
  //temp vars
  private float clickOffset=0;
  private float childrenSize=0;
  private float clickScrollMax=0;
  public LinearLayout(String name_) {
    super(name_);
  }
  public LinearLayout(String name_, Rect pos_) {
    super(name_);
    pos=pos_;
  }
  public void setMode(int mode_) {
    offset=0;
    mode=mode_;
  }
  public void setDirection(int dir) {
    offset=0;
    direction=dir;
  }
  @Override
  public void onLayout() {
    childrenSize=0;
    int count=0;
    for (Element e : children) {
      if (e.isEnabled()) {
        count++;
      }
    }
    if (count == 0) return;//no need to layout.
    if (direction == Attributes.HORIZONTAL) {
      float width=(float)(pos.right - pos.left) / count;
      for (Element e : children) {
        if (e.isEnabled()) {
          if (mode == Attributes.DYNAMIC) {
            width=e.getPreferredSize().x;
          }
          e.setPosition(new Rect(pos.left - offset + childrenSize, pos.top, pos.left - offset + childrenSize + width, pos.bottom));
          childrenSize+=width;
        }
      }
    } else if (direction == Attributes.VERTICAL) {
      float height=(float)(pos.bottom - pos.top) / count;
      for (Element e : children) {
        if (e.isEnabled()) {
          if (mode == Attributes.DYNAMIC) {
            height=e.getPreferredSize().x;
          }
          e.setPosition(new Rect(pos.left, pos.top - offset + childrenSize, pos.right, pos.top - offset + childrenSize + height));
          childrenSize+=height;
        }
      }
    }
  }
  @Override
  public void render(PGraphics g) {
    if (bgColor != 0) {
      g.fill(bgColor);
      pos.render(g);
    }
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (mode == Attributes.STATIC) return true;
    if (e.getAction() == MouseEvent.PRESS) {
      clickOffset=offset;
      clickScrollMax=0;
    } else if (e.getAction() == MouseEvent.DRAG) {
      float value=0;
      int size=0;
      if (direction == Attributes.HORIZONTAL) {
        value=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        size=pos.right - pos.left;
      } else if (direction == Attributes.VERTICAL) {
        value=(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        size=pos.bottom - pos.top;
      }
      clickScrollMax=Math.max(Math.abs(value), clickScrollMax);
      offset=clickOffset + value;//world to input
      if (offset > childrenSize - size) {
        offset=childrenSize - size;
      }
      if (offset < 0) {
        offset=0;
      }
      if (clickScrollMax > KyUI.GESTURE_THRESHOLD) {
        localLayout();
        return false;
      } else {
        offset=clickOffset;
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (clickScrollMax > KyUI.GESTURE_THRESHOLD) return false;
    }
    return true;
  }
}
