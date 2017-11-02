package kyui.test;
import kyui.core.KyUI;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class Main extends PApplet {

  public static void main(String[] args) {
    PApplet.main("kyui.test.Main");
  }
  @Override
  public void settings() {
    size(300, 300);
  }
  @Override
  public void setup() {
    KyUI.start(this);
    // write your other code
  }
  @Override
  public void draw() {
    KyUI.render(g);
    // write your other code
  }
  @Override protected void handleKeyEvent(KeyEvent e){
    super.handleKeyEvent(e);
    KyUI.handleKeyEvent(e);
  }
  @Override protected void handleMouseEvent(MouseEvent e){
    super.handleMouseEvent(e);
    KyUI.handleMouseEvent(e);
  }
}
