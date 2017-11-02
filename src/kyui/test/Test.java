package kyui.test;
import kyui.core.KyUI;
import kyui.element.AbstractButton;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class Test extends PApplet {
  public static void main(String[] args) {
    PApplet.main("kyui.test.Test");
  }
  @Override
  public void settings() {
    size(300, 300);
  }
  @Override
  public void setup() {
    KyUI.start(this);
    AbstractButton b=new AbstractButton("Asdf");
    KyUI.addChild(b);
    b.pos=new Rect(100, 100, 200, 200);
    // write your other code
  }
  @Override
  public void draw() {
    background(255);
    KyUI.render(g);
    // write your other code
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
