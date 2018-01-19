package kyui.core;
import kyui.editor.Attribute;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class RelativeFrame extends Element {
  protected float clickOffsetX=0;
  protected float clickOffsetY=0;
  protected float clickScrollMaxSq=0;
  //modifiable values
  @Attribute
  public boolean scroll=true;
  @Attribute
  public boolean drawAxis=true;
  @Attribute(layout=Attribute.SELF)
  public float intervalX=20;
  @Attribute(layout=Attribute.SELF)
  public float intervalY=20;
  @Attribute(layout=Attribute.SELF)
  public float scaleMin=0.3F;
  @Attribute(layout=Attribute.SELF)
  public float scaleMax=2.0F;
  Vector2 oldMouseValue=new Vector2();
  Vector2 oldClickValue=new Vector2();
  public RelativeFrame(String name) {
    super(name);
    clipping=true;
    relative=true;
    bgColor=KyUI.Ref.color(127);
  }
  @Override
  public void render(PGraphics g) {
    if (drawAxis) {
      g.strokeWeight(4);
      g.stroke(50);
      g.line(-1000, 0, 1000, 0);
      g.line(0, -1000, 0, 1000);
      g.noStroke();
    }
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    if (scroll && e != null) {
      if (e.getAction() == MouseEvent.PRESS) {
        if (entered) {//needs?
          clickOffsetX=transform.center.x;
          clickOffsetY=transform.center.y;
          clickScrollMaxSq=0;
        }
      } else if (e.getAction() == MouseEvent.DRAG) {
        if (pressedL) {
          requestFocus();
          float valueX=(KyUI.mouseClick.getLast().x - KyUI.mouseGlobal.getLast().x);
          float valueY=(KyUI.mouseClick.getLast().y - KyUI.mouseGlobal.getLast().y);
          float value=valueX * valueX + valueY * valueY;
          clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
          setOffset(clickOffsetX + valueX, clickOffsetY + valueY);
          invalidate();
          if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
            return false;
          } else {
            setOffset(clickOffsetX, clickOffsetY);
          }
        }
      } else if (e.getAction() == MouseEvent.RELEASE) {
        if (pressedL && clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
          return false;
        }
      } else if (e.getAction() == MouseEvent.WHEEL) {
        if (entered) {
          transform.scale-=(float)e.getCount() * 5 / 100;//only real scale on pointercount 2.
          if (transform.scale < scaleMin) {
            transform.scale=scaleMin;
          }
          if (transform.scale > scaleMax) {
            transform.scale=scaleMax;
          }
          invalidate();
          return false;
        }
      }
    }
    return true;
  }
  public void setOffset(float valueX, float valueY) {
    transform.center.x=valueX;
    transform.center.y=valueY;
  }
}
