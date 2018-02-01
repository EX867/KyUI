package kyui.test;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.element.TabLayout;
import kyui.loader.ElementLoader;
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
    ElementLoader.loadOnStart();
    KyUI.add(new TabLayout("asdf", new Rect(0, 0, 800, 800)));
    Element f=KyUI.<TabLayout>get2("asdf").addTabFromXml("A", "C:/Users/user/Documents/[Projects]/KyUI/layout_partial_layout.xml", null);
    KyUI.changeLayout();
//    KyUI.getRoot().runRecursively((Element e) -> {
    //      System.out.println(e.getName() + " : " + e.pos+" "+e.children.size());
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
