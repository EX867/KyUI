package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.ItemSelectListener;
import kyui.event.listeners.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
public class LinearList extends Element {
  int strokeWeight=4;
  protected DivisionLayout linkLayout;
  protected LinearLayout listLayout;
  protected RangeSlider slider;
  protected ItemSelectListener selectListener;
  protected SelectableButton selection=null;
  int count=0;
  //modifiable values
  public int sliderSize;
  public int fgColor;
  public LinearList(String name) {
    super(name);
    init();
  }
  public LinearList(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    margin=strokeWeight / 2;
    sliderSize=14;
    linkLayout=new DivisionLayout(getName() + ":linkLayout", pos);
    linkLayout.direction=Attributes.HORIZONTAL;
    listLayout=new LinearLayout(getName() + ":listLayout");
    slider=new RangeSlider(getName() + ":slider");
    linkLayout.addChild(listLayout);
    linkLayout.addChild(slider);
    listLayout.setDirection(Attributes.VERTICAL);
    listLayout.setMode(Attributes.FIXED);
    addChild(linkLayout);
  }
  public void addItem(String text) {
    listLayout.addChild(listLayout.children.size(), new SelectableButton(getName() + ":" + count, this));
    count++;
  }
  public void addItem(int index, String text) {
    listLayout.addChild(index, new SelectableButton(getName() + ":" + count, this));
    count++;
  }
  public void addItem(SelectableButton e) {
    listLayout.addChild(listLayout.children.size(), e);
    e.Ref=this;
  }
  public void addItem(int index, SelectableButton e) {
    listLayout.addChild(index, e);
    e.Ref=this;
  }
  public void removeItem(int index) {
    listLayout.removeChild(listLayout.children.get(index).getName());
  }
  public void setSelectListener(ItemSelectListener l) {
    selectListener=l;
  }
  public void setFixedSize(int size) {//only works on fixed mode.
    listLayout.setFixedSize(size);
    localLayout();
  }
  @Override
  public void onLayout() {
    linkLayout.ratio=1 - (float)sliderSize / (pos.right - pos.left);
    linkLayout.setPosition(pos);
  }
  @Override
  public void render(PGraphics g) {
    g.strokeWeight(strokeWeight);
    g.stroke(fgColor);
    super.render(g);
    g.noStroke();
  }
  public static class SelectableButton extends Button {
    //modifiable values
    boolean selected=false;
    LinearList Ref;
    public SelectableButton(String name, LinearList Ref_) {
      super(name);
      Ref=Ref_;
      init();
    }
    public SelectableButton(String name, Rect pos_, LinearList Ref_) {
      super(name, pos_);
      Ref=Ref_;
      init();
    }
    private void init() {
      setPressListener(new ListItemPressListener(this));
    }
    @Override
    public void render(PGraphics g) {
      if (bgColor != 0) {
        setDrawBgColor(g);
        if (selected) {
          g.fill(ColorExt.brighter(bgColor, 40));
        }
        pos.render(g);
      }
      drawContent(g);
    }
    class ListItemPressListener implements MouseEventListener {
      SelectableButton Ref2;
      public ListItemPressListener(SelectableButton Ref2_) {
        Ref2=Ref2_;
      }
      @Override
      public boolean onEvent(MouseEvent e, int index) {
        if (Ref.selection != null) {
          Ref.selection.selected=false;
          Ref.selection.invalidate();
        }
        selected=true;
        Ref.selection=Ref2;
        if (Ref.selectListener != null) {
          Ref.selectListener.onEvent(index);
        }
        invalidate();
        return false;
      }
    }
  }
  @Override
  public Vector2 getPreferredSize() {
    return new Vector2(pos.right - pos.left, listLayout.fixedSize * listLayout.children.size());
  }
  public int size() {
    return listLayout.size();
  }
}
