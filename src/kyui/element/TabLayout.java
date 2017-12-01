package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
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
  //modifiable values
  public int tabColor1;//selected
  public int tabColor2;
  public int edgeSize=8;
  //protected modifiable values
  protected int tabSize;
  protected Attributes.Rotation rotation=Attributes.Rotation.UP;
  protected Attributes.Rotation buttonRotation=Attributes.Rotation.UP;
  protected Attributes.Rotation buttonEdgeRotation=Attributes.Rotation.UP;
  protected boolean enableX=true;
  //in-class values
  public int selection=0;
  //temp vars
  private int count=0;
  private Rect cacheRect=new Rect(0, 0, 0, 0);
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
    tabSize=38;
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
    addTab(0, "", new Element(getName() + ":default"));
    selectTab(0);//0 means no tab selected.
    localLayout();
    tabLayout.bgColor=KyUI.Ref.color(127);
    contentLayout.bgColor=KyUI.Ref.color(127);
    tabColor1=KyUI.Ref.color(10, 40, 200);
    tabColor2=KyUI.Ref.color(30, 30, 95);
  }
  public void addTab(String text, Element content) {
    addTab(KyUI.INF, text, content);
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
    if (count == 0) {
      btn.setEnabled(false);
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
  public void setMode(LinearLayout.Behavior mode) {
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
  public void setEnableX(boolean v) {
    enableX=v;
  }
  @Override
  public void onLayout() {
    linkLayout.setPosition(pos);
    linkLayout.value=tabSize;
    super.onLayout();
  }
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
      if (!enableX) return super.getPreferredSize();
      if (rotation == Attributes.Rotation.UP || rotation == Attributes.Rotation.DOWN) {
        return new Vector2(KyUI.Ref.textWidth(text) + padding * 2 + textSize, textSize + padding * 2);
      } else {
        return new Vector2(textSize + padding * 2, KyUI.Ref.textWidth(text) + padding * 2 + textSize);
      }
    }
    class TabXButtonListener implements MouseEventListener {
      TabButton Ref;
      public TabXButtonListener(TabButton t) {
        Ref=t;
      }
      public boolean onEvent(MouseEvent e, int index) {
        removeTab(tabs.indexOf(Ref) - 1);
        return false;
      }
    }
  }
}
