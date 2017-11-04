package kyui.element;
import kyui.core.Element;
import kyui.event.listeners.EventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Button extends Element {
  protected boolean pressed;//this parameter indicates this element have been pressed.
  protected EventListener pressListener;
  //modifiable values
  public int bgColor=50;
  public int textColor=255;
  public int textSize=15;
  public String text="Button";
  //
  public Button(String name) {
    super(name);
  }
  public Button(String name, Rect pos_) {
    super(name);
    pos=pos_;
  }
  public void setPressListener(EventListener el) {
    pressListener=el;
  }
  public EventListener getPressListener() {
    return pressListener;
  }
  @Override
  public void render(PGraphics g) {
    if (pressed) {
      g.fill(ColorExt.brighter(bgColor, 40));
    } else if (entered) {
      g.fill(ColorExt.brighter(bgColor, 20));
    } else {
      g.fill(bgColor);
    }
    pos.render(g);
    g.fill(textColor);
    g.textSize(textSize);
    g.text(text, (pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
  }
  @Override
  public void mouseEvent(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      requestFocus();
      pressed=true;
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressed) {
        pressed=false;
        if (pressListener != null) pressListener.onEvent();
        onPress();
        invalidate();
      }
    }
  }
  public void onPress() {
  }
  @Override
  public void mouseExited() {
    pressed=false;
    invalidate();
  }
  @Override
  public void mouseEntered() {
    invalidate();
  }
}
