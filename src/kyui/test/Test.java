package kyui.test;
import kyui.core.*;
import kyui.element.*;
import kyui.event.DropEventListener;
import kyui.event.EventListener;
import kyui.event.ItemSelectListener;
import kyui.loader.ShortcutLoader;
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
    size(/*300*/700, 700 /*300*/);
  }
  int count=0;
  int rcount=0;
  int mcount=0;
  TabLayout f;
  @Override
  public void setup() {
    //frameRate(10);
    //KyUI.setRoot(new Background("root", color(255, 255, 255)));
    KyUI.start(this);
    f=new TabLayout("tabs", new Rect(0, 0, width, height));
    //f.setRotation(Attributes.Rotation.LEFT);
    //f.setButtonRotation(Attributes.Rotation.UP);
    //f.setButtonEdgeRotation(Attributes.Rotation.LEFT);
    //f.setTabSize(70);
    KyUI.add(f);
    f.addTab(" A ", new Element("asdf"));
    f.addTab(" A1 ", new DivisionLayout("division"));
    f.addTab(" A2 ", new Element("asdf2"));
    f.addTab(" A3 ", new TextEdit("asdf3"));
    f.addTab(" A5 ", new TreeGraph("asdf5", "Test"));
    f.addTab(" A6 ", new ImageDrop("asdf6"));
    f.addTab(" A7 ", new RelativeFrame("asdf7"));
    f.addTab(" A8 ", new ColorPickerFull("asdf8"));
    Button bb=new Button("asdfasdfb", new Rect(100, 100, 200, 300));
    KyUI.get("asdf7").addChild(bb);
    //
    //
    TreeGraph t=KyUI.<TreeGraph>get2("asdf5");
    t.addNode("1").addNode("2");
    t.get(0).addNode("3").addNode("4").addNode("5");
    t.get(0).get(1).addNode("6");
    t.get(0).get(1).addNode("7");
    //
    KyUI.get("asdf2").addChild(new IntSlider("slider"));
    KyUI.get("slider").setPosition(new Rect(10, 40, 290, 80));
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
    KyUI.get("division").addChild(new LinearList("list"));
    KyUI.get("division").addChild(new LinearList("list2"));
    KyUI.<DivisionLayout>get2("division").value=0.5F;
    KyUI.<DivisionLayout>get2("division").rotation=Attributes.Rotation.UP;
    KyUI.<DivisionLayout>get2("division").mode=DivisionLayout.Behavior.PROPORTIONAL;
    LinearList e=(LinearList)KyUI.get("list");
    e.direction=Attributes.Direction.HORIZONTAL;
    for (int a=0; a < 10; a++) {
      e.addItem("e" + a);
    }
    LinearList f=(LinearList)KyUI.get("list2");
    for (int a=0; a < 10; a++) {
      LinearList.InspectorColorButton b=new LinearList.InspectorColorButton("f" + a, f);
      b.text="f" + a;
      f.addItem(b);
    }
    KyUI.addDragAndDrop(e, f, new DropEventListener() {
      @Override
      public void onEvent(DropMessenger messenger, MouseEvent end, int endIndex) {
        System.out.println("dropped " + e.getName() + " to " + f.getName() + " with " + messenger.message);
      }
    });
    ShortcutLoader.loadXml(new java.io.File("test_shortcut.xml").getAbsolutePath(), false);
    for (int a=0; a <= 8; a++) {
      int b=a;
      ShortcutLoader.attachTo(a + "", new EventListener() {
        @Override
        public void onEvent(Element e) {
          KyUI.<TabLayout>get2("tabs").selectTab(new Integer(b + 1));
        }
      });
    }
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
    if (key == '>') {
      RelativeFrame r=KyUI.<RelativeFrame>get2("asdf7");
      r.setOffset(r.offsetX + 1, r.offsetY + 1);
    } else if (key == '<') {
      RelativeFrame r=KyUI.<RelativeFrame>get2("asdf7");
      r.setOffset(r.offsetX - 1, r.offsetY - 1);
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
