package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.ItemSelectListener;
import kyui.event.listeners.MouseEventListener;
import kyui.event.listeners.EventListener;
import kyui.task.Task;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class LinearList extends Element {
  int strokeWeight=4;
  protected DivisionLayout linkLayout;
  protected LinearLayout listLayout;
  protected RangeSlider slider;
  protected ItemSelectListener selectListener;
  protected SelectableButton selection=null;
  protected SelectableButton pressItem;
  int count=0;
  //modifiable values
  public int sliderSize;
  public int fgColor;
  public int direction=Attributes.VERTICAL;
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
    listLayout.setAdjustListener(new EventListener() {
      @Override
      public void onEvent(Element e) {
        setSlider();
      }
    });
    slider.setAdjustListener(new EventListener() {
      @Override
      public void onEvent(Element e) {
        setList();
        listLayout.invalidate();
      }
    });
    bgColor=KyUI.Ref.color(127);
    fgColor=50;
    addChild(linkLayout);
  }
  public void addItem(String text) {
    SelectableButton btn=new SelectableButton(getName() + ":" + count, this);
    btn.text=text;
    listLayout.addChild(btn);
    count++;
    afterModify();
  }
  public void addItem(int index, String text) {
    SelectableButton btn=new SelectableButton(getName() + ":" + count, this);
    btn.text=text;
    listLayout.addChild(index, btn);
    count++;
    afterModify();
  }
  public void addItem(SelectableButton e) {
    listLayout.addChild(listLayout.children.size(), e);
    e.Ref=this;
    afterModify();
  }
  public void addItem(int index, SelectableButton e) {
    listLayout.addChild(index, e);
    e.Ref=this;
    afterModify();
  }
  public void removeItem(int index) {
    if (index < 0 || index >= size()) return;
    listLayout.removeChild(index);
    afterModify();
  }
  public void removeItem(String name) {
    listLayout.removeChild(listLayout.children.indexOf(name));
    afterModify();
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
    if (direction == Attributes.VERTICAL) {
      linkLayout.ratio=1 - (float)sliderSize / (pos.right - pos.left);
    } else {
      linkLayout.ratio=1 - (float)sliderSize / (pos.bottom - pos.top);
    }
    linkLayout.direction=direction % 2 + 1;
    listLayout.setDirection(direction);
    slider.direction=direction;
    linkLayout.setPosition(pos);
    setList();
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
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      pressItem=null;
    }
    return true;
  }
  @Override
  public void startDrop(MouseEvent e, int index) {
    if (pressItem != null) {
      KyUI.dropStart(this, e, index, pressItem.getName(), pressItem.text);
    }
  }
  void afterModify() {
    KyUI.taskManager.addTask(new Task() {
      @Override
      public void execute(Object data) {
        setList();
        slider.setOffset(listLayout.getTotalSize(), listLayout.offset);
      }
    }, null);
  }
  void setSlider() {//when move slider
    setSliderLength();
    slider.setOffset(listLayout.getTotalSize(), listLayout.offset);
  }
  void setList() {//when move list
    //list.totalSize is from onLayout.
    setSliderLength();
    listLayout.setOffset(slider.getOffset(listLayout.getTotalSize()));
  }
  void setSliderLength() {
    if (direction == Attributes.VERTICAL) {
      slider.setLength(listLayout.getTotalSize(), pos.bottom - pos.top);
    } else {
      slider.setLength(listLayout.getTotalSize(), pos.right - pos.left);
    }
  }
  public static class SelectableButton extends Button {//parent_max=1;
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
      float height=(pos.bottom - pos.top);
      float overlap=height;
      if (Ref.pos.top > pos.top) {//up overlap
        overlap=(height - Ref.pos.top + pos.top);
      } else if (Ref.pos.bottom < pos.bottom) {//down overlap
        overlap=(height + Ref.pos.bottom - pos.bottom);
      }
      if (bgColor != 0) {
        int c=getDrawBgColor(g);
        if (selected) {//for identification...
          c=(ColorExt.brighter(bgColor, 40));
        } else {
          c=ColorExt.scale(c, overlap / height);
        }
        g.fill(c);
        pos.render(g);
      }
      if (Ref.pos.top > pos.top) {//up overlap
        textOffsetY=(int)(Ref.pos.top - pos.top) / 2;
      } else if (Ref.pos.bottom < pos.bottom) {//down overlap
        textOffsetY=(int)(Ref.pos.bottom - pos.bottom) / 2;
      } else {
        textOffsetY=0;
      }
      if (overlap > textSize) {//optimization needed!
        g.fill(ColorExt.scale(textColor, overlap / height));
        g.textSize(textSize);
        g.pushMatrix();
        g.translate((pos.left + pos.right) / 2 + textOffsetX, (pos.top + pos.bottom) / 2 + textOffsetY);
        for (int a=1; a < rotation; a++) {
          g.rotate(KyUI.Ref.radians(90));
        }
        if (overlap < height && overlap > 0) {
          g.scale(1, (overlap / height));
        }
        g.text(text, 0, 0);
        g.popMatrix();
        //        drawContent(g, ColorExt.scale(textColor, overlap / height));
      }
    }
    @Override
    public boolean mouseEvent(MouseEvent e, int index) {
      if (e.getAction() == MouseEvent.PRESS) {
        Ref.pressItem=this;
      }
      return super.mouseEvent(e, index);
    }
    class ListItemPressListener implements MouseEventListener {//why!!! why you don't make MouseEvent "Press Action" Listener for button...
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
