package kyui.core;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
public class CachingFrame extends Element {
  protected PGraphics display;
  boolean invalidated=false;
  public CachingFrame(String name, Rect pos_) {
    super(name);
    pos=pos_;
    display=KyUI.Ref.createGraphics((int)(pos.right - pos.left), (int)(pos.bottom - pos.top));
  }
  @Override
  synchronized void render_(PGraphics g) {
    if (renderFlag || invalidated) render(null);//???
    renderChildren(display);
    if (renderFlag || invalidated) overlay(null);
    renderFlag=false;
    invalidated=false;
  }
  @Override
  public void render(PGraphics g) {
    //System.out.println(getName() + " rendered in " + KyUI.Ref.frameCount);
    display.beginDraw();
    if (bgColor != 0) {
      display.fill(bgColor);
      pos.render(display);
    }
    display.rectMode(PApplet.CORNERS);
    display.textAlign(PApplet.CENTER, PApplet.CENTER);
    display.noStroke();
    display.textFont(KyUI.fontMain);
  }
  @Override
  boolean checkInvalid(Rect rect) {
    invalidated=true;
    return super.checkInvalid(rect);
  }
  @Override
  public void overlay(PGraphics g) {
    display.endDraw();
  }
  public synchronized void renderReal(PGraphics g) {
    g.image(display, (pos.left + pos.right) / 2, (pos.bottom + pos.top) / 2);
  }
}
