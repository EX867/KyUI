package kyui.editor.inspectorItem;
import kyui.element.FontDrop;
import processing.core.PFont;
public class InspectorFontButton extends InspectorButton<PFont> {
  public FontDrop fontDrop;
  public InspectorFontButton(String name) {
    super(name);
    init();
  }
  private void init() {
    fontDrop=new FontDrop(getName() + ":fontDrop");
    addChild(fontDrop);
  }
  @Override
  public void set(PFont value) {
    fontDrop.font=value;
  }
  @Override
  public PFont get() {
    return fontDrop.font;
  }
}