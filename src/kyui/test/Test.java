package kyui.test;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.element.FileSelectorButton;
import kyui.element.LinearList;
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
    surface.setResizable(true);
    //ElementLoader.loadOnStart();
    //    KyUI.add(new TabLayout("asdf", new Rect(0, 0, 800, 800)));
    //    TabLayout tab=KyUI.<TabLayout>get2("asdf");
    //    tab.enableX=true;
    //    tab.tabRemoveListener=(int index)->{
    //      System.out.println(index);
    //    };
    //    for(int a=0;a<20;a++){
    //      tab.addTab(a+"", new Element(a+""));
    //    }
    //Element f=KyUI.<TabLayout>get2("asdf").addTabFromXml("A", "C:/Users/user/Documents/[Projects]/KyUI/layout_partial_layout.xml", null);
    LinearList asdf=new LinearList("asdf", new Rect(0, 0, 800, 800));
    KyUI.add(asdf);
    //    asdf.overlayOnDrag=50;
    //    asdf.enableReordering();
    //    for (int a=0; a < 20; a++) {
    //      asdf.addItem(a + "");
    //    }
    asdf.setFixedSize(30);
    FileSelectorButton.listDirectory(asdf, new java.io.File("C:/Users/user/Documents"), (java.io.File file) -> {
      System.out.println(file.getAbsolutePath());
    });
    KyUI.addResizeListener((int x,int y)->{
      asdf.setPosition(new Rect(0,0,x,y));
    });
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
