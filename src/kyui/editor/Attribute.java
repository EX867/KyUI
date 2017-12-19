package kyui.editor;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.inspectorItem.*;
import kyui.element.ColorButton;
import kyui.element.LinearList;
import kyui.element.TextBox;
import kyui.element.TreeGraph;
import kyui.event.EventListener;
import kyui.util.DataTransferable;
import processing.event.MouseEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
  //layout param
  public int NONE=0;
  public int SELF=1;
  public int PARENT=2;
  //type param
  public int NORMAL=0;
  public int COLOR=1;//should be integer.(if not,ignored)
  String setter() default "";//if setterName=="", set valueI directly.
  String getter() default "";//same.
  int layout() default NONE;
  int type() default NORMAL;
  //inavlidate is called when set valueI
  public static class Editor {//Editor represents editable one Attribute. field of class
    public Attribute attr;
    Method getMethod;
    Method setMethod;
    public Class c;
    public Field field;
    public DataTransferable ref;//set manually by ElementLoader.AttributeSet. this can be null is ignored.
    public Editor(Attribute attr_, Class c_, Field field_) throws Exception {
      attr=attr_;
      c=c_;
      field=field_;//get field, and set information.
      field.setAccessible(true);//if...just if...
      if (!attr.getter().isEmpty()) {
        getMethod=c.getMethod(attr.getter());
        getMethod.setAccessible(true);
      }
      if (!attr.setter().isEmpty()) {
        setMethod=c.getMethod(attr.setter(), field.getType());
        setMethod.setAccessible(true);
      }
    }
    //objects are Element.
    public Object getField(Object o) throws Exception {
      if (getMethod == null) {
        return field.get(o);
      } else {
        return getMethod.invoke(o);//getMethod must have no parameter.
      }
    }
    public void setField(Object o, Object value) {
      try {
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
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    public void setRef(DataTransferable ref_) {
      ref=ref_;
      ref.setDataChangeListener((Element e) -> {
        setField(Main.selection, ref.get());
      });
    }
  }
}
