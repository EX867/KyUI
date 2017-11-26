package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
public class ColorPickerFull extends Element {
  ColorPicker colorPicker;
  TextBox[] values=new TextBox[7];
  Button[] recentButtons=new Button[10];
  public ColorPickerFull(String name) {
    super(name);
    initStatic();
  }
  private void initStatic() {
    colorPicker=new ColorPicker("KyUI:ColorPickerFull.colorPicker");
    values[0]=new TextBox(getName() + ":red");
    values[1]=new TextBox(getName() + ":green");
    values[2]=new TextBox(getName() + ":blue");
    values[3]=new TextBox(getName() + ":hue");
    values[4]=new TextBox(getName() + ":saturation");
    values[5]=new TextBox(getName() + ":brightness");
    values[6]=new TextBox(getName() + ":alpha");
    for (int a=0; a < 10; a++) {
      recentButtons[a]=new Button(getName() + ":recentButtons" + a);
    }
  }
}
