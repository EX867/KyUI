package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.util.DataTransferable;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
public class KeyCatcher extends Element implements DataTransferable<KyUI.Key> {
  KyUI.Key key=new KyUI.Key(false, false, false, 0, 0);
  EventListener keyChangeEvent;
  int strokeWidth=3;
  @Attribute
  public String text="";
  @Attribute
  public int textSize=15;
  @Attribute(type=Attribute.COLOR)
  public int textColor;
  public int pKeyCount=0;
  public KeyCatcher(String name) {
    super(name);
    bgColor=KyUI.Ref.color(50);
    textColor=KyUI.Ref.color(255);
  }
  //public void setKeyChangeEvent(EventListener el) {//use datachange...instead...
  //  keyChangeEvent=el;
  //}
  @Override
  public void keyEvent(KeyEvent e) {
    if (KyUI.focus == this) {
      if (e.getAction() == KeyEvent.PRESS) {
        int keyCount=KyUI.getKeyCount();
        if (/*keyCount > 0 &&*/ (pKeyCount < keyCount)) {
          key.ctrl=KyUI.ctrlPressed;
          key.alt=KyUI.altPressed;
          key.shift=KyUI.shiftPressed;
          key.key=e.getKey();
          key.keyCode=e.getKeyCode();
          text=key.toString();
          if (keyChangeEvent != null) {
            keyChangeEvent.onEvent(this);
          }
          //System.out.println("ctrl : " + key.ctrl + ", alt : " + key.alt + ", shift : " + key.shift + " " + (int)key.key + " " + key.keyCode);
          invalidate();
        }
      }
    }
    pKeyCount=KyUI.getKeyCount();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        skipRelease=true;//because release action releases focus automatically.
        return false;
      }
    }
    return super.mouseEvent(e, index);
  }
  @Override
  public void render(PGraphics g) {
    super.render(g);
    if (KyUI.focus == this) {
      g.stroke(textColor);
      g.strokeWeight(strokeWidth);
      g.noFill();
      pos.render(g, -strokeWidth * 2);
      g.noStroke();
    }
    g.pushMatrix();
    g.translate((pos.right + pos.left) / 2, (pos.bottom + pos.top) / 2);
    g.textSize(Math.max(1, textSize));
    g.fill(textColor);
    g.text(text, 0, 0);
    g.popMatrix();
  }
  @Override
  public KyUI.Key get() {
    return key;
  }
  @Override
  public void set(KyUI.Key value) {
    key=value;
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    keyChangeEvent=event;//...
  }
}