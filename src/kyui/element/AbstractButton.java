package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class AbstractButton extends Element {
  protected boolean pressed;//this parameter indicates this element have been pressed.
  public AbstractButton(String name) {
    super(name);
  }
  @Override
  public void update() {
  }
  int dir=1;
  @Override
  public void render(PGraphics g) {
    if (dir == 1) {
      pos.left++;
      pos.right++;
    } else {
      pos.left--;
      pos.right--;
    }
    if (pos.left < 0) dir=1;
    else if (pos.right > g.width) dir=2;
    g.fill(g.color(0));
    pos.render(g);
    renderlater();//this is bad.
  }
  @Override
  public void mouseEvent(MouseEvent e) {
    //    if (e.getAction() == MouseEvent.PRESS || e.getAction() == MouseEvent.DRAG) {
    //      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) System.out.println(KyUI.Ref.frameCount);
    //    }
  }
  public void react() {//render in react. if reacted - return true
    /*if (KyUI.mouseState==KyUI.STATE_RELEASED)pressed=false;
    firstOn=firstOn+drawInterval;
    if (isMouseOn(position.x, position.y, size.x, size.y)) {
      if (entered==false) {
        //printLog("UI.react()", "UI "+name+" entered");
      if (skipRendering==false)render ();
    }
    entered=true;
    if (mouseState==AN_PRESS) {
        pressed=true;
        if (skipRendering==false)render ();
        //printLog ("UI.react ()", "UI "+name+" pressed.");
        disableDescription();
        firstOn=0;
      } else if (mouseState==AN_RELEASE) {
        if (pressed) {//disable enter!
          int tempfocus=focus;
          focus=ID;
          UI[tempfocus].render();
          //printLog("focus", ID+"");
        }
        firstPress=0;
        //printLog("UI.react()", "UI "+name+" released");
        if (skipRendering==false)render ();
      } else if (mouseState==AN_PRESSED) {
        firstPress+=drawInterval;
      }
      if (mousePressed==false&&firstOn>((TextBox)UI[getUIid("I_DESCRIPTIONTIME")]).value) {
        description.move(MouseX, MouseY);
        if (descriptionShowed==false) {
          Description_current=ID;
          Description_enabled=true;
          //Frames[currentFrame].render();
          descriptionShowed=true;
        }
      }
    } else {
      resetFocus();
    }*/
  }
  /*protected void disableDescription() {
    Description_enabled=false;
    description.movable=true;
    if (descriptionShowed) {
      Frames[currentFrame].render();
      skipRendering=true;
    }
    descriptionShowed=false;
  }
  void resetFocus() {
    if (entered==true) {
      //printLog("UI.react()", "UI "+name+" mouse out");
      if (skipRendering==false)render ();
    }
    entered=false;
    firstOn=0;
    firstPress=0;
    if (Description_current==ID) {
      disableDescription();
      Description_current=DEFAULT;
    }
  }*/
}
