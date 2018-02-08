package kyui.core;
import kyui.util.Rect;
import kyui.util.Transform;
import processing.core.PApplet;
import processing.core.PGraphics;
public final class CachingFrame extends Element {
  public PGraphics display;
  public boolean invalidated=false;
  public int alpha=0;
  public CachingFrame(String name, Rect pos_) {
    super(name);
    pos=pos_;
    display=KyUI.Ref.createGraphics((int)(pos.right - pos.left), (int)(pos.bottom - pos.top));
  }
  public CachingFrame setAlpha(int a){
    alpha=a;
    return this;
  }
  @Override
  synchronized void render_(PGraphics g) {
    //    if (renderFlag_) {
    //      renderAfter(g);
    //    }
    boolean a=renderFlag || invalidated;
    if (a) render(null);//???
    if (relative) {
      transformRender(display);
    }
    renderChildren(display);
    if (relative) {
      transformRenderAfter(display);
    }
    if (a) {
      display.popMatrix();
      display.endDraw();
    }
    renderFlag=false;
    invalidated=false;
  }
  @Override
  public void render(PGraphics g) {
    display.beginDraw();
    display.pushMatrix();
    display.scale(KyUI.scaleGlobal);
    if (renderFlag) {
      display.clear();
    }
    display.rectMode(PApplet.CORNERS);
    display.textAlign(PApplet.CENTER, PApplet.CENTER);
    display.noStroke();
    display.textFont(KyUI.fontMain);
  }
  public void setTransform(Transform t) {//use carefully!
    relative=true;
    transform=t;
  }
  @Override
  boolean checkInvalid(Rect rect, Rect bounds, Transform last) {
    invalidated=true;
    return super.checkInvalid(rect, bounds, last);
  }
  public void resize(int width, int height) {
    if (width == 0 || height == 0) {
      return;
    }
    display.dispose();
    display=KyUI.Ref.createGraphics(width, height);
    onLayout();//FIX>> resize layout problem!!!
    invalidate();
  }
  public synchronized void renderReal(PGraphics g) {
    g.image(display, display.width / 2, display.height / 2);
  }
  public void clear() {
    display.beginDraw();
    display.clear();
    display.endDraw();
  }
}
