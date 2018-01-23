package kyui.util;
public class Transform {
  public static final Transform identity=new Transform(new Vector2(0, 0), 1);
  public Vector2 center;
  public float scale;//scaled by parent.
  //no rotation.
  public Transform(Vector2 center_, float scale_) {
    center=center_;
    scale=scale_;
  }
  //no allocation optimization.
  public Vector2 trans(Transform before, Vector2 v) {
    return new Vector2(transX(before, v.x), transY(before, v.y));
  }
  public Rect trans(Transform before, Rect r) {
    return new Rect(transX(before, r.left), transY(before, r.top), transX(before, r.right), transY(before, r.bottom));
  }
  public float transX(Transform before, float x) {
    return ((before.center.x - center.x) + (x - before.center.x) * before.scale) / scale;
  }
  public float transY(Transform before, float y) {
    return ((before.center.y - center.y) + (y - before.center.y) * before.scale) / scale;
  }
  public static Transform add(Transform a, Transform b) {
    return new Transform(a.trans(b, b.center), b.scale * a.scale);
  }
  @Override
  public String toString() {
    return "{" + center + ", " + scale + "}";
  }
}
