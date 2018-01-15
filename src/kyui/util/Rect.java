package kyui.util;
import processing.core.PGraphics;
public class Rect implements Cloneable {//it is like android's rect...
  public float left, right, top, bottom;
  public Rect() {
    left=0;
    right=0;
    top=0;
    bottom=0;
  }
  public Rect(float left_, float top_, float right_, float bottom_) {
    left=left_;
    top=top_;
    right=right_;
    bottom=bottom_;
  }
  public Rect set(float left_, float top_, float right_, float bottom_) {
    left=left_;
    top=top_;
    right=right_;
    bottom=bottom_;
    return this;
  }
  public Rect set(Rect other) {
    left=other.left;
    right=other.right;
    top=other.top;
    bottom=other.bottom;
    return this;
  }
  public void render(PGraphics g) {
    g.rect(left, top, right, bottom);
  }
  public void render(PGraphics g, int extend) {
    g.rect(left - extend, top - extend, right + extend, bottom + extend);
  }
  public boolean contains(float x, float y) {
    return x >= left && x <= right && y >= top && y <= bottom;
  }
  public boolean contains(Rect r) {
    //if (left == r.left && r.right == right && top == r.top && bottom == r.bottom) return true;
    //if (left <= r.left && right >= r.right && top <= r.top && bottom >= r.bottom) return true;
    if (left < r.left && right > r.right && top < r.top && bottom > r.bottom) return true;
    return false;
  }
  private boolean containsX(Rect r) {
    return left <= r.left && right >= r.right;
  }
  private boolean containsY(Rect r) {
    return top <= r.top && bottom >= r.bottom;
  }
  public boolean intersects(Rect r) {
    if (r.right < left || r.left > right || r.top > bottom || r.bottom < top) return false;
    if (contains(r) || r.contains(this)) return false;
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
  public static Rect getIntersection(Rect a, Rect b, Rect container) {
    container.set(0, 0, 0, 0);
    if (a.containsX(b)) {
      container.left=b.left;
      container.right=b.right;
    } else if (b.containsX(a)) {
      container.left=a.left;
      container.right=a.right;
    } else {
      if (a.left > b.left) {
        Rect temp=a;
        a=b;
        b=temp;
      }//now a is left than b.
      if (a.right >= b.left) {//intersects in x axis
        container.left=b.left;
        container.right=a.right;
      }
    }
    if (a.containsY(b)) {
      container.top=b.top;
      container.bottom=b.bottom;
    } else if (b.containsY(a)) {
      container.top=a.top;
      container.bottom=a.bottom;
    } else {
      if (a.top > b.top) {
        Rect temp=a;
        a=b;
        b=temp;
      }//now a is up than b.
      if (a.bottom >= b.top) {//intersects in y axis
        container.top=b.top;
        container.bottom=a.bottom;
      }
    }
    return container;
  }
  public Rect translate(float x, float y) {
    return new Rect(left + x, top + y, right + x, bottom + y);
  }
  public Rect getTranslated(float x, float y) {
    return set(left + x, top + y, right + x, bottom + y);
  }
  public Vector2 getSize() {
    return new Vector2(right - left, bottom - top);
  }
  public Rect getScaled(float scale) {
    float centerX=(left + right) / 2;
    float centerY=(top + bottom) / 2;
    float hWidth=right - centerX;
    float hHeight=bottom - centerY;
    return set(centerX - scale * hWidth, centerY - scale * hHeight, centerX + scale * hWidth, centerY + scale * hHeight);
  }
}
