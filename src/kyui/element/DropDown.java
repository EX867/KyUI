package kyui.element;
import kyui.core.Attributes;
import kyui.core.CachingFrame;
import kyui.core.KyUI;
import kyui.event.ItemSelectListener;
import kyui.event.MouseEventListener;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class DropDown extends Button {
  public static String DOWN="\u25BE";
  public static String UP="\u25B4";
  protected Button downButton;
  protected LinearList picker;
  protected ItemSelectListener selectListener;
  protected CachingFrame downLayer;
  protected Button pickerCancel;
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
    setPressListener(new DropButtonListener());
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
      if (picker.size() == 0) return true;
      downButton.text=UP;
      invalidate();
      KyUI.addLayer(downLayer);
      picker.setPosition(new Rect(pos.left, pos.bottom, pos.right, Math.min(pos.bottom + picker.getPreferredSize().y, KyUI.Ref.height)));//!!!
      return false;
    }
  }
  class DropDownClickListener implements ItemSelectListener {
    public void onEvent(int index) {
      KyUI.removeLayer();
      downButton.text=DOWN;
      selectListener.onEvent(index);//propagate. set text and etc...
    }
  }
}