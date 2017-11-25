package kyui.element;
import kyui.core.CachingFrame;
import kyui.core.KyUI;
import kyui.event.listeners.MouseEventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class ColorButton extends Button {
  //modifiable values
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
    bgColor=127;
    padding=4;
  }
  @Override
  protected void drawContent(PGraphics g, int textC) {
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    float sizeX=(pos.right - pos.left) / 2 - padding;
    float sizeY=(pos.bottom - pos.top) / 2 - padding;
    g.fill(c);
    g.rect(-sizeX, -sizeY, sizeX, sizeY);
    g.popMatrix();
  }
  public static class OpenColorPickerEvent implements MouseEventListener {
    //really, you can use this event listener in other ColorButtons too...
    static CachingFrame colorPickerLayer;//one colorPicker shares many OpenColorPickerEvent.
    static ColorPicker colorPicker;
    static TextBox[] values=new TextBox[7];
    private void initStatic() {
      colorPickerLayer=KyUI.getNewLayer();
      colorPicker=new ColorPicker("KyUI:OpenColorPickerEvent.colorPicker");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.red");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.green");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.blue");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.hue");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.saturation");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.brightness");
      values[0]=new TextBox("KyUI:OpenColorPickerEvent.alpha");
    }
    ColorButton c;
    public OpenColorPickerEvent(ColorButton c_) {
      c=c_;
      if (colorPickerLayer == null) {//check if it not initialized...
        initStatic();
      }
    }
    @Override
    public boolean onEvent(MouseEvent e, int index) {
      //System.out.println("color button pressed");
      return false;
    }
  }
}
