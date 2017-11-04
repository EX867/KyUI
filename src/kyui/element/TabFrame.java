package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.EventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.HashMap;
import java.util.LinkedList;
public class TabFrame extends Element {
  protected LinkedList<Button> tabs;
  public LinkedList<Element> contents;//content filled in empty space.
  public HashMap<Integer, Integer> idToIndex;
  //modifiable values
  public int bgColor=0;
  public int tabSize=30;
  public int tabTextEdgeSize=10;
  public int tabContentEdgeSize=10;
  public int tabColor1;//selected
  public int tabColor2;
  public int tabColorSize=8;
  //
  public int selection=0;
  //
  private int count=0;
  private Rect cacheRect=new Rect(0, 0, 0, 0);
  public TabFrame(String name, Rect pos_) {
    super(name);
    pos=pos_;
    tabs=new LinkedList<Button>();
    contents=new LinkedList<Element>();
    idToIndex=new HashMap<Integer, Integer>(100);
    addTab("", new Element(getName() + ":default"));
    tabs.get(0).setEnabled(false);
    localLayout();
    selectTab(0);//0 means no tab selected.
    bgColor=KyUI.Ref.color(127);
    tabColor1=KyUI.Ref.color(0, 0, 255);
    tabColor2=KyUI.Ref.color(0, 0, 127);
  }
  public void addTab(String text, Element content) {
    idToIndex.put(count, tabs.size());
    Button btn=new Button(getName() + ":" + count, pos);
    btn.setPressListener(new TabFramePressListener(count));
    btn.text=text;
    count++;
    tabs.add(btn);
    addChild(btn);
    contents.add(content);
    content.setEnabled(false);
    addChild(content);
  }
  public synchronized void removeTab(int index) {
    if (index < 0 || index >= tabs.size() - 1) return;
    index+=1;
    children.remove(contents.get(index));
    contents.remove(index);
    Button btn=tabs.get(index);
    children.remove(btn);
    tabs.remove(index);
    int id=idToIndex.get(((TabFramePressListener)btn.getPressListener()).id);
    idToIndex.remove(id);
    KyUI.removeElement(btn.getName());
    if (selection == index) selectTab(0);
    else if (selection > index) selectTab(selection - 1);
    for (int a=index; a < tabs.size(); a++) {
      int id2=((TabFramePressListener)tabs.get(a).getPressListener()).id;
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
    if (selection >= 0 && selection < tabs.size()) contents.get(selection).setEnabled(false);
    selection=selection_;
    contents.get(selection).setEnabled(true);
    invalidate();
  }
  @Override
  public void onLayout() {
    int width=0;
    for (Button btn : tabs) {
      if (btn.isEnabled()) {
        int btnWidth=(int)KyUI.Ref.textWidth(btn.text) + tabTextEdgeSize * 2;
        btn.setPosition(new Rect(pos.left + width, pos.top + tabColorSize, pos.left + width + btnWidth, pos.top + tabColorSize + tabSize));
        width+=btnWidth;
      }
    }
    for (Element e : contents) {
      e.setPosition(new Rect(pos.left + tabContentEdgeSize, pos.top + tabColorSize + tabSize + tabContentEdgeSize, pos.right - tabContentEdgeSize, pos.bottom - tabContentEdgeSize));
    }
  }
  @Override
  public void render(PGraphics g) {
    if (bgColor != 0) {
      g.fill(bgColor);
      pos.render(g);
    }
    int width=0;
    int a=0;
    for (Button btn : tabs) {
      if (btn.isEnabled()) {
        int btnWidth=btn.pos.right - btn.pos.left;
        cacheRect.set(pos.left + width, pos.top, pos.left + width + btnWidth, pos.top + tabColorSize);
        if (selection == a) {
          g.fill(tabColor1);
        } else {
          g.fill(tabColor2);
        }
        cacheRect.render(g);
        width+=btnWidth;
      }
      a++;
    }
  }
  public int size() {
    return tabs.size() - 1;
  }
}
