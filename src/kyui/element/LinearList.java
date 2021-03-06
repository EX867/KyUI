package kyui.element;
import kyui.core.Attributes;
import kyui.core.DropMessenger;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.ExtendedRenderer;
import kyui.event.ItemSelectListener;
import kyui.event.MouseEventListener;
import kyui.util.ColorExt;
import kyui.util.Rect;
import kyui.util.Task;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.List;
import java.util.function.BiPredicate;
public class LinearList extends Element {
  public int strokeWeight=4;
  protected DivisionLayout linkLayout;
  public LinearLayout listLayout;//NOOOO!!!!
  protected RangeSlider slider;
  protected ItemSelectListener selectListener;
  public BiPredicate<Integer, Integer> reorderListener;
  protected SelectableButton selection=null;
  protected SelectableButton pressItem;
  SelectableButton pressItemOld;
  int count=0;
  //modifiable values
  @Attribute(layout=Attribute.SELF)
  public int sliderSize;
  @Attribute(type=Attribute.COLOR)
  public int fgColor;
  @Attribute(layout=Attribute.SELF)
  public Attributes.Direction direction=Attributes.Direction.VERTICAL;
  boolean selfReorder=false;
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
    linkLayout.rotation=Attributes.Rotation.RIGHT;
    listLayout=new LinearLayout(getName() + ":listLayout");
    slider=new RangeSlider(getName() + ":slider");
    linkLayout.addChild(listLayout);
    linkLayout.addChild(slider);
    listLayout.setDirection(Attributes.Direction.VERTICAL);
    listLayout.setMode(LinearLayout.Behavior.FIXED);
    listLayout.padding=strokeWeight;
    listLayout.setBgColor(0);
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
        LinearList.this.invalidate();
      }
    });
    bgColor=KyUI.Ref.color(127);
    fgColor=50;
    dropOverlayRenderer=new ExtendedRenderer() {
      @Override
      public void render(PGraphics g) {
        int index=getReorderIndex(KyUI.mouseGlobal.getLast());
        g.noFill();
        g.strokeWeight(strokeWeight);
        g.stroke(0);
        if (direction == Attributes.Direction.HORIZONTAL) {
          float lineY=pos.top + listLayout.padding - listLayout.offset + index * (listLayout.fixedSize + listLayout.padding);
          if (lineY > pos.top && lineY < pos.bottom) {
            g.rect(pos.left + 20, lineY - 5, pos.right - 20, lineY + 5);
          }
        } else if (direction == Attributes.Direction.VERTICAL) {
          float lineY=pos.top + listLayout.padding - listLayout.offset + index * (listLayout.fixedSize + listLayout.padding);
          if (lineY > pos.top && lineY < pos.bottom) {
            g.rect(pos.left + 20, lineY - 5, pos.right - 20, lineY + 5);
          }
        }
        g.noStroke();
      }
    };
    addChild(linkLayout);
  }
  public void enableReordering() {
    KyUI.addDragAndDrop(this, this, (DropMessenger messenger, MouseEvent end, int endIndex) -> {
      final int a=messenger.startIndex;
      KyUI.checkOverlayCondition((Element e, Vector2 pos) -> {
        if (isListItem(e)) {
          int b=getReorderIndex(pos);
          if (reorderListener == null || reorderListener.test(a, b)) {
            listLayout.reorderChild(a, b);//move a to b
            onLayout();
          }
          return true;
        }
        return false;
      });
    });
    selfReorder=true;
    listLayout.draggable=false;
  }
  public int getReorderIndex(Vector2 mouse) {
    int index=0;
    if (direction == Attributes.Direction.VERTICAL) {
      index=(int)((mouse.y + (listLayout.fixedSize + listLayout.padding) / 2 - (pos.top + listLayout.padding - listLayout.offset))) / (listLayout.fixedSize + listLayout.padding);
    } else if (direction == Attributes.Direction.HORIZONTAL) {
      index=(int)((mouse.x + (listLayout.fixedSize + listLayout.padding) / 2 - (pos.left + listLayout.padding - listLayout.offset))) / (listLayout.fixedSize + listLayout.padding);
    }
    return Math.max(Math.min(index, listLayout.children.size() - 1), 0);
  }
  public SelectableButton getSelection(){
    return selection;
  }
  public boolean isListItem(Element e) {
    return e == linkLayout || (e instanceof SelectableButton && ((SelectableButton)e).Ref == LinearList.this);
  }
  public void addItem(String text) {
    SelectableButton btn=new SelectableButton(getName() + ":" + count);
    btn.text=text;
    listLayout.addChild(btn);
    count++;
    afterModify();
  }
  public void addItem(int index, String text) {
    SelectableButton btn=new SelectableButton(getName() + ":" + count);
    btn.text=text;
    listLayout.addChild(index, btn);
    count++;
    afterModify();
  }
  public void addItem(SelectableButton e) {
    listLayout.addChild(e);
    e.Ref=this;
    afterModify();
  }
  public void addItem(int index, SelectableButton e) {
    listLayout.addChild(index, e);
    e.Ref=this;
    afterModify();
  }
  @Override
  public void editorAdd(Element e) {
    if (editorCheck(e)) {
      addItem((SelectableButton)e);
    }
  }
  @Override
  public void editorRemove(String name) {
    removeItem(name);
  }
  @Override
  public boolean editorCheck(Element e) {
    return e instanceof SelectableButton;
  }
  @Override
  public boolean editorIsChild(Element e) {
    return listLayout.children.contains(e);
  }
  public void removeItem(int index) {
    if (index < 0 || index >= size()) return;
    listLayout.removeChild(index);
    afterModify();
  }
  public void removeItem(String name) {
    listLayout.removeChild(listLayout.children.indexOf(KyUI.get(name)));
    afterModify();
  }
  public List<Element> getItems() {
    return listLayout.children;
  }
  public void setItems(List<SelectableButton> items) {
    listLayout.children=(List)items;
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
    if (direction == Attributes.Direction.VERTICAL) {
      linkLayout.value=sliderSize;
      linkLayout.rotation=Attributes.Rotation.RIGHT;
    } else {
      linkLayout.value=sliderSize;
      linkLayout.rotation=Attributes.Rotation.DOWN;
    }
    listLayout.setDirection(direction);
    slider.direction=direction;
    linkLayout.setPosition(pos);
    setList();
    setSliderLength();
  }
  @Override
  public void render(PGraphics g) {
    g.strokeWeight(strokeWeight);
    g.stroke(fgColor);
    fill(g, bgColor);
    listLayout.pos.render(g);
    slider.pos.render(g);
    g.noFill();
    pos.render(g);
    g.noStroke();
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      if (pressItem != null) {
        pressItemOld=pressItem;
      }
      pressItem=null;
    } else if (e.getAction() == MouseEvent.WHEEL) {
      invalidate();
    }
    return true;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.RELEASE) {
      if (pressItemOld != null) {
        pressItemOld.selected=false;
      }
    }
    return true;
  }
  @Override
  public void startDrop(MouseEvent e, int index) {
    if (pressItem != null && !selfReorder) {//selfreordering makes dropstart
      KyUI.dropStart(this, e, getItems().indexOf(pressItem), pressItem.getName(), pressItem.text);
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
  void setList() {
    //when move list
    //list.totalSize is from onLayout.
    setSliderLength();
    listLayout.setOffset(slider.getOffset(listLayout.getTotalSize()));
    listLayout.onLayout();
  }
  void setSliderLength() {
    if (direction == Attributes.Direction.VERTICAL) {
      slider.setLength(listLayout.getTotalSize(), pos.bottom - pos.top);
    } else {
      slider.setLength(listLayout.getTotalSize(), pos.right - pos.left);
    }
  }
  public static class SelectableButton extends Button {//parent_max=1;
    //modifiable values
    protected boolean selected=false;
    protected LinearList Ref;
    public SelectableButton(String name) {
      super(name);
      init();
    }
    @Override
    public void addedTo(Element e) {
      if (e instanceof LinearLayout) {
        if (e.parents.size() == 0) {
          System.out.println("first parent error");//FIX>>
          return;
        }
        if (e.parents.get(0).parents.size() == 0) {
          System.out.println("second parent error");//FIX>>
          return;
        }
        Ref=(LinearList)(e.parents.get(0).parents.get(0));//this works because  LinearList->linkLayout(DivisionLayout)->listLayout(LinearLayout).
        //if user added SelectableButton to other Element directly, there will be error.
      } else {
        //throw new RuntimeException("[KyUI] LinearList : tried to add LinearList.SelectableButton to " + e.getClass().getTypeName());
      }
    }
    @Override
    public boolean editorCheckTo(Element e) {
      return true;
      //return e instanceof LinearList || e instanceof DropDown;
    }
    private void init() {
      setPressListener(new ListItemPressListener(this));
    }
    @Override
    public void render(PGraphics g) {
      float height=(pos.bottom - pos.top);
      float overlap=height;
      if (Ref != null) {
        if (Ref.pos.top > pos.top) {//up overlap
          overlap=(height - Ref.pos.top + pos.top);
          textOffsetY=(int)(Ref.pos.top - pos.top) / 2;
        } else if (Ref.pos.bottom < pos.bottom) {//down overlap
          overlap=(height + Ref.pos.bottom - pos.bottom);
          textOffsetY=(int)(Ref.pos.bottom - pos.bottom) / 2;
        } else {
          textOffsetY=0;
        }
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
      drawContent(g, overlap);
    }
    protected void drawContent(PGraphics g, float overlap) {
      float height=(pos.bottom - pos.top);
      if (overlap > textSize && overlap > 0) {//this can not be correct.
        g.fill(ColorExt.scale(textColor, overlap / height));
        g.textFont(textFont);
        g.textSize(Math.max(1, textSize));
        g.pushMatrix();
        g.translate((pos.left + pos.right) / 2 + textOffsetX, (pos.top + pos.bottom) / 2 + textOffsetY);
        for (int a=1; a <= rotation.ordinal(); a++) {
          g.rotate(KyUI.Ref.radians(90));
        }
        if (overlap < height && overlap > 0) {
          g.scale(1, (overlap / height));
        }
        g.text(text, 0, 0);
        g.textFont(KyUI.fontMain);
        g.popMatrix();
      }
    }
    @Override
    public boolean mouseEvent(MouseEvent e, int index) {
      if (Ref != null) {
        if (e.getAction() == MouseEvent.PRESS) {
          Ref.pressItem=this;
        } else if (e.getAction() == MouseEvent.DRAG) {
          if (Ref.selfReorder && KyUI.dropMessenger == null && !pos.contains(KyUI.mouseGlobal.getLast().x, KyUI.mouseGlobal.getLast().y) && pressedL && Ref.droppableStart) {
            KyUI.dropStart(Ref, e, Ref.getItems().indexOf(this), getName(), text);
          }
        }
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
        if (Ref != null) {
          selected=true;
          if (Ref.selection != null) {
            Ref.selection.selected=false;
            Ref.selection.invalidate();
          }
          Ref.selection=Ref2;
          if (Ref.selectListener != null) {
            Ref.selectListener.onEvent(index);
          }
        }
        invalidate();
        return false;
      }
    }
  }
  @Override
  public Vector2 getPreferredSize() {
    return new Vector2(pos.right - pos.left, listLayout.fixedSize * listLayout.children.size() + (listLayout.children.size() + 1) * padding + strokeWeight * 2);
  }
  @Override
  public int size() {
    return listLayout.size();
  }
}
