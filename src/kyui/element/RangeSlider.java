package kyui.element;
import kyui.core.Attributes;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class RangeSlider extends Button {
  protected int strokeWeight=4;
  @Attribute
  public Attributes.Direction direction=Attributes.Direction.VERTICAL;
  EventListener adjustListener;
  //
  @Attribute
  protected float sliderRatio=0;//value of startPoint
  @Attribute
  protected float sliderLength;//(<height)
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute(type=Attribute.COLOR)
  public int sliderBgColor;
  //temp values
  Rect cacheRect=new Rect();
  private float clickRatio=0;
  private float clickOffset=0;
  private float childrenSize=0;
  private float clickScrollMax=0;
  public static int sensitivity=5;
  public RangeSlider(String name) {
    super(name);
    init();
  }
  public RangeSlider(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    margin=strokeWeight / 2;
    fgColor=50;
    sliderBgColor=KyUI.Ref.color(127);
    bgColor=50;
  }
  public void setOffset(float totalSize, float offset) {
    if (offset > totalSize) {
      totalSize=offset;
    }
    if (totalSize == 0) sliderRatio=0;
    else sliderRatio=(float)offset / totalSize;
  }
  public void setLength(float totalSize, float visibleSize) {
    if (visibleSize > totalSize) {
      totalSize=visibleSize;
    }
    sliderLength=visibleSize * getSize() / totalSize;
  }
  public float getOffset(float totalSize) {
    //float size=getSize();
    return totalSize * sliderRatio;
  }
  public void setAdjustListener(EventListener l) {
    adjustListener=l;
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
  public void render(PGraphics g) {
    if (fgColor != 0) {
      g.strokeWeight(strokeWeight);
      g.stroke(fgColor);
    }
    fill(g, sliderBgColor);
    pos.render(g);
    if (direction == Attributes.Direction.VERTICAL) {
      float sliderPoint=pos.top + (pos.bottom - pos.top) * sliderRatio;
      float sliderPointXm=(float)(pos.right + pos.left) / 2;
      float sliderSizeX=(float)(pos.right - pos.left) / 2 - strokeWeight;
      cacheRect.set(sliderPointXm - sliderSizeX, sliderPoint + strokeWeight, sliderPointXm + sliderSizeX, sliderPoint + sliderLength - strokeWeight);
    } else if (direction == Attributes.Direction.HORIZONTAL) {
      float sliderPoint=pos.left + (pos.right - pos.left) * sliderRatio;
      float sliderPointXm=(float)(pos.bottom + pos.top) / 2;
      float sliderSizeX=(float)(pos.bottom - pos.top) / 2 - strokeWeight;
      cacheRect.set(sliderPoint + strokeWeight, sliderPointXm - sliderSizeX, sliderPoint + sliderLength - strokeWeight, sliderPointXm + sliderSizeX);//same...
    }
    g.noStroke();
    g.fill(getDrawBgColor(g));
    cacheRect.render(g);
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getButton() == PApplet.LEFT && (e.getAction() == MouseEvent.PRESS || (pressedL && e.getAction() == MouseEvent.DRAG))) {
      requestFocus();
      float value=0;
      float size=getSize();
      if (direction == Attributes.Direction.HORIZONTAL) {
        value=(KyUI.mouseGlobal.getLast().x - pos.left - sliderLength / 2);
        //value=(KyUI.mouseGlobal.x - KyUI.mouseClick.x) * KyUI.scaleGlobal;
      } else if (direction == Attributes.Direction.VERTICAL) {
        value=(KyUI.mouseGlobal.getLast().y - pos.top - sliderLength / 2);
        //value=(KyUI.mouseGlobal.y - KyUI.mouseClick.y) * KyUI.scaleGlobal;
      }
      if (size == 0) {
        sliderRatio=0;
      } else {
        sliderRatio=value / size;
        //sliderRatio=clickRatio + (value / size);
        sliderRatio=Math.min(Math.max(sliderRatio, 0), (size - sliderLength) / size);
      }
      if (adjustListener != null) {
        adjustListener.onEvent(this);
      }
      invalidate();
      return false;
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        invalidate();
        return false;
      }
    } else if (e.getAction() == MouseEvent.WHEEL) {
      if (pos.contains(KyUI.mouseGlobal.getLast().x, KyUI.mouseGlobal.getLast().y)) {
        float size=getSize();
        sliderRatio=sliderRatio + e.getCount() * sensitivity / size;
        sliderRatio=Math.min(Math.max(sliderRatio, 0), (size - sliderLength) / size);
        if (adjustListener != null) {
          adjustListener.onEvent(this);
        }
        invalidate();
      }
    }
    return true;
  }
}
