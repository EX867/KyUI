package kyui.util;
import processing.core.PGraphics;
public class Rect {//it is like android's rect...
  public int left, right, top, bottom;
  public Rect(int left_, int top_, int right_, int bottom_) {
    left=left_;
    top=top_;
    right=right_;
    bottom=bottom_;
  }
  public void render(PGraphics g) {
    g.rect(left, top, right, bottom);
  }
  public boolean contains(int x, int y) {
    return x >= left && x <= right && y >= top && y <= bottom;
  }
  public boolean contains(float x, float y) {
    return x >= left && x <= right && y >= top && y <= bottom;
  }
}
