package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.FileDropEventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.core.PImage;
import sojamo.drop.DropEvent;
public class ImageDrop extends Element implements DataTransferable<PImage> {
  EventListener dataChangeListener;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute
  public int strokeWidth=6;
  public PImage image;
  EventListener onDropListener;
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
    ImageDrop self=this;
    KyUI.addDragAndDrop(this, new FileDropEventListener() {
      @Override
      public void onEvent(DropEvent de) {
        String filename=de.file().getAbsolutePath().replace("\\", "/");
        image=KyUI.Ref.loadImage(filename);
        KyUI.log(" image dropped on " + getName() + ", " + filename);
        if (onDropListener != null) {
          onDropListener.onEvent(self);
        }
        if(dataChangeListener!=null){
          dataChangeListener.onEvent(self);
        }
        invalidate();
      }
    });
  }
  public void setDropListener(EventListener l) {
    onDropListener=l;
  }
  @Override
  public void render(PGraphics g) {
    g.stroke(fgColor);
    g.strokeWeight(strokeWidth);
    super.render(g);
    if (image != null) {
      g.imageMode(KyUI.Ref.CENTER);
      g.pushMatrix();
      g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
      g.scale(Math.min((pos.right - pos.left - padding * 2) / (image.width + 1), (pos.bottom - pos.top - padding * 2) / (image.height + 1)));
      g.image(image, (image.width % 2 == 0) ? 0 : (-0.5F), (image.height % 2 == 0) ? 0 : (-0.5F));
      g.popMatrix();
    }
    g.noStroke();
  }
  @Override
  public PImage get() {
    return image;
  }
  @Override
  public void set(PImage value) {
    image=value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}
