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