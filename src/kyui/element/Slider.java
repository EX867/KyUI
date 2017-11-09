package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.event.listeners.OnAdjustListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Slider extends Element {
  int strokeWeight=4;
  int direction=Attributes.HORIZONTAL;
  OnAdjustListener adjustListener;
  Number max;
  Number min;
  Number value;
  int sliderSize=3;//half of width
  public Slider(String name) {
    super(name);
  }
  public Slider(String name, Rect pos_) {
    super(name);
    pos=pos_;
  }
  private void init() {
    margin=strokeWeight / 2;
    max=new Integer(0);
    min=new Integer(0);
    value=new Integer(0);
  }
  public void set(Number min_, Number max_, Number value_) {
    min=min_;
    max=max_;
    value=value_;
  }
  public void set(Number min_, Number max_) {
    min=min_;
    max=max_;
    value=Math.min(Math.max(value.floatValue(), min.floatValue()), max.floatValue());
  }
  public void set(Number value_) {
    value=value_;
    value=Math.min(Math.max(value.floatValue(), min.floatValue()), max.floatValue());
  }
  public void setAdjustListener(OnAdjustListener l) {
    adjustListener=l;
  }
  @Override
  public void render(PGraphics g) {
    super.render(g);
    g.strokeWeight(strokeWeight);
    if (direction == Attributes.HORIZONTAL) {
      g.line(pos.left, pos.top, pos.left, pos.bottom);
      g.line(pos.right, pos.top, pos.right, pos.bottom);
      g.line(pos.left, (pos.top + pos.bottom) / 2, pos.right, (pos.top + pos.bottom) / 2);
    } else if (direction == Attributes.VERTICAL) {
      g.line(pos.left, pos.top, pos.right, pos.top);
      g.line(pos.left, pos.bottom, pos.right, pos.bottom);
      g.line((pos.left + pos.right) / 2, pos.top, (pos.left + pos.right) / 2, pos.bottom);
    }
    float min=this.min.floatValue();
    float max=this.max.floatValue();
    float value=this.value.floatValue();
    if (min < max) {
      if (direction == Attributes.HORIZONTAL) {
        float sizeX=pos.right - pos.left;
        float sizeYh=(pos.bottom - pos.top) / 2;
        float posYm=(pos.top + pos.bottom) / 2;
        float point=pos.left + value * (sizeX / (max - min));
        g.rect(point - sliderSize, posYm - sizeYh, point + sliderSize, posYm + sizeYh);
      } else if (direction == Attributes.VERTICAL) {
        float sizeX=pos.bottom - pos.top;
        float sizeYh=(pos.right - pos.left) / 2;
        float posYm=(pos.left + pos.right) / 2;
        float point=pos.top + value * (sizeX / (max - min));
        g.rect(posYm - sizeYh, point + sliderSize, posYm + sizeYh, point - sliderSize);
      }
    }
    g.noStroke();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS || e.getAction() == MouseEvent.MOVE) {
      if (adjustListener != null) {
        adjustListener.onAdjust();
      }
      return false;
    }
    return true;
  }
}
