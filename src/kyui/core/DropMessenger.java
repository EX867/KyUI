package kyui.core;
import kyui.util.Task;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class DropMessenger extends Element {
  public Element start;
  public MouseEvent startEvent;
  public int startIndex;
  public String message;
  public String displayText;
  //
  public int textSize=20;
  //
  protected Visual defaultVisual=new Visual() {
    @Override
    public void render(DropMessenger e, PGraphics g) {
      fill(g, bgColor);
      pos.render(g);
      g.fill(0);
      g.textSize(e.textSize);
      g.text(e.displayText, (e.pos.right + e.pos.left) / 2, (e.pos.top + e.pos.bottom) / 2);
    }
  };
  protected Visual visual=defaultVisual;
  public DropMessenger(String name, Element start_, MouseEvent startEvent_, int startIndex_, String message_, String displayText_) {
    super(name);
    start=start_;
    startEvent=startEvent_;
    startIndex=startIndex_;
    message=message_;
    displayText=displayText_;
    padding=10;
    KyUI.cacheGraphics.textSize(textSize);//is this making sync error?
    float hwidth=KyUI.cacheGraphics.textWidth(displayText) / 2 + padding;
    float hheight=textSize / 2 + padding;
    setPosition(new Rect(KyUI.mouseGlobal.getLast().x - hwidth, KyUI.mouseGlobal.getLast().y - hheight, KyUI.mouseGlobal.getLast().x + hwidth, KyUI.mouseGlobal.getLast().y + hheight));
    bgColor=KyUI.Ref.color(255, 100);
  }
  @Override
  public void render(PGraphics g) {
    g.clear();
    if (visual != null) {
      visual.render(this, g);
    }
  }
  public void setVisual(Visual v) {
    if (v == null) {
      visual=defaultVisual;
    } else {
      visual=v;
    }
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    float hwidth=(pos.right - pos.left) / 2;
    float hheight=(pos.bottom - pos.top) / 2;
    setPosition(pos.set(KyUI.mouseGlobal.getLast().x - hwidth, KyUI.mouseGlobal.getLast().y - hheight, KyUI.mouseGlobal.getLast().x + hwidth, KyUI.mouseGlobal.getLast().y + hheight));
    return true;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS || e.getAction() == MouseEvent.DRAG) {
      requestFocus();
    } else if (e.getAction() == MouseEvent.RELEASE) {
      KyUI.dropLayer.removeChild(getName());
      KyUI.removeLayer();
      KyUI.taskManager.executeAll();
      KyUI.handleEvent(e);//!!! this is bad! (but this is core thing...)
      KyUI.taskManager.addTask(new Task() {
        @Override
        public void execute(Object data) {
          KyUI.dropMessenger=null;
        }
      }, null);
      return false;
    }
    return super.mouseEvent(e, index);
  }
  public void onEvent(Element end, MouseEvent endEvent, int endIndex) {
    KyUI.getDropEvent(end).onEvent(this, endEvent, endIndex);
  }
  public static interface Visual {
    public void render(DropMessenger e, PGraphics g);
  }
}
