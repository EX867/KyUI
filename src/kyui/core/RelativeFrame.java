package kyui.core;
import kyui.editor.Attribute;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class RelativeFrame extends Element {
  private float clickOffsetX=0;
  private float clickOffsetY=0;
  private float clickScrollMaxSq=0;
  public float offsetX=0;
  public float offsetY=0;
  //modifiable values
  @Attribute
  public boolean scroll=true;
  Vector2 oldMouseValue=new Vector2();
  Vector2 oldClickValue=new Vector2();
  public RelativeFrame(String name) {
    super(name);
    clipping=true;
    bgColor=KyUI.Ref.color(127);
    clipRect=new Rect();
  }
  @Override
  public void clipRect(PGraphics g) {
    if (renderFlag) {
      super.render(g);
    }
    //clipRect.set(pos.left - offsetX, pos.top - offsetY, pos.right - offsetX, pos.bottom - offsetY);
    KyUI.clipRect(g, clipRect.set(pos));
    KyUI.transform(offsetX, offsetY);
    g.pushMatrix();
    g.translate(-offsetX, -offsetY);
  }
  @Override
  public void render(PGraphics g) {
    //nothing!!
  }
  @Override
  public void removeClip(PGraphics g) {
    KyUI.restore();
    super.removeClip(g);
    g.popMatrix();
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    if (scroll) {
      if (e.getAction() == MouseEvent.PRESS) {
        if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
          clickOffsetX=offsetX;
          clickOffsetY=offsetY;
          clickScrollMaxSq=0;
          pressedL=true;//bad thing! FIX it.
        }
      } else if (e.getAction() == MouseEvent.DRAG) {
        if (pressedL) {
          requestFocus();
          float valueX=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
          float valueY=-(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
          float value=valueX * valueX + valueY * valueY;
          clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
          setOffset(clickOffsetX + valueX, clickOffsetY - valueY);
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
    }
    clipPos.getLast().getTranslated(offsetX, offsetY);
    KyUI.mouseGlobal.set(KyUI.mouseGlobal.x + offsetX, KyUI.mouseGlobal.y + offsetY);
    KyUI.mouseClick.set(KyUI.mouseClick.x + offsetX, KyUI.mouseClick.y + offsetY);
    //    kyui.editor.Main.mousextest=(int)KyUI.mouseGlobal.x;
    //    kyui.editor.Main.mouseytest=(int)KyUI.mouseGlobal.y;
    return true;
  }
  @Override
  synchronized boolean mouseEvent_(MouseEvent e, int index, boolean trigger) {
    oldMouseValue.set(KyUI.mouseGlobal);
    oldClickValue.set(KyUI.mouseClick);
    boolean ret=super.mouseEvent_(e, index, trigger);
    KyUI.mouseGlobal.set(oldMouseValue);
    KyUI.mouseClick.set(oldClickValue);
    return ret;
  }
  //offset 0 is root in center.
  public void setOffset(float valueX, float valueY) {
    //float size=pos.right - pos.left;
    //for (Element e : children) {
    //  e.movePosition(offsetX - valueX, offsetY - valueY);
    //}
    offsetX=valueX;
    offsetY=valueY;
  }
}
