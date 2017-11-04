package kyui.element;
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
  public void render(PGraphics g) {
    if (pressed) {
      g.fill(bgColor);
    } else if (entered) {
      if (value) g.fill(ColorExt.brighter(bgColor, 20));
      else g.fill(ColorExt.brighter(bgColor, -20));
    } else {
      if (value) g.fill(ColorExt.brighter(bgColor, 40));
      else g.fill(ColorExt.brighter(bgColor, -40));
    }
    pos.render(g);
    g.fill(textColor);
    g.textSize(textSize);
    g.text(text, (pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
  }
  @Override
  public void onPress() {
    if (value) value=false;
    else value=true;
  }
}
