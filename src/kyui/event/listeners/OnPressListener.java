package kyui.event.listeners;
import kyui.util.Vector2;
public interface OnPressListener {
  /**
   * @param position - position of press, which is relative to transform of element.
   */
  public void onPressed(Vector2 position);
}
