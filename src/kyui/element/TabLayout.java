package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.LinkedList;
public class TabLayout extends Element {
  protected LinkedList<Element> tabs;//only contains TabButton...
  public LinkedList<Element> contents;//content filled in empty space.
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
  protected int rotation=Attributes.ROTATE_NONE;
  protected int buttonRotation=Attributes.ROTATE_NONE;
  protected int buttonEdgeRotation=Attributes.ROTATE_NONE;
  //in-class values
  public int selection=0;
  //temp vars
  private int count=0;
  private Rect cacheRect=new Rect(0, 0, 0, 0);
  public TabLayout(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
    contents=new LinkedList<Element>();
    idToIndex=new HashMap<Integer, Integer>(100);
    linkLayout=new DivisionLayout(getName() + ":linkLayout", pos);
    tabLayout=new LinearLayout(getName() + ":tabLayout");
    contentLayout=new FrameLayout(getName() + ":contentLayout");
    linkLayout.direction=Attributes.VERTICAL;
    addChild(linkLayout);
    linkLayout.addChild(tabLayout);
    linkLayout.addChild(contentLayout);
    tabs=tabLayout.children;
    contents=contentLayout.children;
    addTab("", new Element(getName() + ":default"));
    tabs.get(0).setEnabled(false);
    selectTab(0);//0 means no tab selected.
    localLayout();
  }
  private void init() {
    bgColor=KyUI.Ref.color(127);
    tabColor1=KyUI.Ref.color(0, 0, 255);
    tabColor2=KyUI.Ref.color(0, 0, 127);
    tabSize=38;
  }
  public void addTab(String text, Element content) {
    idToIndex.put(count, tabs.size());
    TabButton btn=new TabButton(getName() + ":" + count);
    btn.setPressListener(new TabFramePressListener(count));
    btn.text=text;
    btn.edgeColor=tabColor2;
    btn.rotation=buttonRotation;
    btn.edgeRotation=buttonEdgeRotation;
    count++;
    tabLayout.addChild(btn);
    contentLayout.addChild(content);
    content.setEnabled(false);
  }
  public synchronized void removeTab(int index) {
    if (index < 0 || index >= tabs.size() - 1) return;
    index+=1;
    Element content=contents.get(index);
    contentLayout.removeChild(content.getName());
    Element btn=tabs.get(index);
    tabLayout.removeChild(btn.getName());
    int id=idToIndex.get(((TabFramePressListener)((TabButton)btn).getPressListener()).id);
    idToIndex.remove(id);
    KyUI.removeElement(btn.getName());
    //
    if (selection == index) selectTab(0);
    else if (selection > index) selectTab(selection - 1);
    for (int a=index; a < tabs.size(); a++) {
      int id2=((TabFramePressListener)((TabButton)tabs.get(a)).getPressListener()).id;
      idToIndex.put(id2, a);
    }
    localLayout();
  }
  class TabFramePressListener implements EventListener {
    int id;
    public TabFramePressListener(int id_) {
      id=id_;
    }
    @Override
    public void onEvent() {
      selectTab(idToIndex.get(id));
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
  public void setMode(int mode) {
    tabLayout.setMode(mode);
    localLayout();
  }
  public void setTabSize(int size) {
    tabSize=size;
    localLayout();
  }
  public void setRotation(int rotation_) {
    rotation=rotation_;
    if (rotation == Attributes.ROTATE_DOWN || rotation == Attributes.ROTATE_NONE) {
      tabLayout.setDirection(Attributes.HORIZONTAL);
      linkLayout.direction=Attributes.VERTICAL;
    } else {
      tabLayout.setDirection(Attributes.VERTICAL);
      linkLayout.direction=Attributes.HORIZONTAL;
    }
    if (rotation == Attributes.ROTATE_RIGHT || rotation == Attributes.ROTATE_DOWN) {
      linkLayout.inverse=true;
    } else {
      linkLayout.inverse=false;
    }
    setButtonRotation(rotation);
  }
  public void setButtonRotation(int rotation_) {
    buttonRotation=rotation_;
    for (Element e : tabs) {
      ((TabButton)e).rotation=buttonRotation;
    }
    setButtonEdgeRotation(buttonRotation);
  }
  public void setButtonEdgeRotation(int rotation_) {
    buttonEdgeRotation=rotation_;
    for (Element e : tabs) {
      ((TabButton)e).edgeRotation=buttonEdgeRotation;
    }
    localLayout();
  }
  @Override
  public void onLayout() {
    linkLayout.setPosition(pos);
    if (linkLayout.direction == Attributes.VERTICAL) linkLayout.ratio=(float)tabSize / (pos.bottom - pos.top);
    else linkLayout.ratio=(float)tabSize / (pos.right - pos.left);
    super.onLayout();
  }
  @Override
  public void render(PGraphics g) {
    if (bgColor != 0) {
      g.fill(bgColor);
      pos.render(g);
    }
  }
  public int size() {
    return tabs.size() - 1;
  }
  public class TabButton extends Button {
    int edgeColor;
    int edgeRotation=Attributes.ROTATE_NONE;
    public TabButton(String name) {
      super(name);
    }
    public TabButton(String name, Rect pos_) {
      super(name, pos_);
    }
    public void render(PGraphics g) {
      if (edgeRotation == Attributes.ROTATE_NONE) {
        cacheRect.set(pos.left, pos.top, pos.right, pos.top + edgeSize);
        textOffsetY=edgeSize / 2;
      } else if (edgeRotation == Attributes.ROTATE_RIGHT) {
        cacheRect.set(pos.right - edgeSize, pos.top, pos.right, pos.bottom);
        textOffsetX=edgeSize / 2;
      } else if (edgeRotation == Attributes.ROTATE_DOWN) {
        cacheRect.set(pos.left, pos.bottom - edgeSize, pos.right, pos.bottom);
        textOffsetY=edgeSize / 2;
      } else if (edgeRotation == Attributes.ROTATE_LEFT) {
        cacheRect.set(pos.left, pos.top, pos.left + edgeSize, pos.bottom);
        textOffsetX=edgeSize / 2;
      }
      super.render(g);
      g.fill(edgeColor);
      cacheRect.render(g);
    }
  }
}
