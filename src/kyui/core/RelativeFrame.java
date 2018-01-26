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
  public float scaleMin=0.1F;
  @Attribute(layout=Attribute.SELF)
  public float scaleMax=2.0F;
  Vector2 oldMouseValue=new Vector2();
  Vector2 oldClickValue=new Vector2();
  protected boolean scrolled=false;
  public RelativeFrame(String name) {
    super(name);
    clipping=true;
    relative=true;
    bgColor=KyUI.Ref.color(127);
    transform=new Transform(new Vector2((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2), 1);//maybe 0...
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
    float deltaX=(rect.left + rect.right - pos.left - pos.right) / 2;
    float deltaY=(rect.top + rect.bottom - pos.top - pos.bottom) / 2;
    super.setPosition(rect);
    transform.center.x+=deltaX;
    transform.center.y+=deltaY;
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {//from LinearLayout.
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    if (scroll) {
      if (e.getAction() == MouseEvent.PRESS) {
        scrolled=false;
        if (entered) {//needs?
          clickOffsetX=transform.center.x;// - centerX;
          clickOffsetY=transform.center.y;// - centerY;
          clickScrollMaxSq=0;
        }
      } else if (e.getAction() == MouseEvent.DRAG) {
        if (pressedL) {
          requestFocus();
          float valueX=(KyUI.mouseGlobal.getLast().x - KyUI.mouseClick.getLast().x) * KyUI.scaleGlobal;
          float valueY=(KyUI.mouseGlobal.getLast().y - KyUI.mouseClick.getLast().y) * KyUI.scaleGlobal;
          float value=valueX * valueX + valueY * valueY;
          clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
          setOffset(clickOffsetX + valueX, clickOffsetY + valueY);
          invalidate();
          if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
            scrolled=true;
            return false;
            //else setOffset(clickOffsetX, clickOffsetY);
          }
        }
      } else if (e.getAction() == MouseEvent.RELEASE) {
        if (pressedL && clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
          scrolled=true;
          return false;
        }
      } else if (e.getAction() == MouseEvent.WHEEL) {
        if (entered) {
          float oldScale=transform.scale;
          transform.scale-=(float)e.getCount() * 5 / 100;//only real scale on pointercount 2.
          if (transform.scale < scaleMin) {
            transform.scale=scaleMin;
          }
          if (transform.scale > scaleMax) {
            transform.scale=scaleMax;
          }
          Vector2 mouse=KyUI.mouseGlobal.getLast();
          setOffset(mouse.x + (transform.center.x - mouse.x) * transform.scale / oldScale, mouse.y + (transform.center.y - mouse.y) * transform.scale / oldScale);
          invalidate();
          return false;
        }
      }
    }
    return true;
  }
  public void setOffset(float valueX, float valueY) {
    transform.center.x=valueX;//(pos.left + pos.right) / 2 +
    transform.center.y=valueY;//(pos.top + pos.bottom) / 2 +
  }
}
