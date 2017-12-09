package kyui.editor.inspectorItem;
import kyui.element.DropDown;
import kyui.element.LinearList;
public class InspectorDropDownButton extends InspectorButton<Enum> {
  public DropDown dropDown;
  public int selection=0;
  public Class Type_c;
  public InspectorDropDownButton(String name) {
    super(name);
    init();
  }
  public InspectorDropDownButton(String name, Class Type_c_) {
    super(name);
    Type_c=Type_c_;
    init();
  }
  private void init() {
    dropDown=new DropDown(getName() + ":dropDown");
    dropDown.setSelectListener((int index) -> {
      selection=index;
      dropDown.text=((LinearList.SelectableButton)dropDown.getPicker().getItems().get(index)).text;
    });
    addChild(dropDown);
  }
  @Override
  public void set(Enum value) {
    dropDown.text=value.toString();
  }
  @Override
  public Enum get() {
    return Enum.valueOf(Type_c, Type_c.getEnumConstants()[selection].toString());
  }
}