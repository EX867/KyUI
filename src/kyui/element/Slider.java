package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Slider extends Element implements DataTransferable {
  EventListener dataChangeListener;
  int strokeWeight=4;
  @Attribute
  Attributes.Direction direction=Attributes.Direction.HORIZONTAL;
  EventListener adjustListener;
  @Attribute(setter="setMax")
  public float max;
  @Attribute(setter="setMin")
  public float min;
  @Attribute(setter="set")
  public float value;
  @Attribute
  public int sliderSize=3;//half of width
  //modifiable values
  @Attribute(type=Attribute.COLOR)
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
    padding=strokeWeight + 2;
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
    if (direction == Attributes.Direction.HORIZONTAL) {
      g.line(pos.left + padding, pos.top + padding, pos.left + padding, pos.bottom - padding);
      g.line(pos.right - padding, pos.top + padding, pos.right - padding, pos.bottom - padding);
      g.line(pos.left + padding, (pos.top + pos.bottom) / 2, pos.right - padding, (pos.top + pos.bottom) / 2);
    } else if (direction == Attributes.Direction.VERTICAL) {
      g.line(pos.left + padding, pos.top + padding, pos.right - padding, pos.top + padding);
      g.line(pos.left + padding, pos.bottom - padding, pos.right - padding, pos.bottom - padding);
      g.line((pos.left + pos.right) / 2, pos.top + padding, (pos.left + pos.right) / 2, pos.bottom - padding);
    }
    if (min < max) {
      if (direction == Attributes.Direction.HORIZONTAL) {
        float sizeX=pos.right - pos.left - 2 * padding;
        float sizeYh=(pos.bottom - pos.top) / 2 - padding;
        float posYm=(pos.top + pos.bottom) / 2;
        float point=pos.left + padding + value * (sizeX / (max - min));
        g.rect(point - sliderSize, posYm - sizeYh, point + sliderSize, posYm + sizeYh);
      } else if (direction == Attributes.Direction.VERTICAL) {
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
    if (direction == Attributes.Direction.VERTICAL) {
      return pos.bottom - pos.top;
    } else if (direction == Attributes.Direction.HORIZONTAL) {
      return pos.right - pos.left;
    }
    return 1;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS || (pressedL && e.getAction() == MouseEvent.DRAG)) {//only works with left event...
      if (pressedL || e.getAction() == MouseEvent.PRESS) {
        requestFocus();
        float size=getSize();
        if (direction == Attributes.Direction.HORIZONTAL) {
          set(min + (max - min) * (KyUI.mouseGlobal.x * KyUI.scaleGlobal - pos.left) / size);
        } else if (direction == Attributes.Direction.VERTICAL) {
          set(min + (max - min) * (KyUI.mouseGlobal.y * KyUI.scaleGlobal - pos.top) / size);
        }
        if (adjustListener != null) {
          adjustListener.onEvent(this);
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
  //for attribute...
  public void setMin(float min_) {
    set(min_, max);
  }
  public void setMax(float max_) {
    set(min, max_);
  }
  @Override
  public Object get() {
    return value;
  }
  @Override
  public void set(Object value) {
    this.value=(float)value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}
