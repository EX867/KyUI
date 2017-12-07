package kyui.element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class ToggleButton extends Button {
  @Attribute
  public boolean value=false;
  public ToggleButton(String name) {
    super(name);
  }
  public ToggleButton(String name, Rect pos_) {
    super(name, pos_);
  }
  @Override
  protected int getDrawBgColor(PGraphics g) {
    if (pressedL) {
      return (bgColor);
    } else if (entered) {
      if (value) return (ColorExt.brighter(bgColor, 20));
      else return (ColorExt.brighter(bgColor, -20));
    } else {
      if (value) return (ColorExt.brighter(bgColor, 40));
      else return (ColorExt.brighter(bgColor, -40));
    }
  }
  @Override
  public void onPress() {
    if (value) value=false;
    else value=true;
  }
}
