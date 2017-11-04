package kyui.core;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
public class Element {
  protected ArrayList<Element> parents=new ArrayList<Element>();
  protected ArrayList<Element> children=new ArrayList<Element>();// all of elements can be viewgroup. for example, each items of listview are element and viewgroup too..
  //
  public Rect pos=new Rect(0, 0, 0, 0);
  public Description description;
  //
  private String Name;//identifier.
  private boolean enabled=true;// this parameter controls object's existence.
  private boolean visible=true;// this parameter controls object rendering
  private boolean active=true;// this parameter controls object control (use inputs)
  protected boolean renderFlag=true;// this parameter makes calling renderlater() renders on parent's render().
  //temp vars
  protected boolean entered=false;
  public Element(String name) {
    Name=name;
    KyUI.addElement(this);
  }
  protected final void addChild(Element object) {
    children.add(object);
    object.parents.add(this);
    setPosition(pos);
  }
  protected final void removeChild(String name) {
    Element object=KyUI.get(name);
    object.parents.remove(this);
    children.remove(object);
  }
  @Override
  public boolean equals(Object other) {
    return (other instanceof Element && ((Element)other).Name.equals(Name));
  }
  public void setPosition(Rect rect) {
    invalidate(pos);
    pos=rect;
    invalidate();
    onLayout();
  }
  public void onLayout() {
    //update children.position.
    //default not modify child's position.
  }
  protected synchronized final void localLayout() {
    setPosition(pos);
  }
  public final String getName() {
    return Name;
  }
  //
  public void setEnabled(boolean state) {
    enabled=state;
  }
  public void setVisible(boolean state) {
    visible=state;
  }
  public void setActive(boolean state) {
    active=state;
  }
  public boolean isEnabled() {
    return enabled;
  }
  public boolean isVisible() {
    return visible;
  }
  public boolean isActive() {
    return active;
  }
  //
  public final void update_() {
    for (int a=0; a < children.size(); a++) {
      if (children.get(a).isEnabled()) children.get(a).update_();
    }
    update();
  }
  public void update() {//override this!
  }
  public final void invalidate() {
    //KyUI.invalidate(pos);
    renderFlag=true;
  }
  public final void invalidate(Rect rect) {
    KyUI.invalidate(rect);
  }
  final void render_(PGraphics g) {
    g.clip(pos.left, pos.top, pos.right, pos.bottom);
    if (renderFlag) render(g);
    for (Element child : children) {
      if (child.isVisible() && child.isEnabled()) {
        if (renderFlag) child.renderFlag=true;
        child.render_(g);
      }
    }
    if (renderFlag) overlay(g);
    renderFlag=false;
    g.noClip();
  }
  public void render(PGraphics g) {//override this!
  }
  public void overlay(PGraphics g) {//override this!
  }
  public void renderlater() {
    renderFlag=true;
  }
  final boolean checkInvalid(Rect rect) {
    if (pos.contains(rect)) {
      if (children.size() == 0) renderFlag=true;
      for (Element child : children) {
        if (child.isVisible() && child.isEnabled()) {
          if (child.checkInvalid(rect)) {//child not contains rect.
            renderFlag=true;
          }
        }
      }
      return false;
    } else {
      return true;
    }
  }
  final void keyEvent_(KeyEvent e) {
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.keyEvent_(e);
    }
    keyEvent(e);
  }
  public void keyEvent(KeyEvent e) {//override this!
    //if(e.getAction()==KeyEvent.PRESS)
  }
  final void keyTyped_(KeyEvent e) {//keyTyped is special, so handled in other method.
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.keyTyped_(e);
    }
    keyTyped(e);
  }
  public void keyTyped(KeyEvent e) {//override this!
    //do not use e.getAction() in here! (incorrect)
  }
  final void mouseEvent_(MouseEvent e) {
    if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
      if (!entered) {
        mouseEntered();
      }
      entered=true;
    } else {
      if (entered) {
        mouseExited();
      }
      entered=false;
      return;
    }
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.mouseEvent_(e);
    }
    mouseEvent(e);
  }
  public void mouseEvent(MouseEvent e) {//override this!
  }
  public void mouseEntered() {
  }
  public void mouseExited() {
  }
  //
  public final void requestFocus() {//make onRequestListener?
    if (KyUI.focus != null) KyUI.focus.renderFlag=true;
    KyUI.focus=this;
    renderFlag=true;
  }
  public final void releaseFocus() {
    KyUI.focus=null;
    renderFlag=true;
  }
}
