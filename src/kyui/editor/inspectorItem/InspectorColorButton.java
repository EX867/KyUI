package kyui.editor.inspectorItem;
import kyui.core.KyUI;
import kyui.element.ColorButton;
import kyui.event.EventListener;
public class InspectorColorButton extends InspectorButton<Integer> {
  public ColorButton colorButton;
  public InspectorColorButton(String name) {
    super(name);
    init();
  }
  private void init() {
    colorButton=new ColorButton(getName() + ":colorButton");
    colorButton.bgColor=KyUI.Ref.color(127);
    colorButton.setPressListener(new ColorButton.OpenColorPickerEvent(colorButton));//auto for picking and storing color
    colorButton.c=KyUI.Ref.color((int)(Math.random() * 0xFF), (int)(Math.random() * 0xFF), (int)(Math.random() * 0xFF), 255);//FIX>>temporary
    addChild(colorButton);
  }
  @Override
  public void set(Integer value) {
    colorButton.c=value;
  }
  @Override
  public Integer get() {
    return colorButton.c;
  }
}