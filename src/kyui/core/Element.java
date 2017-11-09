package kyui.core;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
public class Element {
  protected List<Element> parents=new LinkedList<Element>();
  public List<Element> children=new ArrayList<Element>();// all of elements can be viewgroup. for example, each items of listview are element and viewgroup too..
  protected int children_max=987654321;
  //
  public Rect pos=new Rect(0, 0, 0, 0);
  public Description description;
  public int bgColor=0;
  public int margin=0;
  public int padding=0;
  //
  private String Name;//identifier.
  private boolean enabled=true;// this parameter controls object's existence.
  private boolean visible=true;// this parameter controls object rendering
  private boolean active=true;// this parameter controls object control (use inputs)
  boolean renderFlag=true;// this parameter makes calling renderlater() renders on parent's render().
  //temp vars
  protected boolean entered=false;
  public Element(String name) {
    Name=name;
    KyUI.addElement(this);
  }
  public final void addChild(Element object) {
    addChild(children.size(), object);
  }
  public final void addChild(int index, Element object) {
    if (children_max <= children.size()) {
      System.err.println("[KyUI] children.size() already reached max value.");
      return;
    }
    children.add(index, object);
    object.parents.add(this);
    setPosition(pos);
  }
  public final void removeChild(String name) {
    Element object=KyUI.get(name);
    object.parents.remove(this);
    children.remove(object);
  }
  @Override
  public boolean equals(Object other) {
    return (other instanceof Element && ((Element)other).Name.equals(Name));
  }
  public final void setPosition(Rect rect) {
    invalidate(pos);
    pos=rect;
    localLayout();
  }
  public void onLayout() {
    //update children.pos here.
    //default is recursive.
    for (Element child : children) {
      child.onLayout();
    }
  }
  protected final void localLayout() {
    onLayout();
    invalidate();
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
    KyUI.invalidate(pos);
  }
  public final void invalidate(Rect rect) {
    KyUI.invalidate(rect);
  }
  void render_(PGraphics g) {
    //g.clip(pos.left, pos.top, pos.right, pos.bottom);
    if (renderFlag) render(g);
    renderChildren(g);
    if (renderFlag) overlay(g);
    renderFlag=false;
    //g.noClip();
  }
  final void renderChildren(PGraphics g) {
    for (Element child : children) {
      if (child.isVisible() && child.isEnabled()) {
        if (renderFlag) child.renderFlag=true;
        child.render_(g);
      }
    }
  }
  public void render(PGraphics g) {//override this!
    if (bgColor != 0) {
      g.fill(bgColor);
      pos.render(g);
    }
  }
  public void overlay(PGraphics g) {//override this!
  }
  public final void renderlater() {
    invalidate();
  }
  boolean checkInvalid(Rect rect) {
    if (pos.contains(rect)) {
      if (children.size() == 0) {
        renderFlag=true;
      }
      for (Element child : children) {
        if (child.isVisible() && child.isEnabled()) {
          if (child.checkInvalid(rect)) {//child not contains rect.\
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
  final boolean mouseEvent_(MouseEvent e, int index) {
    if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
      if (!entered) {
        entered=true;
        mouseEntered();
      }
    } else {
      if (entered) {
        entered=false;
        mouseExited();
      }
    }
    boolean childrenIntercept=false;
    if ((entered || KyUI.focus == this) && !mouseEventIntercept(e)) return false;
    int index2=0;
    for (Element child : children) {
      if (child.isEnabled()) {
        if (child.isActive()) {
          if (!child.mouseEvent_(e, index2)) {
            childrenIntercept=true;
          }
        }
        index2++;
      }
    }
    if (childrenIntercept) return false;
    boolean ret=true;
    if (entered || KyUI.focus == this) ret=mouseEvent(e, index);
    if (e.getAction() == MouseEvent.PRESS) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        requestFocus();
      }
    }
    return ret;
  }
  public boolean mouseEventIntercept(MouseEvent e) {//override this!
    return true;
  }
  public boolean mouseEvent(MouseEvent e, int index) {//override this!
    return true;
  }
  public void mouseEntered() {
    invalidate();
    System.out.println(getName() + " " + KyUI.Ref.frameCount);
  }
  public void mouseExited() {
    invalidate();
  }
  //
  public final void requestFocus() {//make onRequestListener?
    if (KyUI.focus != null) KyUI.focus.renderlater();
    KyUI.focus=this;
    renderlater();
  }
  public final void releaseFocus() {
    KyUI.focus=null;
    renderlater();
  }
  public Vector2 getPreferredSize() {
    return new Vector2(pos.right - pos.left, pos.bottom - pos.top);
  }
  public final void refreshElement() {//localLayout for public...it is not so good.
    localLayout();
  }
}
