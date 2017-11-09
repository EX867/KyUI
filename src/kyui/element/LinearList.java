package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.ItemSelectListener;
import kyui.event.listeners.MouseEventListener;
import kyui.event.listeners.OnAdjustListener;
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
  //temp values
  Rect cacheRect=new Rect();
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
    listLayout.padding=strokeWeight;
    slider.margin=0;
    listLayout.setAdjustListener(new OnAdjustListener() {
      @Override
      public void onAdjust() {
        setSlider();
      }
    });
    slider.setAdjustListener(new OnAdjustListener() {
      @Override
      public void onAdjust() {
        setList();
        listLayout.invalidate();
      }
    });
    bgColor=KyUI.Ref.color(127);
    addChild(linkLayout);
  }
  public void addItem(String text) {
    listLayout.addChild(listLayout.children.size(), new SelectableButton(getName() + ":" + count, this));
    count++;
    setSlider();
  }
  public void addItem(int index, String text) {
    listLayout.addChild(index, new SelectableButton(getName() + ":" + count, this));
    count++;
    setSlider();
  }
  public void addItem(SelectableButton e) {
    listLayout.addChild(listLayout.children.size(), e);
    e.Ref=this;
    setSlider();
  }
  public void addItem(int index, SelectableButton e) {
    listLayout.addChild(index, e);
    e.Ref=this;
    setSlider();
  }
  public void removeItem(int index) {
    if (index < 0 || index >= size()) return;
    listLayout.removeChild(listLayout.children.get(index).getName());
    setList();
    setSlider();
  }
  public void setSelectListener(ItemSelectListener l) {
    selectListener=l;
  }
  public void setFixedSize(int size) {//only works on fixed mode.
    listLayout.setFixedSize(size);
    localLayout();
  }
  @Override
  public synchronized void onLayout() {
    linkLayout.ratio=1 - (float)sliderSize / (pos.right - pos.left);
    linkLayout.setPosition(pos);
    setSlider();
  }
  @Override
  public void render(PGraphics g) {
    g.strokeWeight(strokeWeight);
    g.stroke(fgColor);
    g.noFill();
    pos.render(g);
    g.fill(bgColor);
    listLayout.pos.render(g);
    //slider.pos.render(g);
    g.noStroke();
  }
  void setSlider() {//when move slider
    slider.set(listLayout.getTotalSize(), pos.bottom - pos.top, listLayout.offset);
  }
  void setList() {//when move list
    listLayout.setOffset(slider.getOffset(listLayout.getTotalSize()));
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
  @Override
  public int size() {
    return listLayout.size();
  }
}
