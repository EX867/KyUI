package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.LinkedList;
public class TabFrame extends Element {
  protected LinkedList<Element> tabs;//only contains TabButton...
  public LinkedList<Element> contents;//content filled in empty space.
  public HashMap<Integer, Integer> idToIndex;
  protected LinearLayout tabLayout;
  //modifiable values
  public int tabColor1;//selected
  public int tabColor2;
  //protected modifiable values
  protected int tabSize;
  protected int rotation=Attributes.ROTATE_NONE;
  //in-class values
  public int selection=0;
  //temp vars
  private int count=0;
  private Rect cacheRect=new Rect(0, 0, 0, 0);
  public TabFrame(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
    contents=new LinkedList<Element>();
    idToIndex=new HashMap<Integer, Integer>(100);
    tabLayout=new LinearLayout(getName() + ":layout", new Rect(pos.left, pos.top, pos.right, pos.top + tabSize));
    addChild(tabLayout);
    tabs=tabLayout.children;
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
    TabButton btn=new TabButton(getName() + ":" + count, pos);
    btn.setPressListener(new TabFramePressListener(count));
    btn.text=text;
    btn.edgeColor=tabColor2;
    if (rotation == Attributes.ROTATE_DOWN) {
      btn.rotation=Attributes.ROTATE_NONE;
    } else {
      btn.rotation=rotation;
    }
    count++;
    tabLayout.addChild(btn);
    contents.add(content);
    content.setEnabled(false);
    addChild(content);
  }
  public synchronized void removeTab(int index) {
    if (index < 0 || index >= tabs.size() - 1) return;
    index+=1;
    removeChild(contents.get(index).getName());
    contents.remove(index);
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
    tabLayout.pos.bottom=pos.top + size;
    localLayout();
  }
  public void setRotation(int rotation_) {
    rotation=rotation_;
    if (rotation == Attributes.ROTATE_DOWN || rotation == Attributes.ROTATE_NONE) {
      for (Element e : tabs) {
        ((TabButton)e).rotation=Attributes.ROTATE_NONE;
      }
      tabLayout.setDirection(Attributes.HORIZONTAL);
    } else {
      for (Element e : tabs) {
        ((TabButton)e).rotation=rotation;
      }
      tabLayout.setDirection(Attributes.VERTICAL);
    }
    localLayout();
  }
  @Override
  public void onLayout() {
    if (rotation == Attributes.ROTATE_NONE) {
      tabLayout.setPosition(new Rect(pos.left, pos.top, pos.right, pos.top + tabSize));
    } else if (rotation == Attributes.ROTATE_RIGHT) {
      tabLayout.setPosition(new Rect(pos.right - tabSize, pos.top, pos.right, pos.bottom));
    } else if (rotation == Attributes.ROTATE_DOWN) {
      tabLayout.setPosition(new Rect(pos.left, pos.bottom - tabSize, pos.right, pos.bottom));
    } else if (rotation == Attributes.ROTATE_LEFT) {
      tabLayout.setPosition(new Rect(pos.left, pos.top, pos.left + tabSize, pos.bottom));
    }
    for (Element e : contents) {
      if (rotation == Attributes.ROTATE_NONE) {
        e.setPosition(new Rect(pos.left + e.margin, pos.top + tabSize + e.margin, pos.right - e.margin, pos.bottom - e.margin));
      } else if (rotation == Attributes.ROTATE_RIGHT) {
        e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin - tabSize, pos.bottom - e.margin));
      } else if (rotation == Attributes.ROTATE_DOWN) {
        e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin, pos.bottom - e.margin - tabSize));
      } else if (rotation == Attributes.ROTATE_LEFT) {
        e.setPosition(new Rect(pos.left + e.margin + tabSize, pos.top + e.margin, pos.right - e.margin, pos.bottom));
      }
    }
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
}
