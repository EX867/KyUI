package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class RangeSlider extends Element {
  int strokeWeight=6;
  int direction=Attributes.VERTICAL;
  //
  float sliderRatio;//ratio of startPoint
  int sliderLength;
  //modifiable values
  int fgColor;
  //temp values
  Rect cacheRect=new Rect();
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
  }
  public void set(int totalSize, int visibleSize, int offset) {
    sliderLength=visibleSize * visibleSize * totalSize;
    sliderRatio=(float)offset / totalSize;
  }
  public float getOffset(int totalSize) {
    int size=1;
    if (direction == Attributes.VERTICAL) {
      size=pos.bottom - pos.top - sliderLength;
    } else if (direction == Attributes.HORIZONTAL) {
      size=pos.right - pos.left - sliderLength;
    }
    return totalSize * sliderRatio / size;
  }
  @Override
  public void render(PGraphics g) {
    g.fill(bgColor);
    g.strokeWeight(strokeWeight);
    g.stroke(fgColor);
    pos.render(g);
    if (direction == Attributes.VERTICAL) {
      float sliderPoint=(pos.bottom - pos.top) * sliderRatio;
      float sliderPointXm=(float)(pos.right + pos.left) / 2;
      float sliderSizeX=(float)(pos.right - pos.left) * 2 / 5;
      cacheRect.set(sliderPointXm - sliderSizeX, sliderPoint - sliderLength, sliderPointXm + sliderSizeX, sliderPoint + sliderLength);
    } else if (direction == Attributes.HORIZONTAL) {
      float sliderPoint=(pos.right - pos.left) * sliderRatio;
      float sliderPointXm=(float)(pos.bottom + pos.top) / 2;
      float sliderSizeX=(float)(pos.bottom - pos.top) * 2 / 5;
      cacheRect.set(sliderPointXm - sliderSizeX, sliderPoint - sliderLength, sliderPointXm + sliderSizeX, sliderPoint + sliderLength);//same...
    }
    cacheRect.render(g);
    g.noStroke();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    return true;
  }
}
