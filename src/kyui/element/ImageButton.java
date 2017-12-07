package kyui.element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.core.PImage;
public class ImageButton extends Button {
  @Attribute
  PImage image;
  public ImageButton(String name) {
    super(name);
  }
  public ImageButton(String name, Rect pos_) {
    super(name, pos_);
  }
  public ImageButton(String name, PImage image_) {
    super(name);
    image=image_;
  }
  public ImageButton(String name, Rect pos_, PImage image_) {
    super(name, pos_);
    image=image_;
  }
  @Override
  protected void drawContent(PGraphics g, int textC) {
    if (image == null) return;
    g.imageMode(KyUI.Ref.CENTER);
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    g.image(image, 0, 0);
    g.popMatrix();
  }
}
