//package default;
public class TestElement extends kyui.element.Button{
  public TestElement(String name) {
    super(name);
    setPressListener((processing.event.MouseEvent e, int index) -> {
      System.out.println("[TestExternalElement] " + getClass().getTypeName() + " - " + getName() + " is pressed.");
      return false;
    });
  }
}
