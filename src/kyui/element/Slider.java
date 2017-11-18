package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Slider extends Element {
  int strokeWeight=4;
  int direction=Attributes.HORIZONTAL;
  EventListener adjustListener;
  float max;
  float min;
  float value;
  int sliderSize=3;//half of width
  //modifiable values
  public int fgColor=50;
  public Slider(String name) {
    super(name);
    init();
  }
  public Slider(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    bgColor=KyUI.Ref.color(127);
    padding=strokeWeight / 2 + 3;
    max=10;
    min=0;
    value=10;
  }
  public void set(float min_, float max_, float value_) {
    min=min_;
    max=max_;
    value=value_;
  }
  public void set(float min_, float max_) {
    min=min_;
    max=max_;
    value=Math.min(Math.max(value, min), max);
  }
  public void set(float value_) {
    value=value_;
    value=Math.min(Math.max(value, min), max);
  }
  public void setAdjustListener(EventListener l) {
    adjustListener=l;
  }
  @Override
  public void render(PGraphics g) {
    super.render(g);
    g.strokeWeight(strokeWeight);
    g.noFill();
    g.stroke(fgColor);
    if (direction == Attributes.HORIZONTAL) {
      g.line(pos.left + padding, pos.top + padding, pos.left + padding, pos.bottom - padding);
      g.line(pos.right - padding, pos.top + padding, pos.right - padding, pos.bottom - padding);
      g.line(pos.left + padding, (pos.top + pos.bottom) / 2, pos.right - padding, (pos.top + pos.bottom) / 2);
    } else if (direction == Attributes.VERTICAL) {
      g.line(pos.left + padding, pos.top + padding, pos.right - padding, pos.top + padding);
      g.line(pos.left + padding, pos.bottom - padding, pos.right - padding, pos.bottom - padding);
      g.line((pos.left + pos.right) / 2, pos.top + padding, (pos.left + pos.right) / 2, pos.bottom - padding);
    }
    if (min < max) {
      if (direction == Attributes.HORIZONTAL) {
        float sizeX=pos.right - pos.left - 2 * padding;
        float sizeYh=(pos.bottom - pos.top) / 2 - padding;
        float posYm=(pos.top + pos.bottom) / 2;
        float point=pos.left + padding + value * (sizeX / (max - min));
        g.rect(point - sliderSize, posYm - sizeYh, point + sliderSize, posYm + sizeYh);
      } else if (direction == Attributes.VERTICAL) {
        float sizeX=pos.bottom - pos.top - 2 * padding;
        float sizeYh=(pos.right - pos.left) / 2 - padding;
        float posYm=(pos.left + pos.right) / 2;
        float point=pos.top + padding + value * (sizeX / (max - min));
        g.rect(posYm - sizeYh, point + sliderSize, posYm + sizeYh, point - sliderSize);
      }
    }
    g.noStroke();
  }
  private float getSize() {
    if (direction == Attributes.VERTICAL) {
      return pos.bottom - pos.top;
    } else if (direction == Attributes.HORIZONTAL) {
      return pos.right - pos.left;
    }
    return 1;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if ((e.getAction() == MouseEvent.PRESS || e.getAction() == MouseEvent.DRAG)) {//only works with left event...
      if (pressedL) {
        requestFocus();
        float size=getSize();
        if (direction == Attributes.HORIZONTAL) {
          set(min + (max - min) * (KyUI.mouseGlobal.x * KyUI.scaleGlobal - pos.left) / size);
        } else if (direction == Attributes.VERTICAL) {
          set(min + (max - min) * (KyUI.mouseGlobal.y * KyUI.scaleGlobal - pos.top) / size);
        }
        if (adjustListener != null) {
          adjustListener.onEvent();
        }
        invalidate();
        return false;
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        invalidate();
      }
    }
    return true;
  }
}
