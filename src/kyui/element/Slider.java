package kyui.element;
import kyui.core.Element;
import kyui.util.Rect;
import processing.core.PGraphics;
public class Slider extends Element{
  public Slider(String name) {
    super(name);
  }
  public Slider(String name,Rect pos_) {
    super(name);
    pos=pos_;
  }
  @Override public void render(PGraphics g){

  }
}
