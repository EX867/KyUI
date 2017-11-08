package kyui.element;
import kyui.core.Element;
import kyui.event.listeners.ItemSelectListener;
import kyui.util.Rect;

import java.util.ArrayList;
public class LinearList extends Element {
  protected DivisionLayout linkLayout;
  protected LinearLayout listLayout;
  protected Slider slider;
  protected ItemSelectListener selectListener;
  //modifiable values
  public int sliderSize;
  public LinearList(String name) {
    super(name);
    init();
  }
  public LinearList(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  public void addItem(int index, Element item) {
    listLayout.addChild(index, item);
  }
  public void removeItem(int index) {
    listLayout.removeChild(listLayout.children.get(index).getName());
  }
  public void setSelectListener(ItemSelectListener l){
    selectListener=l;
  }
  private void init() {
    sliderSize=14;
    linkLayout=new DivisionLayout(getName() + ":linkLayout", pos);
    listLayout=new LinearLayout(getName() + ":listLayout");
    slider=new Slider(getName() + ":slider");
    linkLayout.addChild(listLayout);
    linkLayout.addChild(slider);
  }
}
