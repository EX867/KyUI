package kyui.core;
import kyui.editor.Attribute;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class RelativeFrame extends Element {
  protected float clickOffsetX=0;
  protected float clickOffsetY=0;
  protected float clickScrollMaxSq=0;
  public float offsetX=0;
  public float offsetY=0;
  public float scale=1.0F;
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
    bgColor=KyUI.Ref.color(127);
    clipRect=new Rect();
  }
  @Override
  public void clipRect(PGraphics g) {
    if (renderFlag) {
      super.render(g);
    }
    oldMouseValue.set(KyUI.mouseGlobal);
    oldClickValue.set(KyUI.mouseClick);
    //clipRect.set(pos.left - offsetX, pos.top - offsetY, pos.right - offsetX, pos.bottom - offsetY);
    KyUI.clipRect(g, clipRect.set(pos));
    KyUI.transform(offsetX * scale - (pos.left + pos.right) / 2, offsetY * scale - (pos.top + pos.bottom) / 2, scale);
    g.pushMatrix();
    g.translate((pos.left + pos.right) / 2 - offsetX * scale, (pos.top + pos.bottom) / 2 - offsetY * scale);
    g.scale(scale);
  }
  @Override
  public void render(PGraphics g) {
    if (drawAxis) {
      g.stroke(50);
      g.line(-1000, 0, 1000, 0);
      g.line(0, -1000, 0, 1000);
      float centerX=(pos.left + pos.right) / 2;
      float centerY=(pos.top + pos.bottom) / 2;
      g.noFill();
      g.ellipse((KyUI.mouseGlobal.x - centerX) / scale + offsetX, (KyUI.mouseGlobal.y - centerY) / scale + offsetY, 20, 20);
      g.noStroke();
    }
    //nothing!!
  }
  @Override
  public void update() {
    if (drawAxis) {
      invalidate();
    }
  }
  @Override
  public void removeClip(PGraphics g) {
    KyUI.restore();
    super.removeClip(g);
    g.popMatrix();
    KyUI.mouseGlobal.set(oldMouseValue);
    KyUI.mouseClick.set(oldClickValue);
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    if (scroll) {
      if (e.getAction() == MouseEvent.PRESS) {
        if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {//needs?
          clickOffsetX=offsetX;
          clickOffsetY=offsetY;
          clickScrollMaxSq=0;
          pressedL=true;//bad thing! FIX it.
        }
      } else if (e.getAction() == MouseEvent.DRAG) {
        if (pressedL) {
          requestFocus();
          float valueX=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal / scale;
          float valueY=(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal / scale;
          float value=valueX * valueX + valueY * valueY;
          clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
          setOffset(clickOffsetX + valueX, clickOffsetY + valueY);
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
      } else if (e.getAction() == MouseEvent.WHEEL) {
        if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
          scale-=(float)e.getCount() * 5 / 100;//only real scale on pointercount 2.
          if (scale < scaleMin) {
            scale=scaleMin;
          }
          if (scale > scaleMax) {
            scale=scaleMax;
          }
          invalidate();
          return false;
        }
      }
    }
    Rect r=clipPos.getLast();
    r.set((r.left - centerX) / scale + offsetX, (r.top - centerY) / scale + offsetY, (r.right - centerX) / scale + offsetX, (r.bottom - centerY) / scale + offsetY);
    KyUI.mouseGlobal.set((KyUI.mouseGlobal.x - centerX) / scale + offsetX, (KyUI.mouseGlobal.y - centerY) / scale + offsetY);
    KyUI.mouseClick.set((KyUI.mouseClick.x - centerX) / scale + offsetX, (KyUI.mouseClick.y - centerY) / scale + offsetY);
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
