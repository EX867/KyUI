package kyui.util;
import processing.core.PGraphics;
public class Rect implements Cloneable {//it is like android's rect...
  public int left, right, top, bottom;
  public Rect(int left_, int top_, int right_, int bottom_) {
    left=left_;
    top=top_;
    right=right_;
    bottom=bottom_;
  }
  public Rect(float left_, float top_, float right_, float bottom_) {
    left=(int)left_;
    top=(int)top_;
    right=(int)right_;
    bottom=(int)bottom_;
  }
  public void set(int left_, int top_, int right_, int bottom_) {
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
  public boolean contains(Rect r) {
    if (left == r.left && r.right == right && top == r.top && bottom == r.bottom) return true;
    if (left < r.left && right > r.right && top < r.top && bottom > r.bottom) return true;
    return false;
  }
  public boolean intersects(Rect r) {
    if (r.right <= left || r.left >= right || r.top >= bottom || r.bottom <= top) return false;
    return true;
  }
  @Override
  public String toString() {
    return "[(" + left + ", " + top + ")~(" + right + ", " + bottom + ")]";
  }
  @Override
  public Rect clone() {
    return new Rect(left, top, right, bottom);
  }
}
