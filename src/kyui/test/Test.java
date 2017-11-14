package kyui.test;
import kyui.core.*;
import kyui.element.*;
import kyui.event.listeners.DropEventListener;
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
    //f.setRotation(Attributes.ROTATE_LEFT);
    //f.setButtonRotation(Attributes.ROTATE_NONE);
    //f.setButtonEdgeRotation(Attributes.ROTATE_LEFT);
    //f.setTabSize(70);
    KyUI.add(f);
    f.addTab("A", new Element("asdf"));
    f.addTab("A1", new DivisionLayout("division"));
    f.addTab("A2", new Element("asdf2"));
    KyUI.get("asdf2").addChild(new IntSlider("slider"));
    KyUI.get("slider").setPosition(new Rect(10, 40, 290, 70));
    DropDown d=new DropDown("droptest");
    d.text="aggdagga";
    d.setSelectListener(new ItemSelectListener() {
      @Override
      public void onEvent(int index) {
        d.text=index + "";
      }
    });
    Vector2 s=d.getPreferredSize();
    d.setPosition(new Rect(0, 40, 0 + s.x, 40 + s.y));
    for (int a=0; a < 30; a++) {
      d.addItem("item");
    }
    KyUI.get("asdf").addChild(d);
    ((DivisionLayout)KyUI.get("division")).direction=Attributes.VERTICAL;
    KyUI.get("division").addChild(new LinearList("list"));
    KyUI.get("division").addChild(new LinearList("list2"));
    LinearList e=(LinearList)KyUI.get("list");
    e.direction=Attributes.HORIZONTAL;
    for (int a=0; a < 10; a++) {
      e.addItem("e" + a);
    }
    LinearList f=(LinearList)KyUI.get("list2");
    for (int a=0; a < 10; a++) {
      f.addItem("f" + a);
    }
    KyUI.addDragAndDrop(e, f, new DropEventListener() {
      @Override
      public void onEvent(DropMessenger messenger, MouseEvent end, int endIndex) {
        System.out.println("dropped " + e.getName() + " to " + f.getName() + " with " + messenger.message);
      }
    });
    //    KyUI.addDragAndDrop(f, e, new DropEventListener() {
    //      @Override
    //      public void onEvent(DropMessenger messenger, MouseEvent end, int endIndex) {
    //        System.out.println("dropped " + f.getName() + " to " + e.getName() + " with " + messenger.message);
    //      }
    //    });
    // write your other code
    KyUI.changeLayout();
  }
  @Override
  public void draw() {
    KyUI.render(g);
    // write your other code
    strokeWeight(1);
    noFill();
    ellipse(mouseX, mouseY, 20, 20);
    fill(0);
    line(0, mouseY, width, mouseY);
    line(mouseX, 0, mouseX, height);
    strokeWeight(5);
    line(mouseX, mouseY, pmouseX, pmouseY);
    text(frameRate, mouseX + 10, mouseY + 12);
  }
  int lcount=0;
  @Override
  public void keyTyped() {
    if (key == ' ') {
      f.addTab("Tab" + count, new ToggleButton("Asdf" + count));
      f.localLayout();
      count++;
    } else if (key == '>') {
      LinearList e=(LinearList)KyUI.get("list");
      e.addItem("" + lcount++);
    } else if (key == '<') {
      LinearList e=(LinearList)KyUI.get("list");
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
