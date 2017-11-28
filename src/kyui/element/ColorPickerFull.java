package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import kyui.event.MouseEventListener;
import kyui.util.Rect;
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
    colorPicker=new ColorPicker(getName() + ":colorPicker");
    addChild(colorPicker);
    values[0]=new TextBox(getName() + ":red", "red", "0~255");
    values[1]=new TextBox(getName() + ":green", "green", "0~255");
    values[2]=new TextBox(getName() + ":blue", "blue", "0~255");
    values[3]=new TextBox(getName() + ":hue", "hue", "0~255");
    values[4]=new TextBox(getName() + ":saturation", "saturation", "0~255");
    values[5]=new TextBox(getName() + ":brightness", "brightness", "0~255");
    values[6]=new TextBox(getName() + ":alpha", "alpha", "0~255");
    for (int a=0; a < 10; a++) {
      int b=a;
      recentButtons[a]=new ColorButton(getName() + ":recentButtons" + a);
      recentButtons[a].setPressListener(new MouseEventListener() {
        @Override
        public boolean onEvent(MouseEvent e, int index) {
          System.out.println(b + " is pressed " + recentButtons[b].c);
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
            recentButtons[b].invalidate();
            recentButtons[b + 1].invalidate();
          }
          return false;
        }
      });
      addChild(recentButtons[a]);
    }
    colorPicker.setAdjustListener(new EventListener() {
      @Override
      public void onEvent(Element e) {
        recentButtons[0].c=colorPicker.selectedRGB;
        recentButtons[0].invalidate();
      }
    });
    for (int a=0; a < 7; a++) {
      addChild(values[a]);
    }
    colorPicker.attachRGB(values[0], values[1], values[2]);
    colorPicker.attachHSB(values[3], values[4], values[5]);
    colorPicker.attachA(values[6]);
    acceptButton=new Button(getName() + ":acceptButton");
    addChild(acceptButton);
    colorPicker.bgColor=0xFF7F7F7F;
  }
  @Override
  public void onLayout() {
    //default size 460*800==23*40
    float startX=pos.left;
    float startY=pos.top;
    float scale=1;
    if ((pos.right - pos.left) / 23 > (pos.bottom - pos.top) / 40) {//calc with height.
      startX+=((pos.right - pos.left) - (pos.bottom - pos.top) * 23 / 40) / 2;
      scale=(float)(pos.bottom - pos.top) / 800;
    } else {//calc with width.
      startY+=((pos.bottom - pos.top) - (pos.right - pos.left) * 40 / 23) / 2;
      scale=(float)(pos.right - pos.left) / 460;
    }
    colorPicker.setPosition(new Rect(startX + 0 * scale, startY + 0 * scale, startX + 460 * scale, startY + 460 * scale));
    for (int a=0; a < 6; a++) {
      values[a].setPosition(new Rect(startX + (10 + 150 * (a % 3)) * scale, startY + (470 + 70 * (a / 3)) * scale, startX + (150 + 150 * (a % 3)) * scale, startY + (530 + 70 * (a / 3)) * scale));
    }
    values[6].setPosition(new Rect(startX + 10 * scale, startY + 400 * scale, startX + 110 * scale, startY + 460 * scale));
    for (int a=0; a < 10; a++) {
      recentButtons[a].setPosition(new Rect(startX + (51 + 77 * (a % 5)) * scale, startY + (625 + 67 * (a / 5)) * scale, startX + (101 + 77 * (a % 5)) * scale, startY + (675 + 67 * (a / 5)) * scale));
    }
    acceptButton.setPosition(new Rect(startX + 390 * scale, startY + 10 * scale, startX + 450 * scale, startY + 70 * scale));
  }
}
