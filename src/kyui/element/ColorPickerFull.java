package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.MouseEventListener;
import processing.event.MouseEvent;
public class ColorPickerFull extends Element {
  ColorPicker colorPicker;
  TextBox[] values=new TextBox[7];
  ColorButton[] recentButtons=new ColorButton[10];
  Button acceptButton;
  public ColorPickerFull(String name) {
    super(name);
    init();
  }
  private void init() {
    colorPicker=new ColorPicker(getName()+":colorPicker");
    addChild(colorPicker);
    values[0]=new TextBox(getName() + ":red");
    values[1]=new TextBox(getName() + ":green");
    values[2]=new TextBox(getName() + ":blue");
    values[3]=new TextBox(getName() + ":hue");
    values[4]=new TextBox(getName() + ":saturation");
    values[5]=new TextBox(getName() + ":brightness");
    values[6]=new TextBox(getName() + ":alpha");
    for(int a=0;a<7;a++){
      addChild(values[a]);
    }
    colorPicker.attachRGB(values[0], values[1], values[2]);
    colorPicker.attachHSB(values[3], values[4], values[5]);
    colorPicker.attachA(values[6]);
    for (int a=0; a < 10; a++) {
      int b=a;
      recentButtons[a]=new ColorButton(getName() + ":recentButtons" + a);
      recentButtons[a].setPressListener(new MouseEventListener() {
        @Override
        public boolean onEvent(MouseEvent e, int index) {
          colorPicker.setColorRGB(recentButtons[b].c);
          if (b == 0) {
            recentButtons[b].c=colorPicker.selectedRGB;
          }
          if (b != recentButtons.length - 1) {
            if (recentButtons[b + 1].c != recentButtons[b].c) {
              for (int a=b; a < recentButtons.length - 1; a++) {
                recentButtons[a + 1].c=recentButtons[a].c;
              }
            }
          }
          return false;
        }
      });
      addChild(recentButtons[a]);
    }
    acceptButton=new Button(getName()+":acceptButton");
    addChild(acceptButton);
  }
  @Override
  public void onLayout() {
    //ADD>>set position
  }
}
