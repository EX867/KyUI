package kyui.element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.core.PImage;
public class ImageButton extends Button {
  @Attribute
  public PImage image;
  @Attribute
  public boolean scaled=false;
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
    if (scaled) {
      g.scale(Math.min((pos.right - pos.left - padding * 2) / (image.width + 1), (pos.bottom - pos.top - padding * 2) / (image.height + 1)));
    }
    g.image(image, (image.width % 2 == 0) ? 0 : (-0.5F), (image.height % 2 == 0) ? 0 : (-0.5F));
    g.popMatrix();
  }
  @Override
  public Vector2 getPreferredSize() {
    if (image == null) {
      return new Vector2(padding * 2, padding * 2);
    }
    return new Vector2(padding * 2 + image.width, padding * 2 + image.height);
  }
}
