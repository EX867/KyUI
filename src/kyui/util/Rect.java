package kyui.util;
import processing.core.PGraphics;
public class Rect {//it is like android's rect...
  int left, right, top, bottom;
  public Rect(int left_, int right_, int top_, int bottom_) {
    left=left_;
    right=right_;
    top=top_;
    bottom=bottom_;
  }
  public void render(PGraphics g) {
    g.rect(left, top, right, bottom);
  }
}
