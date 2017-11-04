package kyui.element;
import kyui.core.Element;
import processing.core.PGraphics;
public class Background extends Element {
  int bgColor;
  public Background(String name, int c) {
    super(name);
    bgColor=c;
  }
  @Override
  public void render(PGraphics g) {
    g.background(bgColor);
  }
}
