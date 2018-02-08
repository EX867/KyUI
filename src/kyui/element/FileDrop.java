package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.FileDropEventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import processing.core.PGraphics;
import sojamo.drop.DropEvent;

import java.io.File;
public class FileDrop extends Element implements DataTransferable<File> {
  EventListener dataChangeListener;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute
  public int strokeWidth=6;
  public File file;
  @Attribute
  public int textSize=15;
  @Attribute
  public String hint="";
  @Attribute(type=Attribute.COLOR)
  public int hintColor;
  EventListener onDropListener;
  public FileDrop(String name) {
    super(name);
    init();
  }
  public FileDrop(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    margin=strokeWidth / 2;
    padding=strokeWidth / 2 + 1;
    bgColor=KyUI.Ref.color(127);
    fgColor=KyUI.Ref.color(50);
    hintColor=KyUI.Ref.color(200, 140);
    FileDrop self=this;
    KyUI.addDragAndDrop(this, new FileDropEventListener() {
      @Override
      public void onEvent(DropEvent de) {
        file=de.file();
        KyUI.log(" file dropped on " + getName() + ", " + de.filePath());
        if (onDropListener != null) {
          onDropListener.onEvent(self);
        }
        if (dataChangeListener != null) {
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
    fill(g, bgColor);
    pos.render(g, -strokeWidth / 2);
    if (file == null) {
      g.fill(fgColor);
      g.textSize(Math.max(1, textSize));
      g.textLeading(Math.max(1, textSize));
      g.text(hint, (pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    } else {
      g.fill(fgColor);
      g.textSize(Math.max(1, textSize));
      g.text(file.getAbsolutePath(), (pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
    }
    g.noStroke();
  }
  @Override
  public File get() {
    return file;
  }
  @Override
  public void set(File value) {
    file=value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}
