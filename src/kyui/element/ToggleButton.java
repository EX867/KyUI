package kyui.element;
import kyui.core.KyUI;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class ToggleButton extends Button {
  public boolean value=false;
  public ToggleButton(String name) {
    super(name);
  }
  public ToggleButton(String name, Rect pos_) {
    super(name, pos_);
  }
  @Override
  protected void setDrawBgColor(PGraphics g) {
    if (pressed) {
      g.fill(bgColor);
    } else if (entered) {
      if (value) g.fill(ColorExt.brighter(bgColor, 20));
      else g.fill(ColorExt.brighter(bgColor, -20));
    } else {
      if (value) g.fill(ColorExt.brighter(bgColor, 40));
      else g.fill(ColorExt.brighter(bgColor, -40));
    }
  }
  @Override
  public void onPress() {
    if (value) value=false;
    else value=true;
  }
}
