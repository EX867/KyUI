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
  private Rect pos;
  public Description description;
  //
  private String Name;//identifier.
  private boolean enabled;// this parameter controls object's existence.
  private boolean visible;// this parameter controls object rendering
  private boolean active;// this parameter controls object control (use inputs)
  protected boolean renderFlag=false;// this parameter makes calling renderlater() renders on parent's render().
  public Element(String name) {
    Name=name;
    KyUI.addElement(this);
    setPosition(new Rect(0,0,0,0));
  }
  public final void addChild(Element object) {
    children.add(object);
    object.parents.add(this);
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
  //그냥 있는거임!!
  public void setPosition(Rect rect) {
    KyUI.invalidate(pos);
    pos=rect;
    KyUI.invalidate(pos);
  }
  public Rect getPosition(){
    return pos;
  }
  public final String getName() {
    return Name;
  }
  //
  public final void setEnabled(boolean state) {
    enabled=state;
  }
  public final void setVisible(boolean state) {
    visible=state;
  }
  public final void setActive(boolean state) {
    active=state;
  }
  public final boolean isEnabled() {
    return enabled;
  }
  public final boolean isVisible() {
    return visible;
  }
  public final boolean isActive() {
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
  final void render_(PGraphics g) {
    if(renderFlag)return;//don't need to render.
    renderFlag=false;
    for (Element child : children) {
      if (child.isVisible() && child.isEnabled()) child.render_(g);
    }
    render(g);
  }
  protected void render(PGraphics g) {//override this!
  }
  final void keyEvent_(KeyEvent e) {
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.keyEvent_(e);
    }
    keyEvent(e);
  }
  protected void keyEvent(KeyEvent e) {//override this!
    //if(e.getAction()==KeyEvent.PRESS)
  }
  final void keyTyped_(KeyEvent e) {//keyTyped is special, so handled in other method.
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.keyTyped_(e);
    }
    keyTyped(e);
  }
  protected void keyTyped(KeyEvent e) {//override this!
    //do not use e.getAction() in here! (incorrect)
  }
  final void mouseEvent_(MouseEvent e) {
    for (Element child : children) {
      if (child.isActive() && child.isEnabled()) child.mouseEvent_(e);
    }
    mouseEvent(e);
  }
  protected void mouseEvent(MouseEvent e) {//override this!
  }
}
