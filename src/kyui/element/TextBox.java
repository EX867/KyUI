package kyui.element;
import kyui.util.Rect;
public class TextBox  extends TextEdit{
  String title="";
  int value;
  public TextBox(String name) {
    super(name);
  }
  public TextBox(String name, Rect pos_) {
    super(name, pos_);
  }
}
