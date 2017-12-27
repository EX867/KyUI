package kyui.test;
import kyui.core.KyUI;
import kyui.loader.ElementLoader;
import kyui.loader.LayoutLoader;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
class Test2 extends PApplet {
  public static void main(String[] args) {
    PApplet.main("kyui.test.Test2");
  }
  public void settings() {
    size(800, 800);
  }
  public void setup() {
    KyUI.start(this);
    ElementLoader.loadOnStart();
    //LayoutLoader.loadXML(KyUI.get("KyUI:0"), loadXML("test_layout.xml"));
    //    KyUI.<kyui.element.Button>get2("button1").setPressListener((processing.event.MouseEvent e, int index) -> {
    //      System.out.println("clicked! " + KyUI.frameCount);
    //      return false;
    //    });
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
