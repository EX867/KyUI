package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import kyui.util.Rect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.awt.Color;
public class ColorPicker extends Element {
  ColorPicker this_=this;
  class ColorModifyListenerRGB implements EventListener {
    @Override
    public void onEvent(Element e) {
      updateColorFromRGB();
      ((TextBox)e).value=Math.max(0, Math.min(255, ((TextBox)e).value));
      if (onAdjust != null) {
        onAdjust.onEvent(this_);
      }
      invalidate();
    }
  }
  class ColorModifyListenerHSB implements EventListener {
    @Override
    public void onEvent(Element e) {
      updateColorFromHSB();
      ((TextBox)e).value=Math.max(0, Math.min(255, ((TextBox)e).value));
      if (onAdjust != null) {
        onAdjust.onEvent(this_);
      }
      invalidate();
    }
  }
  class ColorModifyListenerA implements EventListener {
    @Override
    public void onEvent(Element e) {
      updateColorFromA();
      ((TextBox)e).value=Math.max(0, Math.min(255, ((TextBox)e).value));
      if (onAdjust != null) {
        onAdjust.onEvent(this_);
      }
      invalidate();
    }
  }
  ColorModifyListenerRGB rgbListener=new ColorModifyListenerRGB();
  ColorModifyListenerHSB hsbListener=new ColorModifyListenerHSB();
  ColorModifyListenerA aListener=new ColorModifyListenerA();
  EventListener onAdjust;
  PGraphics colorImage;
  //attachments
  protected TextBox red;
  protected TextBox green;
  protected TextBox blue;
  protected TextBox hue;
  protected TextBox saturation;
  protected TextBox brightness;
  protected TextBox alpha;
  protected int selectedRGB=0;
  protected int selectedHSB=0;
  //temp vars
  boolean hueClicked=false;
  boolean sbClicked=false;
  int alphav=255;
  Rect cacheRect=new Rect();
  public ColorPicker(String name) {
    super(name);
    padding=6;
    bgColor=KyUI.Ref.color(127);
  }
  public ColorPicker(String name, Rect pos_) {
    super(name);
    pos=pos_;
    padding=6;
    bgColor=KyUI.Ref.color(127);
    createColorImage();
  }
  public void setAdjustListener(EventListener e) {
    onAdjust=e;
  }
  @Override
  public void setPosition(Rect rect) {
    super.setPosition(rect);
    createColorImage();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    float centerX=(pos.right + pos.left) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    float width=Math.min((pos.right - pos.left), (pos.bottom - pos.top));
    if (e.getAction() == MouseEvent.PRESS && ((KyUI.mouseGlobal.x - centerX) * (KyUI.mouseGlobal.x - centerX) + (KyUI.mouseGlobal.y - centerY) * (KyUI.mouseGlobal.y - centerY) > width * width / 4)) {
      skipPress=true;
      pressedL=false;
      return true;
    } else if (pressedL && e.getAction() == MouseEvent.RELEASE && ((KyUI.mouseGlobal.x - centerX) * (KyUI.mouseGlobal.x - centerX) + (KyUI.mouseGlobal.y - centerY) * (KyUI.mouseGlobal.y - centerY) > width * width / 4)) {
      skipRelease=true;
      return true;
    }
    if (e.getAction() == MouseEvent.PRESS || ((e.getAction() == MouseEvent.DRAG || e.getAction() == MouseEvent.RELEASE) && pressedL)) {
      requestFocus();
      float radius=(float)Math.sqrt((KyUI.mouseGlobal.x - centerX) * (KyUI.mouseGlobal.x - centerX) + (KyUI.mouseGlobal.y - centerY) * (KyUI.mouseGlobal.y - centerY));
      if (((width / 4 < radius && radius < width / 2) || hueClicked) && sbClicked == false) {
        float atan2pos=(float)Math.atan2(KyUI.mouseGlobal.y - centerY, KyUI.mouseGlobal.x - centerX);
        if (atan2pos < 0) atan2pos+=PApplet.TWO_PI;
        selectedHSB=KyUI.Ref.color((atan2pos) * 256 / PApplet.TWO_PI, KyUI.Ref.green(selectedHSB), KyUI.Ref.blue(selectedHSB), alphav);
        selectedRGB=Color.HSBtoRGB(KyUI.Ref.red(selectedHSB) / 255, KyUI.Ref.green(selectedHSB) / 255, KyUI.Ref.blue(selectedHSB) / 255);
        selectedRGB=KyUI.Ref.color(KyUI.Ref.red(selectedRGB), KyUI.Ref.green(selectedRGB), KyUI.Ref.blue(selectedRGB), alphav);
        updateColorRGB();
        updateColorHSB();
        hueClicked=true;
        invalidate();
      } else if ((cacheRect.set(centerX - width / 6, centerY - width / 6, centerX + width / 6, centerY + width / 6).contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y) || sbClicked) && hueClicked == false) {
        selectedHSB=KyUI.Ref.color(KyUI.Ref.red(selectedHSB), (KyUI.mouseGlobal.x - centerX + width / 6) * 255 / (width / 3), (centerY + width / 6 - KyUI.mouseGlobal.y) * 255 / (width / 3), alphav);
        selectedRGB=Color.HSBtoRGB(KyUI.Ref.red(selectedHSB) / 255, KyUI.Ref.green(selectedHSB) / 255, KyUI.Ref.blue(selectedHSB) / 255);
        selectedRGB=KyUI.Ref.color(KyUI.Ref.red(selectedRGB), KyUI.Ref.green(selectedRGB), KyUI.Ref.blue(selectedRGB), alphav);
        updateColorRGB();
        updateColorHSB();
        sbClicked=true;
        invalidate();
      }
      if (onAdjust != null) {
        onAdjust.onEvent(this);
      }
      if (e.getAction() == MouseEvent.RELEASE) {
        hueClicked=false;
        sbClicked=false;
      }
      return false;
    }
    return true;
  }
  @Override
  public void render(PGraphics g) {
    if (colorImage == null) {
      createColorImage();
      if (colorImage == null) {
        return;
      }
    }
    float centerX=(pos.right + pos.left) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    float width=Math.min((pos.right - pos.left), (pos.bottom - pos.top));
    //draw hue image
    g.imageMode(PApplet.CENTER);
    g.image(colorImage, centerX, centerY);//includes bg.
    //draw hue marker
    g.stroke(0);
    g.strokeWeight(1);
    g.noFill();
    //
    g.pushMatrix();
    g.translate(centerX, centerY);
    g.pushMatrix();
    g.rotate(KyUI.Ref.red(selectedHSB) * PApplet.TWO_PI / 255 - PApplet.radians(90));
    g.rectMode(PApplet.CENTER);
    g.rect(0, width / 3, 8, width / 6 + 12);
    g.line(0, width / 4 - 6, 0, width * 5 / 12 + 6);
    g.popMatrix();
    //draw sb
    g.noStroke();
    for (int a=2; a < 256; a+=4) {
      for (int b=2; b < 256; b+=4) {
        g.fill(Color.HSBtoRGB((float)KyUI.Ref.red(selectedHSB) / 255, (float)a / 255, (float)b / 255));
        g.rect(-width / 6 + width * a / (256 * 3), width / 6 - width * b / (256 * 3), width * 4 / (256 * 3), width * 4 / (256 * 3));
      }
    }
    //draw edge of sb
    g.noFill();
    g.strokeWeight(1);
    g.stroke(0);
    g.rect(0, 0, width / 3, width / 3);
    //draw sb marker
    if (KyUI.Ref.blue(selectedHSB) > 128) g.stroke(0);
    else g.stroke(255);
    g.rect(-width / 6 + KyUI.Ref.green(selectedHSB) * width / 765, width / 6 - KyUI.Ref.blue(selectedHSB) * width / 765, 8, 8);
    //end
    g.popMatrix();
    g.noStroke();
    g.rectMode(PApplet.CORNERS);
  }
  public void attachRGB(TextBox r, TextBox g, TextBox b) {
    red=r;
    green=g;
    blue=b;
    r.setTextChangeListener(rgbListener);
    g.setTextChangeListener(rgbListener);
    b.setTextChangeListener(rgbListener);
    updateColorRGB();
  }
  public void attachHSB(TextBox h, TextBox s, TextBox b) {
    hue=h;
    saturation=s;
    brightness=b;
    h.setTextChangeListener(hsbListener);
    s.setTextChangeListener(hsbListener);
    b.setTextChangeListener(hsbListener);
    updateColorHSB();
  }
  public void attachA(TextBox a) {
    alpha=a;
    a.setTextChangeListener(aListener);
    a.setText("255");
  }
  public int getColor() {
    return selectedRGB;
  }
  public void setColorRGB(int c) {
    selectedRGB=c;
    updateColorRGB();
    selectedHSB=KyUI.Ref.color(KyUI.Ref.hue(selectedRGB), KyUI.Ref.saturation(selectedRGB), KyUI.Ref.brightness(selectedRGB));
    updateColorHSB();
  }
  void updateColorFromRGB() {
    if (red == null) return;//only compare once...
    selectedRGB=KyUI.Ref.color(red.value, green.value, blue.value);
    selectedHSB=KyUI.Ref.color(KyUI.Ref.hue(selectedRGB), KyUI.Ref.saturation(selectedRGB), KyUI.Ref.brightness(selectedRGB));
    updateColorHSB();
  }
  void updateColorFromHSB() {
    if (hue == null) return;//only compare once...
    selectedHSB=KyUI.Ref.color(hue.value, saturation.value, brightness.value);
    selectedRGB=Color.HSBtoRGB(KyUI.Ref.red(selectedHSB) / 255, KyUI.Ref.green(selectedHSB) / 255, KyUI.Ref.blue(selectedHSB) / 255);
    updateColorRGB();
  }
  void updateColorFromA() {
    if (alpha == null) return;
    alphav=alpha.value;
    selectedRGB=KyUI.Ref.color(KyUI.Ref.red(selectedRGB), KyUI.Ref.green(selectedRGB), KyUI.Ref.blue(selectedRGB), alphav);
    selectedHSB=KyUI.Ref.color(KyUI.Ref.red(selectedHSB), KyUI.Ref.green(selectedHSB), KyUI.Ref.blue(selectedHSB), alphav);
  }
  void updateColorRGB() {
    if (red == null) return;
    red.setText("" + (int)KyUI.Ref.red(selectedRGB));
    green.setText("" + (int)KyUI.Ref.green(selectedRGB));
    blue.setText("" + (int)KyUI.Ref.blue(selectedRGB));
  }
  void updateColorHSB() {
    if (hue == null) return;
    hue.setText("" + (int)KyUI.Ref.red(selectedHSB));
    saturation.setText("" + (int)KyUI.Ref.green(selectedHSB));
    brightness.setText("" + (int)KyUI.Ref.blue(selectedHSB));
  }
  //void updateColorA(){ } - no need.
  private void createColorImage() {
    float width=Math.min((pos.right - pos.left), (pos.bottom - pos.top));
    if (width <= 0) return;
    colorImage=KyUI.Ref.createGraphics((int)width, (int)width);
    colorImage.beginDraw();
    //colorImage.background(bgColor, KyUI.Ref.alpha(bgColor));
    colorImage.ellipseMode(PApplet.RADIUS);
    colorImage.rectMode(PApplet.RADIUS);
    colorImage.translate(width / 2, width / 2);
    colorImage.noStroke();
    colorImage.fill(bgColor, KyUI.Ref.alpha(bgColor));
    colorImage.ellipse(0, 0, width * 5 / 12 + 7, width * 5 / 12 + 7);
    int a=0;
    while (a < 255) {
      colorImage.fill(Color.HSBtoRGB((float)a / 255, 1.0F, 1.0F));
      colorImage.stroke(Color.HSBtoRGB((float)a / 255, 1.0F, 1.0F));
      colorImage.arc(0, 0, width * 5 / 12, width * 5 / 12, a * (PApplet.TWO_PI / 255), (a + 1) * (PApplet.TWO_PI / 255), PApplet.PIE);
      a=a + 1;
    }
    colorImage.stroke(0);
    colorImage.strokeWeight(1);
    colorImage.noFill();
    colorImage.ellipse(0, 0, width * 5 / 12, width * 5 / 12);
    colorImage.fill(KyUI.Ref.red(bgColor), KyUI.Ref.green(bgColor), KyUI.Ref.blue(bgColor), 255);
    colorImage.ellipse(0, 0, width / 4, width / 4);
    colorImage.endDraw();
  }
}
