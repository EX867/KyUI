package kyui.element;
import com.sun.istack.internal.Nullable;
import kyui.core.CachingFrame;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.MouseEventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.HashMap;
public class ColorButton extends Button implements DataTransferable<Integer> {
  EventListener dataChangeListener;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int c=0xFFFFFFFF;
  public ColorButton(String name) {
    super(name);
    init();
  }
  public ColorButton(String name, Rect pos_) {
    super(name, pos_);
    init();
  }
  private void init() {
    padding=8;
    bgColor=KyUI.Ref.color(90);
  }
  @Override
  protected void drawContent(PGraphics g, int textC) {
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    float sizeX=(pos.right - pos.left) / 2 - padding;
    float sizeY=(pos.bottom - pos.top) / 2 - padding;
    fill(g, c);
    g.rect(-sizeX, -sizeY, sizeX, sizeY);
    g.popMatrix();
  }
  @Override
  public Integer get() {
    return c;
  }
  @Override
  public void set(Integer value) {
    c=value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
  public static class OpenColorPickerEvent implements MouseEventListener {
    //really, you can use this event listener in other ColorButtons too...
    static CachingFrame colorPickerLayer;//one colorPicker shares many OpenColorPickerE
    public static ColorPickerFull colorPicker;
    static HashMap<ColorButton, EventListener> acceptEv;
    static ColorButton last;//not works in nested situation(colorbutton is in colorpicker)
    MouseEventListener onPressListener;
    ColorButton c;
    public OpenColorPickerEvent(ColorButton c_) {
      this(c_, null);
    }
    public OpenColorPickerEvent(ColorButton c_, @Nullable MouseEventListener onPressListener) {//add this object to colorbutton.
      c=c_;
      if (colorPickerLayer == null) {
        acceptEv=new HashMap<ColorButton, EventListener>(100);
        colorPickerLayer=KyUI.getNewLayer();
        colorPicker=new ColorPickerFull("KyUI:OpenColorPickerEvent.colorPicker");
        colorPickerLayer.addChild(colorPicker);
        colorPicker.acceptButton.setPressListener((MouseEvent e, int index) -> {
          if (acceptEv.get(last) != null) {
            acceptEv.get(last).onEvent(last);
          }
          KyUI.removeLayer();
          return false;
        });
        KyUI.addResizeListener((int w, int h) -> {
          //if (!KyUI.isRootPresent(colorPickerLayer)) {
          //  colorPickerLayer.resize((int)e.pos.right/* - e.pos.left*/, (int)e.pos.bottom/* - e.pos.top*/);
          //}
          colorPicker.setPosition(new Rect(0, 0, w, h));
        });
        colorPicker.setPosition(new Rect(0, 0, colorPickerLayer.display.width, colorPickerLayer.display.height));
      }
      acceptEv.put(c, (Element e) -> {
        c.c=colorPicker.colorPicker.getColor();
        if (c.dataChangeListener != null) {
          c.dataChangeListener.onEvent(c);
        }
      });
      c.setPressListener(this);
    }
    @Override
    public boolean onEvent(MouseEvent e, int index) {
      last=c;
      if (onPressListener != null) {
        onPressListener.onEvent(e, index);
      }
      colorPicker.setColorRGB(c.c);
      KyUI.addLayer(colorPickerLayer);
      return false;
    }
  }
}
