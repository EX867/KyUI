package kyui.util;
public class Vector2 {
  public static Vector2 identity=new Vector2();
  public float x;
  public float y;
  public Vector2() {
    x=0;
    y=0;
  }
  public Vector2(float x_, float y_) {
    x=x_;
    y=y_;
  }
  public void set(float x_, float y_) {
    x=x_;
    y=y_;
  }
  public void set(Vector2 vector2) {
    x=vector2.x;
    y=vector2.y;
  }
  public Vector2 add(Vector2 other) {
    return new Vector2(x + other.x, y + other.y);
  }
  public Vector2 addAssign(Vector2 other) {
    x+=other.x;
    y+=other.y;
    return this;
  }
  public Vector2 sub(Vector2 other) {
    return new Vector2(x - other.x, y - other.y);
  }
  public Vector2 subAssign(Vector2 other) {
    x-=other.x;
    y-=other.y;
    return this;
  }
  public Vector2 multi(float r) {
    return new Vector2(x * r, y * r);
  }
  public Vector2 multiAssign(float r) {
    x*=r;
    y*=r;
    return this;
  }
  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
