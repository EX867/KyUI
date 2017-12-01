package kyui.editor;
import kyui.core.Attributes;
import kyui.core.KyUI;
import kyui.core.Element;
import kyui.element.*;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class Main extends PApplet {
  public static void main(String[] args) {
    PApplet.main("kyui.editor.Main");
  }
  @Override
  public void settings() {
    size(1920, 1000);
  }
  final String startText="KyUI Layout Editor 0.1";
  @Override
  public void setup() {
    surface.setLocation(0, 0);
    surface.setIcon(loadImage("data/editor.png"));
    surface.setTitle(startText);
    frameRate(30);
    KyUI.start(this);
    DivisionLayout main_statusDivision=new DivisionLayout("main_statusDivision", new Rect(0, 0, width, height));
    main_statusDivision.rotation=Attributes.Rotation.DOWN;
    main_statusDivision.value=40;
    TabLayout main_tabs=new TabLayout("main_tabs");
    main_tabs.setEnableX(false);
    main_tabs.setRotation(Attributes.Rotation.LEFT);
    main_tabs.setTabSize(40);
    //create tabs
    DivisionLayout main_layout=new DivisionLayout("main_layout");
    DivisionLayout main_colors=new DivisionLayout("main_colors");
    DivisionLayout main_shortcut=new DivisionLayout("main_shortcuts");
    //set main_layout
    main_layout.rotation=Attributes.Rotation.RIGHT;
    main_layout.value=400;
    RelativeFrame layout_frame=new RelativeFrame("layout_frame");
    LinearLayout layout_right=new LinearLayout("layout_right");
    layout_right.setMode(LinearLayout.Behavior.LEAVE);
    layout_right.bgColor=color(50);
    layout_right.padding=3;
    //set layout_right
    LinearLayout layout_top=new LinearLayout("layout_top", new Rect(0, 0, 400, 40));
    TreeGraph<Element> layout_tree=new TreeGraph<Element>("layout_Tree", new Rect(0, 0, 400, 400), "[root]");
    LinearList layout_elements=new LinearList("layout_elements", new Rect(0, 0, 400, 150));//will add images to this
    LinearList layout_inspector=new LinearList("layout_inspector", new Rect(0, 0, 400, 350));
    layout_top.bgColor=color(127);
    layout_top.setMode(LinearLayout.Behavior.FIXED);
    layout_top.setFixedSize(34);
    layout_top.padding=3;
    layout_elements.direction=Attributes.Direction.HORIZONTAL;
    layout_right.addChild(layout_top);
    layout_right.addChild(layout_tree);
    layout_right.addChild(layout_elements);
    layout_right.addChild(layout_inspector);
    layout_right.setDirection(Attributes.Direction.VERTICAL);
    ImageToggleButton layout_frame_move=new ImageToggleButton("layout_frame_move", loadImage("data/move.png"));
    layout_top.addChild(layout_frame_move);
    //add all to main_layout
    main_layout.addChild(layout_frame);
    layout_frame.addChild(new Button("Adfaf", new Rect(0, 0, 400, 400)));//test
    main_layout.addChild(layout_right);
    //set main_colors
    main_colors.rotation=Attributes.Rotation.DOWN;
    //set main_shortcuts
    main_shortcut.rotation=Attributes.Rotation.DOWN;
    //add tabs to main_tabs
    main_tabs.addTab("  LAYOUT   ", main_layout);
    main_tabs.addTab("  COLORS   ", main_colors);
    main_tabs.addTab(" SHORTCUT  ", main_shortcut);
    //add tabs and status to main
    main_statusDivision.addChild(main_tabs);
    main_statusDivision.addChild(new StatusBar("main_status"));
    //    KyUI.get("division").addChild(new LinearList("list"));
    //    KyUI.get("division").addChild(new LinearList("list2"));
    //    KyUI.<DivisionLayout>get2("division").value=0.5F;
    //    KyUI.<DivisionLayout>get2("division").rotation=Attributes.Rotation.UP;
    //    KyUI.<DivisionLayout>get2("division").mode=DivisionLayout.Behavior.PROPORTIONAL;
    //    LinearList e=(LinearList)KyUI.get("list");
    //    e.direction=Attributes.Direction.HORIZONTAL;
    KyUI.add(main_statusDivision);
    KyUI.changeLayout();
    KyUI.<StatusBar>get2("main_status").text=startText;
    main_tabs.selectTab(1);
  }
  @Override
  public void draw() {
    KyUI.render(g);
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
