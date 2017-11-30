package kyui.util;
public abstract class Attribute<Type> {
  public String name;
  public Class type;
  public Attribute(String name_, Type defaultValue) {
    name=name_;
    set(defaultValue);
    type=defaultValue.getClass();
  }
  public abstract Type get();//get value from attached view.
  public abstract void set(Type value);//set modified value to attribute.
}
