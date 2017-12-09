package kyui.editor.inspectorItem;
import kyui.element.TextBox;
public class InspectorTextButton<Type> extends InspectorButton<Type> {//type must be string or int or float...
  public TextBox textBox;//get this TextBox directly and you can modify this.
  public Class Type_c;//oh..dirty. if it is not same from Type, it will not work.
  //usually you can do set Type just Object. (you don't need to use get and set...it is just for automation)
  public InspectorTextButton(String name) {
    super(name);
    init();
  }
  public InspectorTextButton(String name, Class Type_c_) {//class parameter only do this and you can even change manually.
    super(name);
    init();
    Type_c=Type_c_;
    if (Type_c == Integer.class || Type_c == int.class) {
      textBox.setNumberOnly(TextBox.NumberType.INTEGER);
    } else if (Type_c == Float.class || Type_c == float.class) {
      textBox.setNumberOnly(TextBox.NumberType.FLOAT);
    } else if (Type_c == String.class) {
      textBox.setNumberOnly(TextBox.NumberType.NONE);
    }
  }
  private void init() {
    textBox=new TextBox(getName() + ":texBox");
    addChild(textBox);
  }
  @Override
  public void set(Type value) {
    textBox.setText(castToString(value));
  }
  @Override
  public Type get() {
    if (Type_c == Integer.class || Type_c == int.class) {
      return (Type)new Integer(textBox.valueI);
    } else if (Type_c == Float.class || Type_c == float.class) {
      return (Type)new Float(textBox.valueF);
    } else if (Type_c == String.class) {
      return (Type)textBox.getText();
    } else {
      return null;//this will make NullPointerException. but this is already an error.
    }
  }
  String castToString(Type value) {
    if (value.getClass() == Integer.class || value.getClass() == int.class) {
      return "" + (Integer)value;
    } else if (value.getClass() == Float.class || value.getClass() == float.class) {
      return "" + (Float)value;
    } else if (value.getClass() == String.class) {
      return (String)value;
    } else {
      return "";
    }
  }
}