package kyui.core;
import kyui.editor.Attribute;
import kyui.util.Rect;
import kyui.util.Transform;
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
    transform=new Transform(new Vector2(), 1);
  }
  @Override
  public void render(PGraphics g) {
    super.render(g);
    if (drawAxis) {
      g.strokeWeight(4 * transform.scale);
      g.stroke(50);
      g.line(transform.center.x, transform.center.y - 1000 * transform.scale, transform.center.x, transform.center.y + 1000 * transform.scale);
      g.line(transform.center.x - 1000 * transform.scale, transform.center.y, transform.center.x + 1000 * transform.scale, transform.center.y);
      g.noStroke();
    }
  }
  @Override
  public void setPosition(Rect rect) {
    super.setPosition(rect);
    transform.center.x=(pos.left + pos.right) / 2;
    transform.center.y=(pos.top + pos.bottom) / 2;
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    if (scroll) {
      if (e.getAction() == MouseEvent.PRESS) {
        if (entered) {//needs?
          clickOffsetX=transform.center.x - centerX;
          clickOffsetY=transform.center.y - centerY;
          clickScrollMaxSq=0;
        }
      } else if (e.getAction() == MouseEvent.DRAG) {
        if (pressedL) {
          requestFocus();
          float valueX=(KyUI.mouseGlobal.getFirst().x - KyUI.mouseClick.getFirst().x);
          float valueY=(KyUI.mouseGlobal.getFirst().y - KyUI.mouseClick.getFirst().y);
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
    transform.center.x=(pos.left + pos.right) / 2 + valueX;
    transform.center.y=(pos.top + pos.bottom) / 2 + valueY;
  }
}
