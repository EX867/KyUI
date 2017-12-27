package kyui.element;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.Rect;
public class IntSlider extends Slider {
  EventListener dataChangeListener;
  @Attribute(setter="setMax")
  public int maxI;
  @Attribute(setter="setMin")
  public int minI;
  @Attribute(setter="set")
  public int valueI;
  public IntSlider(String name) {
    super(name);
    init();
  }
  public IntSlider(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    maxI=10;
    minI=0;
    valueI=10;
  }
  @Override
  public void set(float min_, float max_, float value_) {
    super.set((int)min_, (int)max_, (int)value_);
    set((int)min_, (int)max_, (int)value_);
  }
  @Override
  public void set(float min_, float max_) {
    super.set((int)min_, (int)max_);
    set((int)min_, (int)max_);
  }
  @Override
  public void set(float value_) {
    super.set((int)value_);
    set((int)value_);
  }
  public void set(int min_, int max_, int value_) {
    minI=min_;
    maxI=max_;
    valueI=value_;
    super.set(min_, max_, value_);
  }
  public void set(int min_, int max_) {
    minI=min_;
    maxI=max_;
    valueI=Math.min(Math.max(valueI, minI), maxI);
    super.set(min_, max_);
  }
  public void set(int value_) {
    valueI=value_;
    valueI=Math.min(Math.max(valueI, minI), maxI);
    super.set(value_);
  }
  //for attribute...
  public void setMin(int min_) {
    set(min_, max);
  }
  public void setMax(int max_) {
    set(min, max_);
  }
  @Override
  public void set(Object value) {
    valueI=(int)value;
  }
}
