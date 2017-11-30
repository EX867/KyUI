package kyui.util;
import java.util.ArrayList;
public class AttributeSet {
  ArrayList<Attribute> set;
  public AttributeSet() {
    set=new ArrayList<Attribute>();
  }
  public void add(Attribute a) {
    set.add(a);
  }
}
