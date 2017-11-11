package kyui.core;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class DropMessenger extends Element {
  public Element start;
  public MouseEvent startEvent;
  public int startIndex;
  public String message;
  public String displayText;
  public DropMessenger(String name, Element start_, MouseEvent startEvent_, int startIndex_, String message_, String displayText_) {
    super(name);
    start=start_;
    startEvent=startEvent_;
    startIndex=startIndex_;
    message=message_;
    displayText=displayText_;
    setPosition(start.pos);
    bgColor=KyUI.Ref.color(255, 100);
  }
  @Override
  public void render(PGraphics g) {
    g.clear();
    super.render(g);
    g.fill(0);
    g.textSize(20);
    g.text(displayText, (pos.right + pos.left) / 2, (pos.top + pos.bottom) / 2);
  }
  @Override
  public void update() {
    setPosition(new Rect(KyUI.mouseGlobal.x - 50, KyUI.mouseGlobal.y - 20, KyUI.mouseGlobal.x + 50, KyUI.mouseGlobal.y + 20));
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS || e.getAction() == MouseEvent.DRAG) {
      requestFocus();
    } else if (e.getAction() == MouseEvent.RELEASE) {
      KyUI.removeLayer();
      KyUI.dropLayer.removeChild(KyUI.dropMessenger.getName());
      KyUI.removeElement(KyUI.dropMessenger.getName());
      KyUI.handleEvent(e);//!!! this is bad! (but this is core thing...)
      return false;
    }
    return super.mouseEvent(e, index);
  }
  public void onEvent(Element end, MouseEvent endEvent, int endIndex) {
    KyUI.getDropEvent(end).onEvent(this, endEvent, endIndex);
  }
}
