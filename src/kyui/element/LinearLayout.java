package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class LinearLayout extends Element {
  protected float offset=0;
  EventListener adjustListener;
  //protected modifiable values
  protected int mode=Attributes.DYNAMIC;
  protected int direction=Attributes.HORIZONTAL;
  protected int fixedSize;
  //temp vars
  private float clickOffset=0;
  private float childrenSize=0;
  private float clickScrollMax=0;
  private Rect cacheRect=new Rect();
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
    fixedSize=60;
  }
  public void setMode(int mode_) {
    offset=0;
    mode=mode_;
  }
  public void setDirection(int dir) {
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
    startClip=(int)offset / fixedSize;
    if (direction == Attributes.HORIZONTAL) {
      endClip=(int)(offset + pos.right - pos.left) / fixedSize + 2;
    } else if (direction == Attributes.VERTICAL) {
      endClip=(int)(offset + pos.bottom - pos.top) / fixedSize + 2;
    }
  }
  public void setOffset(float value) {
    float size=0;
    if (direction == Attributes.HORIZONTAL) {
      size=pos.right - pos.left;
    } else if (direction == Attributes.VERTICAL) {
      size=pos.bottom - pos.top;
    }
    offset=value;
    if (offset > childrenSize - size) {
      offset=childrenSize - size;
    }
    if (offset < 0) {
      offset=0;
    }
    localLayout();
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
    if (mode == Attributes.FIXED) {
      int end=Math.min(children.size(), endClip);
      float first=padding;
      if (startClip > 0) first=0;
      childrenSize=Math.max(0, startClip - 1) * fixedSize;
      if (direction == Attributes.HORIZONTAL) {
        for (int a=Math.max(0, startClip - 1); a < end; a++) {
          Element e=children.get(a);
          if (e.isEnabled()) {
            e.setPosition(new Rect(pos.left - offset + childrenSize + first, pos.top + padding, pos.left - offset + childrenSize + fixedSize - padding + first, pos.bottom - padding));
          }
          childrenSize+=fixedSize + first;
          first=0;
        }
      } else if (direction == Attributes.VERTICAL) {
        for (int a=Math.max(0, startClip - 1); a < end; a++) {
          Element e=children.get(a);
          if (e.isEnabled()) {
            e.setPosition(new Rect(pos.left + padding, pos.top - offset + childrenSize + first, pos.right - padding, pos.top - offset + childrenSize + fixedSize - padding + first));
          }
          childrenSize+=fixedSize + first;
          first=0;
        }
      }
      childrenSize=(size()) * fixedSize;
    } else {
      float first=padding;
      if (direction == Attributes.HORIZONTAL) {
        float width=(float)(pos.right - pos.left) / count;
        for (Element e : children) {
          if (e.isEnabled()) {
            if (mode == Attributes.DYNAMIC) {
              width=e.getPreferredSize().x;
            }
            e.setPosition(new Rect(pos.left - offset + childrenSize + first, pos.top + padding, pos.left - offset + childrenSize + width - padding + first, pos.bottom - padding));
            childrenSize+=width;
            first=0;
          }
        }
      } else if (direction == Attributes.VERTICAL) {
        float height=(float)(pos.bottom - pos.top) / count;
        for (Element e : children) {
          if (e.isEnabled()) {
            if (mode == Attributes.DYNAMIC) {
              height=e.getPreferredSize().y;
            }
            e.setPosition(new Rect(pos.left + padding, pos.top - offset + childrenSize + first, pos.right - padding, pos.top - offset + childrenSize + height - padding + first));
            childrenSize+=height;
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
    cacheRect.set(pos.left + padding, pos.top + padding, pos.right - padding, pos.bottom - padding);
    KyUI.clipRect(g, cacheRect);
    if (mode == Attributes.FIXED) {
      setClip();
    }
    g.noStroke();
  }
  @Override
  public void overlay(PGraphics g) {
    KyUI.removeClip(g);
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (mode == Attributes.STATIC) return true;
    if (mode == Attributes.FIXED) {
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
        if (direction == Attributes.HORIZONTAL) {
          value=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        } else if (direction == Attributes.VERTICAL) {
          value=(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        }
        clickScrollMax=Math.max(Math.abs(value), clickScrollMax);
        setOffset(clickOffset + value);
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
