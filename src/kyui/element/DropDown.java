package kyui.element;
import kyui.core.Attributes;
import kyui.core.CachingFrame;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import kyui.event.ItemSelectListener;
import kyui.event.MouseEventListener;
import kyui.util.DataTransferable;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class DropDown extends Button implements DataTransferable<Integer> {
  EventListener dataChangeListener;
  public static String DOWN="\u25BE";
  public static String UP="\u25B4";
  protected Button downButton;
  protected LinearList picker;
  protected ItemSelectListener selectListener;
  protected CachingFrame downLayer;
  protected Button pickerCancel;
  int selectedIndex=0;
  MouseEventListener pressListener2;
  //
  public DropDown(String name) {
    super(name);
    init();
  }
  public DropDown(String name, Rect pos_) {
    super(name, pos_);
    init();
  }
  private void init() {
    picker=new LinearList(getName() + ":picker");
    downButton=new Button(getName() + ":downButton");
    pickerCancel=new Button(getName() + ":pickerCancel");
    picker.setFixedSize(40);
    downButton.bgColor=0;
    downButton.text=DOWN;
    super.setPressListener(new DropButtonListener());
    picker.setSelectListener(new DropDownClickListener());
    addChild(downButton);
    downLayer=KyUI.getNewLayer();
    downLayer.addChild(pickerCancel);
    pickerCancel.addChild(picker);
    pickerCancel.text="";
    pickerCancel.setPosition(new Rect(0, 0, KyUI.Ref.width, KyUI.Ref.height));
    pickerCancel.setPressListener(new MouseEventListener() {
      @Override
      public boolean onEvent(MouseEvent e, int index) {
        KyUI.removeLayer();
        downButton.text=DOWN;
        return false;
      }
    });
    pickerCancel.bgColor=0;
  }
  @Override
  public void setPressListener(MouseEventListener el) {
    pressListener2=el;
  }
  public void setSelectListener(ItemSelectListener l) {
    selectListener=l;
  }
  //picker functions.
  public LinearList getPicker() {
    return picker;
  }
  public void addItem(LinearList.SelectableButton e) {
    picker.addItem(e);
  }
  public void addItem(int index, LinearList.SelectableButton e) {
    picker.addItem(index, e);
  }
  public void addItem(String e) {
    picker.addItem(e);
  }
  public void addItem(int index, String e) {
    picker.addItem(index, e);
  }
  public void setFixedSize(int size) {
    picker.setFixedSize(size);
  }
  public void removeItem(int index) {
    picker.removeItem(index);
  }
  public void removeItem(String name) {
    picker.removeItem(name);
  }
  @Override
  public void editorAdd(Element e) {
    if (editorCheck(e)) {
      addItem((LinearList.SelectableButton)e);
    }
  }
  @Override
  public void editorRemove(String name) {
    removeItem(name);
  }
  @Override
  public boolean editorCheck(Element e) {
    return e instanceof LinearList.SelectableButton;
  }
  @Override
  public boolean editorIsChild(Element e) {
    return picker.editorIsChild(e);
  }
  @Override
  public void onLayout() {
    float size;
    downButton.rotation=rotation;
    if (rotation.ordinal() % 2 == 0) {
      size=(pos.bottom - pos.top);
    } else {
      size=(pos.right - pos.left);
    }
    float x=0, y=0;
    if (rotation == Attributes.Rotation.UP) {
      x=pos.right - size / 2;
      y=(pos.top + pos.bottom) / 2;
    } else if (rotation == Attributes.Rotation.RIGHT) {
      x=(pos.left + pos.right) / 2;
      y=pos.bottom - size / 2;
    } else if (rotation == Attributes.Rotation.DOWN) {
      x=pos.left + size / 2;
      y=(pos.top + pos.bottom) / 2;
    } else if (rotation == Attributes.Rotation.LEFT) {
      x=(pos.left + pos.right) / 2;
      y=pos.top + size / 2;
    }
    downButton.setPosition(new Rect(x, y, x, y));
  }
  @Override
  public void render(PGraphics g) {
    textOffsetX=0;
    downButton.textOffsetY=-downButton.textSize / 4;
    if (rotation.ordinal() % 2 == 0) {
      textOffsetX-=(pos.bottom - pos.top) / 3;
    } else {
      textOffsetX-=(pos.right - pos.left) / 3;
    }
    super.render(g);
  }
  @Override
  public Vector2 getPreferredSize() {
    if (rotation == Attributes.Rotation.UP || rotation == Attributes.Rotation.DOWN) {
      return new Vector2(KyUI.Ref.textWidth(text) + padding * 4 + textSize, textSize + padding * 2);
    } else {
      return new Vector2(textSize + padding * 2, KyUI.Ref.textWidth(text) + padding * 4 + textSize);
    }
  }
  class DropButtonListener implements MouseEventListener {
    public boolean onEvent(MouseEvent e, int index) {
      if (pressListener2 != null) {
        pressListener2.onEvent(e, index);
      }
      if (picker.size() == 0) return true;
      downButton.text=UP;
      picker.setFixedSize((int)(pos.bottom - pos.top));
      picker.onLayout();
      Rect screen=transformsAcc.getLast().trans(kyui.util.Transform.identity, new Rect(0, 0, KyUI.Ref.width, KyUI.Ref.height));
      Rect rect=new Rect(pos.left, pos.bottom, pos.right, pos.bottom + picker.getPreferredSize().y);
      if (screen.bottom - pos.bottom < pos.top - screen.top) {
        rect.top=pos.top - picker.getPreferredSize().y;
        rect.bottom=pos.top;
      }
      picker.setPosition(Rect.getIntersection(rect, screen, new Rect()));
      downLayer.setTransform(transformsAcc.getLast());
      KyUI.addLayer(downLayer);
      return false;
    }
  }
  class DropDownClickListener implements ItemSelectListener {
    public void onEvent(int index) {
      KyUI.removeLayer();
      downButton.text=DOWN;
      selectedIndex=index;
      text=((Button)picker.getItems().get(selectedIndex)).text;
      if (selectListener != null) {
        selectListener.onEvent(index);//propagate. set text and etc...
      }
      if (dataChangeListener != null) {
        dataChangeListener.onEvent(DropDown.this);//propagate. set text and etc...
      }
    }
  }
  @Override
  public Integer get() {
    return selectedIndex;
  }
  @Override
  public void set(Integer value) {
    if (value < 0 || value >= picker.getItems().size()) return;
    selectedIndex=value;
    text=((Button)picker.getItems().get(selectedIndex)).text;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    dataChangeListener=event;
  }
}