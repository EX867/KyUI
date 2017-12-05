package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class LinearLayout extends Element {
  public static enum Behavior {
    STATIC, DYNAMIC, FIXED, LEAVE
  }
  protected float offset=0;
  EventListener adjustListener;
  //protected modifiable values
  protected Behavior mode=Behavior.DYNAMIC;
  protected Attributes.Direction direction=Attributes.Direction.HORIZONTAL;
  protected int fixedSize;
  //temp vars
  private float clickOffset=0;
  private float childrenSize=0;
  private float clickScrollMax=0;
  private Rect cacheRect=new Rect();
  protected boolean draggable=false;
  public LinearLayout(String name_) {
    super(name_);
    init();
  }
  public LinearLayout(String name_, Rect pos_) {
    super(name_);
    pos=pos_;
    init();
  }
  private void init() {
    clipping=true;
    fixedSize=60;
  }
  public void setMode(Behavior mode_) {
    offset=0;
    mode=mode_;
  }
  public void setDirection(Attributes.Direction dir) {
    offset=0;
    direction=dir;
  }
  public void setFixedSize(int size) {
    fixedSize=size;
    localLayout();
  }
  public float getTotalSize() {
    return childrenSize;//return already calculated value.
  }
  void setClip() {
    if (mode == Behavior.FIXED) {
      startClip=(int)offset / fixedSize;
      if (direction == Attributes.Direction.HORIZONTAL) {
        endClip=(int)(offset + pos.right - pos.left) / fixedSize + 2;
      } else if (direction == Attributes.Direction.VERTICAL) {
        endClip=(int)(offset + pos.bottom - pos.top) / fixedSize + 2;
      }
    }
  }
  public void setOffset(float value) {
    float size=0;
    if (direction == Attributes.Direction.HORIZONTAL) {
      size=pos.right - pos.left;
    } else if (direction == Attributes.Direction.VERTICAL) {
      size=pos.bottom - pos.top;
    }
    offset=value;
    if (offset > childrenSize - size) {
      offset=childrenSize - size;
    }
    if (offset < 0) {
      offset=0;
    }
  }
  public void setAdjustListener(EventListener l) {
    adjustListener=l;
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
    setClip();
    if (mode == Behavior.FIXED) {
      int end=Math.min(children.size(), endClip);
      float first=padding;
      if (startClip > 0) first=0;
      childrenSize=Math.max(0, startClip - 1) * fixedSize;
      if (direction == Attributes.Direction.HORIZONTAL) {
        for (int a=Math.max(0, startClip - 1); a < end; a++) {
          Element e=children.get(a);
          if (e.isEnabled()) {
            e.setPosition(new Rect(pos.left - offset + childrenSize + first, pos.top + padding, pos.left - offset + childrenSize + fixedSize + first, pos.bottom - padding));
          }
          childrenSize+=fixedSize + first + padding;
          first=0;
        }
      } else if (direction == Attributes.Direction.VERTICAL) {
        for (int a=Math.max(0, startClip - 1); a < end; a++) {
          Element e=children.get(a);
          if (e.isEnabled()) {
            e.setPosition(new Rect(pos.left + padding, pos.top - offset + childrenSize + first, pos.right - padding, pos.top - offset + childrenSize + fixedSize + first));
          }
          childrenSize+=fixedSize + first + padding;
          first=0;
        }
      }
      childrenSize=(size()) * fixedSize;
    } else {
      float first=padding;
      if (direction == Attributes.Direction.HORIZONTAL) {
        float width=(float)(pos.right - pos.left) / count;
        for (Element e : children) {
          if (e.isEnabled()) {
            if (mode == Behavior.DYNAMIC) {
              width=e.getPreferredSize().x;
            } else if (mode == Behavior.LEAVE) {
              width=e.pos.right - e.pos.left;//unstable, but don't fix
            }
            e.setPosition(new Rect(pos.left - offset + childrenSize + first, pos.top + padding, pos.left - offset + childrenSize + width + first, pos.bottom - padding));
            childrenSize+=width + first + padding;
            first=0;
          }
        }
      } else if (direction == Attributes.Direction.VERTICAL) {
        float height=(float)(pos.bottom - pos.top) / count;
        for (Element e : children) {
          if (e.isEnabled()) {
            if (mode == Behavior.DYNAMIC) {
              height=e.getPreferredSize().y;
            } else if (mode == Behavior.LEAVE) {
              height=e.pos.bottom - e.pos.top;
            }
            e.setPosition(new Rect(pos.left + padding, pos.top - offset + childrenSize + first, pos.right - padding, pos.top - offset + childrenSize + height + first));
            childrenSize+=height + first + padding;
            first=0;
          }
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
    if (mode == Behavior.FIXED) {
      setClip();
    }
    g.noStroke();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.WHEEL) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        setOffset(offset + e.getCount() * KyUI.WHEEL_COUNT);
        localLayout();
        return false;
      }
    }
    return true;
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (!draggable) return true;
    if (mode == Behavior.STATIC) return true;
    if (mode == Behavior.FIXED) {
      setClip();
    }
    if (e.getAction() == MouseEvent.PRESS) {
      clickOffset=offset;
      clickScrollMax=0;
      if (adjustListener != null) {
        adjustListener.onEvent(this);
      }
    } else if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL) {
        requestFocus();
        float value=0;
        if (direction == Attributes.Direction.HORIZONTAL) {
          value=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        } else if (direction == Attributes.Direction.VERTICAL) {
          value=(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        }
        clickScrollMax=Math.max(Math.abs(value), clickScrollMax);
        setOffset(clickOffset + value);
        localLayout();
        if (clickScrollMax > KyUI.GESTURE_THRESHOLD) {
          if (adjustListener != null) {
            adjustListener.onEvent(this);
          }
          return false;
        } else {
          offset=clickOffset;
        }
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL && clickScrollMax > KyUI.GESTURE_THRESHOLD) return false;
    }
    return true;
  }
}
