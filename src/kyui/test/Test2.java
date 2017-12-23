package kyui.test;
import kyui.core.KyUI;
import kyui.element.LinearList;
import kyui.loader.ElementLoader;
import kyui.loader.LayoutLoader;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class Test2 extends PApplet {
  public static void main(String[] args) {
    PApplet.main("kyui.test.Test2");
  }
  public void settings() {
    size(800, 800);
  }
  public void setup() {
    KyUI.start(this);
    ElementLoader.loadOnStart(new LinearList("test2-ele"), new LinearList("test2-ins"));
    LayoutLoader.loadXML(KyUI.get("KyUI:0"), loadXML("TestLayout.xml"));
  }
  public void draw() {
    background(0);
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
