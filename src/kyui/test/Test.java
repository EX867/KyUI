package kyui.test;
import kyui.core.KyUI;
import kyui.element.TextBox;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class Test extends PApplet {
  public static void main(String[] args) {
    PApplet.main("kyui.test.Test");
  }
  public void settings() {
    size(800, 800);
  }
  public void setup() {
    KyUI.start(this);
    KyUI.add(new TextBox("asdf", new Rect(0, 0, 800, 800)));
    KyUI.changeLayout();
    KyUI.<TextBox>get2("asdf").textSize=15;
  }
  public void draw() {
    KyUI.render(g);
  }
  @Override
  protected void handleKeyEvent(KeyEvent e) {
    super.handleKeyEvent(e);
    KyUI.handleEvent(e);
  }
  @Override
  protected void handleMouseEvent(MouseEvent e) {
    super.handleMouseEvent(e);
    KyUI.handleEvent(e);
  }
}
