package kyui.editor.inspectorItem;
import kyui.element.ToggleButton;
public class InspectorToggleButton extends InspectorButton<Boolean> {
  public ToggleButton toggleButton;
  public InspectorToggleButton(String name) {
    super(name);
    init();
  }
  private void init() {
    toggleButton=new ToggleButton(getName() + ":toggleButton");
    addChild(toggleButton);
  }
  @Override
  public void set(Boolean value) {
    toggleButton.value=value;
  }
  @Override
  public Boolean get() {
    return toggleButton.value;
  }
}