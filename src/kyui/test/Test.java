package kyui.test;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.element.*;
import kyui.event.listeners.ItemSelectListener;
import kyui.util.Rect;
import kyui.util.Vector2;
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
  int rcount=0;
  int mcount=0;
  TabLayout f;
  @Override
  public void setup() {
    height=300;
    //frameRate(10);
    //KyUI.setRoot(new Background("root", color(255, 255, 255)));
    KyUI.start(this);
    f=new TabLayout("tabs", new Rect(0, 0, width, height));
    f.setRotation(Attributes.ROTATE_LEFT);
    f.setButtonRotation(Attributes.ROTATE_NONE);
    f.setButtonEdgeRotation(Attributes.ROTATE_LEFT);
    f.setTabSize(70);
    KyUI.add(f);
    f.addTab("A", new Element("asdf"));
    f.addTab("A1", new LinearList("adfad"));
    DropDown d=new DropDown("droptest");
    d.text="aggdagga";
    d.setSelectListener(new ItemSelectListener() {
      @Override
      public void onEvent(int index) {
        d.text=index + "";
      }
    });
    Vector2 s=d.getPreferredSize();
    d.setPosition(new Rect(70, 0, 70 + s.x, s.y));
    d.addItem("item");
    d.addItem("item");
    d.addItem("item");
    d.addItem("item");
    d.addItem("item");
    KyUI.get("asdf").addChild(d);
    LinearList e=(LinearList)KyUI.get("adfad");
    for (int a=0; a < 10; a++) {
      e.addItem("" + a);
    }
    // write your other code
    KyUI.changeLayout();
  }
  @Override
  public void draw() {
    //long aa=System.currentTimeMillis();
    KyUI.render(g);
    // write your other code
  }
  int lcount=0;
  @Override
  public void keyTyped() {
    if (key == ' ') {
      f.addTab("Tab" + count, new ToggleButton("Asdf" + count));
      count++;
    } else if (key == '>') {
      LinearList e=(LinearList)KyUI.get("adfad");
      e.addItem("" + lcount++);
    } else if (key == '<') {
      LinearList e=(LinearList)KyUI.get("adfad");
      e.removeItem(e.size() - 1);
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
