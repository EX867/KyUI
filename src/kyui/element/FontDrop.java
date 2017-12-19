package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.FileDropEventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import sojamo.drop.DropEvent;
public class FontDrop extends Element implements DataTransferable<PFont> {
  EventListener dataChangeListener;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute
  public int strokeWidth=6;
  public PFont font;
  @Attribute
  public int textSize=15;
  EventListener onDropListener;
  public FontDrop(String name) {
    super(name);
    init();
  }
  public FontDrop(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    font=KyUI.fontMain;
    margin=strokeWidth / 2;
    padding=strokeWidth / 2 + 1;
    bgColor=KyUI.Ref.color(127);
    fgColor=KyUI.Ref.color(50);
    FontDrop self=this;
    KyUI.addDragAndDrop(this, new FileDropEventListener() {
      @Override
      public void onEvent(DropEvent de) {
        String filename=de.file().getAbsolutePath().replace("\\", "/");
        font=KyUI.Ref.createFont(filename, 20);
        System.out.println("[KyUI] font dropped on " + getName() + ", " + filename);
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
    g.fill(fgColor);
    g.textFont(font);
    g.textSize(Math.max(1, textSize));
    g.text("Aa한글", (pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    g.textFont(KyUI.fontMain);
    g.noStroke();
  }
  @Override
  public PFont get() {
    return font;
  }
  @Override
  public void set(PFont value) {
    font=value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}
