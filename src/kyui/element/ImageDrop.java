package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.FileDropEventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.core.PImage;
import sojamo.drop.DropEvent;
public class ImageDrop extends Element {
  //modifiable values
  public int fgColor;
  public int strokeWidth=6;
  public PImage display;
  public ImageDrop(String name) {
    super(name);
    init();
  }
  public ImageDrop(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    margin=strokeWidth / 2;
    padding=strokeWidth / 2 + 1;
    bgColor=KyUI.Ref.color(127);
    fgColor=KyUI.Ref.color(50);
    KyUI.addDragAndDrop(this, new FileDropEventListener() {
      @Override
      public void onEvent(DropEvent de) {
        String filename=de.file().getAbsolutePath().replace("\\", "/");
        display=KyUI.Ref.loadImage(filename);
        System.out.println("[KyUI]image dropped on " + getName() + ", " + filename);
        invalidate();
      }
    });
  }
  @Override
  public void render(PGraphics g) {
    g.stroke(fgColor);
    g.strokeWeight(strokeWidth);
    super.render(g);
    if (display != null) {
      g.imageMode(KyUI.Ref.CENTER);
      g.pushMatrix();
      g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
      g.scale(Math.min((pos.right - pos.left - padding * 2) / (display.width + 1), (pos.bottom - pos.top - padding * 2) / (display.height + 1)));
      g.image(display, (display.width % 2 == 0) ? 0 : (-0.5F), (display.height % 2 == 0) ? 0 : (-0.5F));
      g.popMatrix();
    }
    g.noStroke();
  }
}
