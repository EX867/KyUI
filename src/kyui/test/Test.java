package kyui.test;
import kyui.core.KyUI;
import kyui.element.*;
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
  int count=0;
  TabFrame f;
  @Override
  public void setup() {
    //KyUI.setRoot(new Background("root", color(255, 255, 255)));
    KyUI.start(this);
    f=new TabFrame("tabs", new Rect(0, 0, width, height));
    KyUI.add(f);
    // write your other code
  }
  @Override
  public void draw() {
    KyUI.render(g);
    // write your other code
  }
  @Override
  public void keyTyped() {
    if (key == '>') {
      f.addTab("Tab" + count, new ToggleButton("Asdf" + count));
      count++;
    } else if (key == '<') {
      //f.removeTab(f.size() - 1);
      f.removeTab(0);
    }
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
