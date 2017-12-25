package kyui.editor;
import kyui.core.Attributes;
import kyui.core.DropMessenger;
import kyui.core.KyUI;
import kyui.core.Element;
import kyui.element.*;
import kyui.event.DropEventListener;
import kyui.event.EventListener;
import kyui.event.MouseEventListener;
import kyui.loader.ElementLoader;
import kyui.loader.LayoutLoader;
import kyui.util.Rect;
import kyui.core.RelativeFrame;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import sojamo.drop.DropEvent;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
public class Main extends PApplet {
  public static Element selection=null;//used in layout_tree
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
    surface.setResizable(true);
    surface.setLocation(0, 0);
    surface.setIcon(loadImage("data/editor.png"));
    surface.setTitle(startText);
    frameRate(30);
    KyUI.start(this);
    DivisionLayout main_statusDivision=new DivisionLayout("main_statusDivision", new Rect(0, 0, width, height));
    main_statusDivision.rotation=Attributes.Rotation.DOWN;
    main_statusDivision.value=40;
    TabLayout main_tabs=new TabLayout("main_tabs");
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
    layout_tree.getRoot().content=layout_frame;
    layout_tree.setSelectListener(new EventListener() {
      @Override
      public void onEvent(Element e_) {
        Element e=(Element)((TreeGraph.Node)e_).content;
        selection=e;
        ElementLoader.AttributeSet attrs=ElementLoader.attributes.get(e.getClass());
        if (attrs != null) {
          layout_inspector.setItems((java.util.List)attrs.items);
          layout_inspector.localLayout();
          attrs.setAttribute(e);
        } else {
          System.out.println("class " + e.getClass().getTypeName() + " returned null.");
        }
      }
    });
    layout_elements.direction=Attributes.Direction.HORIZONTAL;
    layout_elements.setFixedSize(150);
    KyUI.addDragAndDrop(layout_elements, layout_tree, new DropEventListener() {
      private int count=0;//FIX>>to find not duplicated name!
      @Override
      public void onEvent(DropMessenger messenger, MouseEvent end, int endIndex) {
        ElementLoader.ElementImage e=KyUI.<ElementLoader.ElementImage>get2(messenger.message);//e.element.getInstance()..
        TreeGraph.Node<Element> node=layout_tree.getNodeOver(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y);
        if (node != null) {
          String name=e.element.getSimpleName() + count + "_" + ((System.currentTimeMillis() / 1000) % 100000);//FIX>>defult valueI
          TreeGraph.Node n=ElementLoader.addElement(node, name, e.element);
          if (n != null) {
            Element el=(Element)n.content;
            el.setPosition(new Rect(200, 200, 400, 400));//TEST(delete)
            layout_tree.localLayout();
            count++;//FIX>>(2)
          }
        }
      }
    });
    layout_right.addChild(layout_top);
    layout_right.addChild(layout_tree);
    layout_right.addChild(layout_elements);
    layout_right.addChild(layout_inspector);
    layout_right.setDirection(Attributes.Direction.VERTICAL);
    ImageToggleButton layout_frame_move=new ImageToggleButton("layout_frame_move", ElementLoader.loadImageResource("move.png"));
    layout_frame_move.setPressListener((MouseEvent e, int index) -> {
      layout_frame.scroll=!layout_frame_move.value;
      return false;
    });
    ImageButton layout_export=new ImageButton("layout_export", ElementLoader.loadImageResource("export.png"));
    EventListener action_export=(Element e) -> {
      PrintWriter write=createWriter("layout.xml");
      write.write(LayoutLoader.saveXML(layout_tree.getRoot()).format(2));
      write.close();
      System.out.println("[KyUI] exported as layout.xml in " + KyUI.frameCount);
    };
    layout_export.setPressListener((MouseEvent e, int index) -> {
      action_export.onEvent(null);
      return false;
    });
    ImageButton layout_delete=new ImageButton("layout_delete", ElementLoader.loadImageResource("delete.png"));
    layout_delete.setPressListener((MouseEvent e, int index) -> {
      if (layout_tree.selection != null) {
        if (layout_tree.getRoot() != layout_tree.selection) {
          layout_tree.selection.delete();
          layout_tree.invalidate();
        }
      }
      return false;
    });
    layout_top.addChild(layout_frame_move);
    layout_top.addChild(layout_export);
    layout_top.addChild(layout_delete);
    //add all to main_layout
    main_layout.addChild(layout_frame);
    main_layout.addChild(layout_right);
    //set main_colors
    main_colors.rotation=Attributes.Rotation.DOWN;
    main_colors.value=40;
    DivisionLayout colors_down=new DivisionLayout("colors_down");
    colors_down.rotation=Attributes.Rotation.RIGHT;
    colors_down.value=60;
    LinearList colors_list=new LinearList("colors_list");
    Button colors_add=new Button("colors_add");
    colors_add.text="ADD";
    colors_add.margin=3;
    TextBox colors_addVar=new TextBox("colors_addVar");
    colors_addVar.setNumberOnly(TextBox.NumberType.NONE);
    colors_add.setPressListener((MouseEvent e, int index) -> {
      String text=colors_addVar.getText();
      if (!colors_addVar.error && !text.isEmpty() && !ElementLoader.vars.containsKey(text)) {
        ElementLoader.vars.put(text, new InspectorColorVarButton.ColorVariable(text, 0xFF000000));//default value=black.
        ColorButton cb=new ColorButton("variableValue:" + text);
        cb.setPressListener(new ColorButton.OpenColorPickerEvent(cb));
        InspectorButton1 b=new InspectorButton1<String, ColorButton>("variable:" + text, cb);
        b.text=text;
        b.setDataChangeListener((Element el) -> {
          InspectorColorVarButton.ColorVariable var=ElementLoader.vars.get(text);
          var.value=cb.c;
          for (InspectorColorVarButton.ColorVariable.Reference ref : var.references) {
            ref.attr.setField(ref.el, cb.c);
          }
        });
        colors_list.addItem(b);
      }
      return false;
    });
    colors_down.addChild(colors_addVar);
    colors_down.addChild(colors_add);
    main_colors.addChild(colors_list);
    main_colors.addChild(colors_down);
    //set main_shortcuts
    main_shortcut.rotation=Attributes.Rotation.DOWN;
    //add tabs to main_tabs
    main_tabs.addTab("  LAYOUT   ", main_layout);
    main_tabs.addTab("  COLORS   ", main_colors);
    main_tabs.addTab(" SHORTCUT  ", main_shortcut);
    //add tabs and status to main
    main_statusDivision.addChild(main_tabs);
    main_statusDivision.addChild(new StatusBar("main_status"));
    KyUI.add(main_statusDivision);
    KyUI.changeLayout();
    KyUI.addResizeListener((int w, int h) -> {
      main_statusDivision.pos.set(0, 0, w, h);
      main_statusDivision.onLayout();
      main_statusDivision.invalidate();
    });
    KyUI.<StatusBar>get2("main_status").text=startText;
    ElementLoader.loadOnStart(layout_elements, layout_inspector);
    ElementLoader.vars.put("NONE", new InspectorColorVarButton.ColorVariable("NONE", 0));
    main_tabs.selectTab(1);
    KyUI.addShortcut(new KyUI.Shortcut("Export", true, false, false, 19, java.awt.event.KeyEvent.VK_S, action_export, false));
    KyUI.addDragAndDrop(layout_tree, (DropEvent de) -> {
      String filename=de.file().getAbsolutePath().replace("\\", "/");
      LayoutLoader.loadXML(layout_frame, loadXML(filename), layout_tree.getRoot());//make root to node!
      KyUI.changeLayout();
    });
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
