package kyui.element;
import com.sun.istack.internal.Nullable;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.MouseEventListener;
import kyui.loader.ElementLoader;
import kyui.loader.LayoutLoader;
import kyui.util.ColorExt;
import kyui.util.HideInEditor;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.data.XML;
import processing.event.MouseEvent;

import java.util.HashMap;
import java.util.List;
//ADD>>capability to set tabLayout's length smaller than its size.
public class TabLayout extends Element {
  protected List<Element> tabs;//only contains TabButton...
  protected List<Element> contents;//content filled in empty space.
  public HashMap<Integer, Integer> idToIndex;
  //pointers
  protected DivisionLayout linkLayout;
  protected LinearLayout tabLayout;
  protected FrameLayout contentLayout;
  @Attribute(layout=Attribute.SELF, setter="setMode")
  public LinearLayout.Behavior mode=LinearLayout.Behavior.DYNAMIC;
  //modifiable values
  @Attribute(type=Attribute.COLOR)
  public int tabColor1;//selected
  @Attribute(type=Attribute.COLOR)
  public int tabColor2;
  //tabs attributes...
  @Attribute
  public int edgeSize=8;
  //protected modifiable values
  @Attribute(setter="setTabSize")
  protected int tabSize;
  @Attribute(setter="setRotation")//these attributes are not synchronized with list properly...but that has no problem. data will be saved by real element's valueI.
  protected Attributes.Rotation rotation=Attributes.Rotation.UP;
  @Attribute(setter="setButtonRotation")
  protected Attributes.Rotation buttonRotation=Attributes.Rotation.UP;
  @Attribute(setter="setButtonEdgeRotation")
  protected Attributes.Rotation buttonEdgeRotation=Attributes.Rotation.UP;
  @Attribute(layout=Attribute.SELF)
  public boolean enableX=false;
  @Attribute(layout=Attribute.SELF)
  public boolean enableAdd=true;
  @Attribute(layout=Attribute.SELF)
  public int tabPadding=10;
  @Attribute(setter="setTextSize")
  protected int textSize=15;
  @Attribute(setter="setTextColor", type=Attribute.COLOR)
  protected int textColor=0xFFFFFFFF;
  @Attribute(setter="setTabBgColor", type=Attribute.COLOR)
  protected int tabBgColor;
  //tabLayout attributes
  @Attribute(setter="setFixedSize", layout=Attribute.SELF)
  protected int fixedSize;
  @Attribute(setter="setIntervalSize", layout=Attribute.SELF)
  protected int intervalSize;
  static int cnt=0;
  public EventListener addTabListener=(Element e) -> {
  };
  //in-class values
  public int selection=0;
  protected TabButton defaultTab;
  //temp vars
  private int count=0;
  private Rect cacheRect=new Rect(0, 0, 0, 0);
  //dummy field(?)
  //  @Attribute(setter="setContentLayoutName", getter="getContentLayoutName", layout=Attribute.SELF)
  //  String contentLayoutName;
  void setContentLayoutName(String name) {
    Element val=KyUI.get(name);
    if (val != null && val instanceof FrameLayout) {
      attachExternalFrame((FrameLayout)val);
    }
  }
  //  String getContentLayoutName() {
  //    return contentLayout.getName();
  //  }
  public TabLayout(String name) {
    super(name);
    init();
  }
  public TabLayout(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    tabSize=40;
    idToIndex=new HashMap<Integer, Integer>(100);
    linkLayout=new DivisionLayout(getName() + ":linkLayout");//, pos);
    tabLayout=new LinearLayout(getName() + ":tabLayout");
    contentLayout=new FrameLayout(getName() + ":contentLayout");
    linkLayout.rotation=Attributes.Rotation.UP;
    linkLayout.addChild(tabLayout);
    linkLayout.addChild(contentLayout);
    addChild(linkLayout);
    tabs=tabLayout.children;
    contents=contentLayout.children;
    tabBgColor=KyUI.Ref.color(50);
    addTab(0, "+", new Element(getName() + "::default"));
    selectTab(0);//0 means no tab selected.
    tabLayout.bgColor=KyUI.Ref.color(127);
    contentLayout.bgColor=KyUI.Ref.color(127);
    tabColor1=KyUI.Ref.color(10, 40, 200);
    tabColor2=KyUI.Ref.color(30, 30, 95);
    KyUI.taskManager.executeAll();
    TabButton t=(TabButton)tabLayout.children.get(0);
    fixedSize=tabLayout.fixedSize;
    defaultTab.setPressListener((MouseEvent e, int index) -> {
      addTabListener.onEvent(TabLayout.this);
      return false;
    });
  }
  public void attachExternalFrame(FrameLayout frame) {//this can used when only frame has same children count with contentLayout (say 0!)
    if (frame.children.size() != contentLayout.children.size()) {
      throw new RuntimeException("[KyUI] attached frameLayout's count is not same with contentLayout's count.");
    }
    if (frame == linkLayout.children.get(1)) {
      contentLayout=frame;
      contentLayout.setEnabled(true);
    } else {
      contentLayout.setEnabled(false);
      contentLayout=frame;
    }
  }
  public void addTab(String text, Element content) {
    addTab(KyUI.INF, text, content);
  }
  public Element addTabFromXml(String text, String path, @Nullable LinearList colorList) {
    XML xml=KyUI.Ref.loadXML(path);
    if (xml == null) {
      return null;
    }
    FrameLayout frame=new FrameLayout(getName() + ":" + path.replaceAll("\\\\", "/").substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")));
    LayoutLoader.loadXML(frame, xml, null, colorList);
    addTab(KyUI.INF, text, frame);
    return frame;
  }
  public void addTab(int index, String text, Element content) {
    index++;
    idToIndex.put(count, index);
    TabButton btn=new TabButton(getName() + ":" + count);
    btn.setPressListener(new TabLayoutPressListener(count));
    btn.text=text;
    btn.edgeColor=tabColor2;
    btn.rotation=buttonRotation;
    btn.edgeRotation=buttonEdgeRotation;
    btn.textSize=textSize;
    btn.xButton.textSize=textSize;
    btn.xButton.textOffsetY=-textSize / 4;
    btn.textColor=textColor;
    btn.bgColor=tabBgColor;
    if (count == 0) {
      defaultTab=btn;
    }
    content.setEnabled(false);
    count++;
    tabLayout.addChild(index, btn);
    contentLayout.addChild(index, content);
    //
    if (selection >= index) selectTab(selection + 1);
    for (int a=index; a < tabs.size(); a++) {
      int id2=((TabLayoutPressListener)((TabButton)tabs.get(a)).getPressListener()).id;
      idToIndex.put(id2, a);
    }
  }
  public void removeTab(int index) {
    index++;
    if (index < 0 || index >= tabs.size()) return;
    contentLayout.removeChild(index);
    idToIndex.remove(idToIndex.get(((TabLayoutPressListener)((TabButton)tabs.get(index)).getPressListener()).id));
    tabLayout.removeChild(index);
    //
    if (selection == index) selectTab(0);
    else if (selection > index) selectTab(selection - 1);
    for (int a=index; a < tabs.size(); a++) {
      idToIndex.put(((TabLayoutPressListener)((TabButton)tabs.get(a)).getPressListener()).id, a);
    }
    localLayout();
  }
  class TabLayoutPressListener implements MouseEventListener {
    int id;
    public TabLayoutPressListener(int id_) {
      id=id_;
    }
    @Override
    public boolean onEvent(MouseEvent e, int index) {
      selectTab(index);
      return true;
    }
  }
  public void selectTab(int selection_) {
    if (selection_ < 0 || selection_ >= tabs.size()) return;
    if (selection >= 0 && selection < tabs.size()) {
      ((TabButton)tabs.get(selection)).edgeColor=tabColor2;
      contents.get(selection).setEnabled(false);
    }
    selection=selection_;
    ((TabButton)tabs.get(selection)).edgeColor=tabColor1;
    contents.get(selection).setEnabled(true);
    invalidate();
  }
  public void setTabNames(String[] texts) {
    for (int a=1; a <= texts.length && a < tabLayout.children.size(); a++) {
      ((TabButton)tabLayout.children.get(a)).text=texts[a - 1];
    }
  }
  public void setMode(LinearLayout.Behavior mode) {
    this.mode=mode;
    tabLayout.setMode(mode);
    localLayout();
  }
  public void setTabSize(int size) {
    tabSize=size;
    localLayout();
  }
  public void setRotation(Attributes.Rotation rotation_) {
    rotation=rotation_;
    if (rotation == Attributes.Rotation.DOWN || rotation == Attributes.Rotation.UP) {
      tabLayout.setDirection(Attributes.Direction.HORIZONTAL);
    } else {
      tabLayout.setDirection(Attributes.Direction.VERTICAL);
    }
    if (rotation == Attributes.Rotation.RIGHT || rotation == Attributes.Rotation.DOWN) {
      linkLayout.inverse=true;
    } else {
      linkLayout.inverse=false;
    }
    linkLayout.rotation=rotation;
    setButtonRotation(rotation);
  }
  public void setButtonRotation(Attributes.Rotation rotation_) {
    buttonRotation=rotation_;
    for (Element e : tabs) {
      ((TabButton)e).rotation=buttonRotation;
    }
    setButtonEdgeRotation(buttonRotation);
  }
  public void setButtonEdgeRotation(Attributes.Rotation rotation_) {
    buttonEdgeRotation=rotation_;
    for (Element e : tabs) {
      ((TabButton)e).edgeRotation=buttonEdgeRotation;
    }
    localLayout();
  }
  public void setTextSize(int v) {
    for (Element e : tabLayout.children) {
      TabButton t=(TabButton)e;
      t.textSize=v;
      t.xButton.textSize=v;
      t.xButton.textOffsetY=-v / 4;
    }
    textSize=v;
  }
  public void setTextColor(int v) {
    for (Element e : tabLayout.children) {
      ((TabButton)e).textColor=v;
    }
    textColor=v;
  }
  public void setFixedSize(int v) {
    tabLayout.setFixedSize(v);
    fixedSize=v;
  }
  public void setTabBgColor(int v) {
    for (Element e : tabLayout.children) {
      e.setBgColor(v);
    }
    tabBgColor=v;
  }
  public void setIntervalSize(int v) {
    tabLayout.intervalSize=v;
    intervalSize=v;
  }
  @Override
  public void onLayout() {
    defaultTab.setEnabled(enableAdd);
    tabLayout.starts=enableAdd ? 0 : 1;
    linkLayout.value=tabSize;
    contentLayout.padding=padding;
    for (Element e : tabLayout.children) {
      e.padding=tabPadding;
    }
    linkLayout.setPosition(pos);
    //contentLayout.onLayout();
  }
  @Override
  public void editorAdd(Element e) {
    if (editorCheck(e)) {
      addTab("Tab", e);
      KyUI.taskManager.executeAll();
      selectTab(tabLayout.children.size() - 1);
    }
  }
  @Override
  public void editorRemove(String name) {
    int index=contents.indexOf(KyUI.get(name));
    removeTab(index - 1);
  }
  @Override
  public boolean editorIsChild(Element e) {
    return contents.contains(e);
  }
  @HideInEditor
  public class TabButton extends Button {
    int edgeColor;
    Attributes.Rotation edgeRotation=Attributes.Rotation.UP;
    Button xButton;
    public TabButton(String name) {
      super(name);
      init();
    }
    public TabButton(String name, Rect pos_) {
      super(name, pos_);
      init();
    }
    void init() {
      xButton=new Button(getName() + ":xButton");
      xButton.text="x";
      xButton.bgColor=ColorExt.brighter(bgColor, -10);
      xButton.textOffsetY=-textSize / 4;
      addChild(xButton);
      xButton.setPressListener(new TabXButtonListener(this));
    }
    public void render(PGraphics g) {
      if (this == defaultTab) {
        textOffsetX=-textSize % 2;
        textOffsetY=-textSize / 6;
        if (bgColor != 0) {
          fill(g, getDrawBgColor(g));
          pos.render(g, -2);
        }
        drawContent(g, textColor);
        return;
      }
      textOffsetX=0;
      textOffsetY=0;
      if (edgeRotation == Attributes.Rotation.UP) {
        cacheRect.set(pos.left, pos.top, pos.right, pos.top + edgeSize);
        textOffsetY=edgeSize / 4;
      } else if (edgeRotation == Attributes.Rotation.RIGHT) {
        cacheRect.set(pos.right - edgeSize, pos.top, pos.right, pos.bottom);
        textOffsetX=-edgeSize / 4;
      } else if (edgeRotation == Attributes.Rotation.DOWN) {
        cacheRect.set(pos.left, pos.bottom - edgeSize, pos.right, pos.bottom);
        textOffsetY=-edgeSize / 4;
      } else if (edgeRotation == Attributes.Rotation.LEFT) {
        cacheRect.set(pos.left, pos.top, pos.left + edgeSize, pos.bottom);
        textOffsetY=edgeSize / 4;
      }
      if (enableX) {
        if (rotation.ordinal() % 2 == 0) {
          textOffsetX-=(pos.bottom - pos.top) / 3;
        } else {
          textOffsetX-=(pos.right - pos.left) / 3;
        }
      }
      super.render(g);
      g.fill(edgeColor);
      cacheRect.render(g);
    }
    @Override
    public void onLayout() {
      if (this == defaultTab) {
        super.onLayout();
        return;
      }
      float size;
      xButton.rotation=rotation;
      if (rotation.ordinal() % 2 == 0) {
        size=(pos.bottom - pos.top);
      } else {
        size=(pos.right - pos.left);
      }
      if ((edgeRotation.ordinal() - rotation.ordinal()) % 2 != 1) {
        size-=edgeSize;
      }
      size/=2;
      if (rotation == Attributes.Rotation.UP) {
        xButton.setPosition(new Rect(pos.right - size * 3 / 2, pos.top + size / 2, pos.right - size / 2, pos.bottom - size / 2));
      } else if (rotation == Attributes.Rotation.RIGHT) {
        xButton.setPosition(new Rect(pos.left + size / 2, pos.bottom - size * 3 / 2, pos.right - size / 2, pos.bottom - size / 2));
      } else if (rotation == Attributes.Rotation.DOWN) {
        xButton.setPosition(new Rect(pos.left + size / 2, pos.top + size / 2, pos.left + size * 3 / 2, pos.bottom - size / 2));
      } else if (rotation == Attributes.Rotation.LEFT) {
        xButton.setPosition(new Rect(pos.left + size / 2, pos.top + size / 2, pos.right - size / 2, pos.top + size * 3 / 2));
      }
      if (edgeRotation == Attributes.Rotation.UP) {
        xButton.setPosition(new Rect(xButton.pos.left + edgeSize / 2, xButton.pos.top + edgeSize / 2, xButton.pos.right + edgeSize / 2, xButton.pos.bottom + edgeSize / 2));
      } else if (edgeRotation == Attributes.Rotation.RIGHT) {
        xButton.setPosition(new Rect(xButton.pos.left - edgeSize, xButton.pos.top, xButton.pos.right - edgeSize, xButton.pos.bottom));
      } else if (edgeRotation == Attributes.Rotation.DOWN) {
        xButton.setPosition(new Rect(xButton.pos.left - edgeSize / 2, xButton.pos.top - edgeSize / 2, xButton.pos.right - edgeSize / 2, xButton.pos.bottom - edgeSize / 2));
      } else if (edgeRotation == Attributes.Rotation.LEFT) {
        xButton.setPosition(new Rect(xButton.pos.left, xButton.pos.top, xButton.pos.right, xButton.pos.bottom));
      }
      xButton.setEnabled(enableX);
      //super.onLayout();
    }
    @Override
    public Vector2 getPreferredSize() {
      if (!enableX || this == defaultTab) return super.getPreferredSize();
      padding=tabPadding;
      if (rotation == Attributes.Rotation.UP || rotation == Attributes.Rotation.DOWN) {
        return super.getPreferredSize().addAssign(new Vector2((pos.bottom - pos.top) * 2 / 3, 0));
      } else {
        return super.getPreferredSize().addAssign(new Vector2(0, (pos.right - pos.left) * 2 / 3));
      }
    }
    class TabXButtonListener implements MouseEventListener {
      TabButton Ref;
      public TabXButtonListener(TabButton t) {
        Ref=t;
      }
      public boolean onEvent(MouseEvent e, int index) {
        if (!ElementLoader.isEditor) {
          removeTab(tabs.indexOf(Ref) - 1);
        }
        return false;
      }
    }
  }
}
