package kyui.element;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.core.PImage;
public class ImageToggleButton extends ToggleButton {
  PImage image;
  public ImageToggleButton(String name, PImage image_) {
    super(name);
    image=image_;
  }
  public ImageToggleButton(String name, Rect pos_, PImage image_) {
    super(name, pos_);
    image=image_;
  }
  @Override
  protected void drawContent(PGraphics g) {
    g.image(image, 0, 0);
  }
}
