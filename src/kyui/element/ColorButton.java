package kyui.element;
import kyui.core.CachingFrame;
import kyui.core.KyUI;
import kyui.event.MouseEventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class ColorButton extends Button {
  //modifiable values
  public int c=0xFF000000;
  public ColorButton(String name) {
    super(name);
    init();
  }
  public ColorButton(String name, Rect pos_) {
    super(name, pos_);
    init();
  }
  private void init() {
    padding=6;
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
  public static class OpenColorPickerEvent implements MouseEventListener {
    //really, you can use this event listener in other ColorButtons too...
    static CachingFrame colorPickerLayer;//one colorPicker shares many OpenColorPickerE
    static ColorPickerFull colorPicker;
    ColorButton c;
    public OpenColorPickerEvent(ColorButton c_) {
      c=c_;
      if (colorPickerLayer == null) {
        colorPickerLayer=KyUI.getNewLayer();
        colorPicker=new ColorPickerFull("KyUI:OpenColorPickerEvent.colorPicker");
      }
    }
    @Override
    public boolean onEvent(MouseEvent e, int index) {
      //System.out.println("color button pressed");
      return false;
    }
  }
}
