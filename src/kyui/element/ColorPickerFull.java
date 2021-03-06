package kyui.element;
import kyui.core.Element;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.MouseEventListener;
import kyui.loader.ElementLoader;
import kyui.util.Rect;
import processing.event.MouseEvent;
public class ColorPickerFull extends Element {
  public ColorPicker colorPicker;
  TextBox[] values = new TextBox[7];
  ColorButton[] recentButtons = new ColorButton[10];
  ImageButton acceptButton;
  @Attribute(setter = "setEnableAlpha", getter = "getEnableAlpha")
  boolean enableAlpha;
  @Attribute(setter = "setEnableX", getter = "getEnableX")
  boolean enableX;
  @Attribute(setter = "setSelectable")
  protected boolean selectable = false;
  public EventListener colorSelectListener = (e) -> {
  };
  public ColorButton selected = null;
  void setEnableAlpha(boolean v) {
    values[6].setEnabled(v);
  }
  boolean getEnableAlpha() {
    return values[6].isEnabled();
  }
  void setEnableX(boolean v) {
    acceptButton.setEnabled(v);
  }
  boolean getEnableX() {
    return acceptButton.isEnabled();
  }
  public ColorPickerFull(String name) {
    super(name);
    init();
  }
  public void setSelectable(boolean v) {
    selectable = v;
    if (v) {
      selected = recentButtons[0];
      selected.selected = true;
      selected.invalidate();
    } else {
      for (int a = 0; a < recentButtons.length; a++) {
        recentButtons[a].selected = false;
        invalidate();
      }
    }
  }
  private void init() {
    colorPicker = new ColorPicker(getName() + ":colorPicker");
    values[0] = new TextBox(getName() + ":red", "red", "0~255");
    values[1] = new TextBox(getName() + ":green", "green", "0~255");
    values[2] = new TextBox(getName() + ":blue", "blue", "0~255");
    values[3] = new TextBox(getName() + ":hue", "hue", "0~255");
    values[4] = new TextBox(getName() + ":saturation", "saturation", "0~255");
    values[5] = new TextBox(getName() + ":brightness", "brightness", "0~255");
    values[6] = new TextBox(getName() + ":alpha", "alpha", "0~255");
    addChild(colorPicker);
    for (int a = 0; a < 10; a++) {
      int b = a;
      recentButtons[a] = new ColorButton("");//getName() + ":recentButtons" + a);
      recentButtons[a].setPressListener(new MouseEventListener() {
        @Override
        public boolean onEvent(MouseEvent e, int index) {
          colorPicker.setColorRGBA(recentButtons[b].c);
          if (b == 0) {
            recentButtons[b].c = colorPicker.selectedRGB;
            if (recentButtons[0].c != recentButtons[1].c) {
              for (int c = recentButtons.length - 2; c >= 0; c--) {
                recentButtons[c + 1].c = recentButtons[c].c;
                recentButtons[c + 1].invalidate();
              }
            }
          }
          colorSelectListener.onEvent(recentButtons[b]);
          if (selectable) {
            for (int a = 0; a < 10; a++) {
              recentButtons[a].selected = false;
              recentButtons[a].invalidate();
            }
            //            if (selected != null) {
            //              selected.selected = false;
            //              selected.invalidate();
            //            }
            selected = recentButtons[b];
            selected.selected = true;
          }
          colorPicker.invalidate();
          return false;
        }
      });
      addChild(recentButtons[a]);
    }
    colorPicker.setAdjustListener(new EventListener() {
      @Override
      public void onEvent(Element e) {
        recentButtons[0].c = colorPicker.selectedRGB;
        if (selectable && recentButtons[0] == selected) {
          colorSelectListener.onEvent(recentButtons[0]);
        }
        recentButtons[0].invalidate();
      }
    });
    for (int a = 0; a < 7; a++) {
      addChild(values[a]);
    }
    colorPicker.attachRGB(values[0], values[1], values[2]);
    colorPicker.attachHSB(values[3], values[4], values[5]);
    colorPicker.attachA(values[6]);
    acceptButton = new ImageButton(getName() + ":acceptButton", ElementLoader.loadImageResource("exit.png"));
    addChild(acceptButton);
    colorPicker.bgColor = 0xFF7F7F7F;
  }
  @Override
  public void onLayout() {
    //default size 460*800==23*40
    float startX = pos.left;
    float startY = pos.top;
    float scale = 1;
    if ((pos.right - pos.left) / 23 > (pos.bottom - pos.top) / 40) {//calc with height.
      startX += ((pos.right - pos.left) - (pos.bottom - pos.top) * 23 / 40) / 2;
      scale = (float)(pos.bottom - pos.top) / 800;
    } else {//calc with width.
      startY += ((pos.bottom - pos.top) - (pos.right - pos.left) * 40 / 23) / 2;
      scale = (float)(pos.right - pos.left) / 460;
    }
    colorPicker.setPosition(new Rect(startX + 0 * scale, startY + 0 * scale, startX + 460 * scale, startY + 460 * scale));
    for (int a = 0; a < 6; a++) {
      values[a].setPosition(new Rect(startX + (10 + 150 * (a % 3)) * scale, startY + (470 + 70 * (a / 3)) * scale, startX + (150 + 150 * (a % 3)) * scale, startY + (530 + 70 * (a / 3)) * scale));
    }
    values[6].setPosition(new Rect(startX + 10 * scale, startY + 400 * scale, startX + 110 * scale, startY + 460 * scale));
    for (int a = 0; a < 10; a++) {
      if (recentButtons[a] != null) {//FIX>>strange...
        recentButtons[a].setPosition(new Rect(startX + (51 + 77 * (a % 5)) * scale, startY + (625 + 67 * (a / 5)) * scale, startX + (101 + 77 * (a % 5)) * scale, startY + (675 + 67 * (a / 5)) * scale));
      }
    }
    if (acceptButton != null) {//FIX>>strange...
      acceptButton.setPosition(new Rect(startX + 390 * scale, startY + 10 * scale, startX + 450 * scale, startY + 70 * scale));
    }
  }
  public void setColorRGB(int c) {
    colorPicker.setColorRGB(c);
    recentButtons[0].c = c;
  }
  @Override
  public void setBgColor(int c) {
    super.setBgColor(c);
    colorPicker.setBgColor(c);
  }
}
