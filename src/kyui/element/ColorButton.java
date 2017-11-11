package kyui.element;
import kyui.event.listeners.MouseEventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class ColorButton extends Button {
  int c;
  public ColorButton(String name, int c_) {
    super(name);
    c=c_;
  }
  public ColorButton(String name, Rect pos_, int c_) {
    super(name, pos_);
    c=c_;
  }
  @Override
  protected void drawContent(PGraphics g, int textC) {
    float sizeX=(pos.right - pos.left) * 4 / 10;
    float sizeY=(pos.bottom - pos.top) * 4 / 10;
    g.fill(c);
    g.rect(-sizeX, -sizeY, sizeX, sizeY);
  }
  public class OpenColorPickerEvent implements MouseEventListener {
    @Override
    public boolean onEvent(MouseEvent e, int index) {
      return true;
    }
  }
}
