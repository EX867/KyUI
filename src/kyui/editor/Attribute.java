package kyui.editor;
import kyui.core.Element;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
public @interface Attribute {
  //layout param
  public int NONE=0;
  public int SELF=1;
  public int PARENT=2;
  //type param
  public int NORMAL=0;
  public int COLOR=1;//should be integer.(if not,ignored)
  String setter() default "";//if setterName=="", set value directly.
  String getter() default "";//same.
  int layout() default NONE;
  int type() default NORMAL;
  //inavlidate is called when set value
  public static class Editor {//Editor represents editable one Attribute. field of class
    public Attribute attr;
    Method getMethod;
    Method setMethod;
    public Class c;
    public Field field;
    public Editor(Attribute attr_, Class c_, Field field_) throws Exception {
      attr=attr_;
      c=c_;
      field=field_;//get field, and set information.
      field.setAccessible(true);
      if (!attr.getter().isEmpty()) {
        getMethod=c.getMethod(attr.getter());
      }
      getMethod.setAccessible(true);
      if (!attr.setter().isEmpty()) {
        setMethod=c.getMethod(attr.setter());
      }
      setMethod.setAccessible(true);
    }
    //objects are Element.
    public Object getField(Object o) throws Exception {
      if (getMethod == null) {
        return field.get(o);
      } else {
        return getMethod.invoke(o);//getMethod must have no parameter.
      }
    }
    public void setField(Object o, Object value) throws Exception {
      if (setMethod == null) {
        field.set(o, value);
        Element e=(Element)o;
        if (attr.layout() == Attribute.PARENT) {
          if (e.parents.size() > 0) {
            e.parents.get(0).localLayout();
          }
        } else if (attr.layout() == Attribute.SELF) {
          e.localLayout();
        }
      } else {
        setMethod.invoke(o, value);//setMethod must have one parameter.
      }
      ((Element)o).invalidate();
    }
  }
}
