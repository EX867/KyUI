package kyui.util;
public class Transform {
  public Vector2 pos;
  public float scale;
  //no rotation.
  public Transform(Vector2 pos_, float scale_) {
    pos=pos_;
    scale=scale_;
  }
}
