package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Vector2;
import processing.event.MouseEvent;
//FIX>> process relative position in more general and correct way...
public class RelativeFrame extends Element {
  private float clickOffsetX=0;
  private float clickOffsetY=0;
  private float clickScrollMaxSq=0;
  public float offsetX=0;
  public float offsetY=0;
  //modifiable values
  public boolean scroll=true;
  public RelativeFrame(String name) {
    super(name);
    clipping=true;
    bgColor=KyUI.Ref.color(127);
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    if (!scroll) {
      return true;
    }
    if (e.getAction() == MouseEvent.PRESS) {
      clickOffsetX=offsetX;
      clickOffsetY=offsetY;
      clickScrollMaxSq=0;
    } else if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL) {
        requestFocus();
        float valueX=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        float valueY=-(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        float value=valueX * valueX + valueY * valueY;
        clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
        setOffset(clickOffsetX + valueX, clickOffsetY - valueY);
        onLayout();
        invalidate();
        if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
          return false;
        } else {
          offsetX=clickOffsetX;
          offsetY=clickOffsetY;
        }
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL && clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
        return false;
      }
    }
    return true;
  }
  //offset 0 is root in center.
  public void setOffset(float valueX, float valueY) {
    float size=pos.right - pos.left;
    for (Element e : children) {
      e.movePosition(offsetX - valueX, offsetY - valueY);
    }
    offsetX=valueX;
    offsetY=valueY;
  }
}
