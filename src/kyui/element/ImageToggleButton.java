package kyui.element;
import kyui.editor.Attribute;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.core.PImage;
public class ImageToggleButton extends ImageButton {
  @Attribute
  public boolean value=false;
  public ImageToggleButton(String name) {
    super(name);
  }
  public ImageToggleButton(String name, Rect pos_) {
    super(name, pos_);
  }
  public ImageToggleButton(String name, PImage image_) {
    super(name, image_);
  }
  public ImageToggleButton(String name, Rect pos_, PImage image_) {
    super(name, pos_, image_);
  }
  @Override
  protected int getDrawBgColor(PGraphics g) {
    if (pressedL) {//duplicated,but it is simplest solution I think...
      return bgColor;
    } else if (entered) {
      if (value) {
        return (ColorExt.brighter(bgColor, 20));
      } else return (ColorExt.brighter(bgColor, -20));
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
