package kyui.core;
import kyui.task.Task;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
public class Element {
  public List<Element> parents=new ArrayList<Element>();
  public List<Element> children=new ArrayList<Element>();// all of elements can be viewgroup. for example, each items of listview are element and viewgroup too..
  protected int children_max=987654321;
  //tasks
  static class ModifyChildrenTask implements Task {
    Element parent;
    public ModifyChildrenTask(Element parent_) {
      parent=parent_;
    }
    @Override
    public void execute(Object data_raw) {
      if (data_raw instanceof AddChildData) {
        AddChildData data=(AddChildData)data_raw;
        if (parent.children_max <= parent.children.size()) {
          System.err.println("[KyUI] children.size() already reached max value.");
          return;
        }
        KyUI.addElement(data.element);
        parent.children.add(Math.min(data.index, parent.children.size()), data.element);
        data.element.parents.add(parent);
      } else if (data_raw instanceof RemoveChildData) {
        RemoveChildData data=(RemoveChildData)data_raw;
        Element element=parent.children.get(data.index);
        element.parents.remove(this);
        parent.children.remove(data.index);
        if (element.parents.size() == 0) KyUI.removeElement(element.getName());
      } else if (data_raw instanceof ReorderChildData) {
        ReorderChildData data=(ReorderChildData)data_raw;
        Element temp=parent.children.get(data.a);
        parent.children.set(data.a, parent.children.get(data.b));
        parent.children.set(data.b, temp);
      }
      parent.localLayout();
    }
  }
  ModifyChildrenTask modifyChildrenTask=new ModifyChildrenTask(this);//task for this object.
  class AddChildData {
    int index;
    Element element;
    public AddChildData(int index_, Element element_) {
      element=element_;
      index=index_;
    }
  }
  class RemoveChildData {
    int index;
    public RemoveChildData(int index_) {
      index=index_;
    }
  }
  class ReorderChildData {
    int a;
    int b;
    public ReorderChildData(int a_, int b_) {
      a=a_;
      b=b_;
    }
  }
  //
  private String Name;//identifier.
  private boolean enabled=true;// this parameter controls object's existence.
  private boolean visible=true;// this parameter controls object rendering
  private boolean active=true;// this parameter controls object control (use inputs)
  boolean renderFlag=true;// this parameter makes calling renderlater() renders on parent's render().
  public Rect pos=new Rect(0, 0, 0, 0);
  public Description description;
  public int bgColor=0;
  public int margin=0;
  public int padding=0;
  //
  protected int startClip=0;//used in rendering or
  protected int endClip=KyUI.INF;
  //clip
  protected boolean clipping=false;
  //dnd
  public DropMessenger.Visual dropVisual;//used when creating drop messenger
  public boolean droppableStart=false;//this element can be start of drag.
  public boolean droppableEnd=false;//this element can be end of drag.
  //temp vars
  protected boolean entered=false;
  protected boolean pressedL=false;//this parameter indicates this element have been pressed left.
  protected boolean pressedR=false;//this parameter indicates this element have been pressed right.
  protected Rect clipRect;
  //control flow
  protected boolean skipRelease=false;
  //
  public Element(String name) {
    Name=name;
  }
  //children modify
  public final void addChild(Element object) {
    addChild(KyUI.INF, object);
  }
  public final void addChild(int index, Element element) {
    KyUI.taskManager.addTask(modifyChildrenTask, new AddChildData(index, element));
  }
  public final void removeChild(int index) {
    KyUI.taskManager.addTask(modifyChildrenTask, new RemoveChildData(index));
  }
  public final void removeChild(String name) {
    int index=children.indexOf(KyUI.get(name));
    if (index == -1) return;
    KyUI.taskManager.addTask(modifyChildrenTask, new RemoveChildData(index));
  }
  public final void reorderChild(int a, int b) {
    KyUI.taskManager.addTask(modifyChildrenTask, new ReorderChildData(a, b));
  }
  @Override
  public boolean equals(Object other) {
    return (other instanceof Element && ((Element)other).Name.equals(Name));
  }
  public void setPosition(Rect rect) {
    invalidate(pos);
    pos=rect;
    localLayout();
  }
  public void movePosition(float x, float y) {//this is not good ...
    pos.set(pos.left + x, pos.top + y, pos.right + x, pos.bottom + y);
    for (Element child : children) {
      child.movePosition(x, y);
    }
  }
  public void onLayout() {
    //update children.pos here.
    //default is recursive.
    for (Element child : children) {
      child.onLayout();
    }
  }
  public final void localLayout() {
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
  public final void invalidate() {//far different from invalidate(Rect rect)...
    KyUI.invalidateElement(this);
  }
  public final void invalidate(Rect rect) {
    KyUI.invalidate(rect);
  }
  void render_(PGraphics g) {
    if (clipping) {
      clipRect(g);
    }
    if (renderFlag) {
      render(g);
    }
    renderChildren(g);
    if (renderFlag) {
      overlay(g);
    }
    if (clipping) {
      removeClip(g);
    }
    renderFlag=false;
  }
  final void renderChildren(PGraphics g) {
    int end=Math.min(children.size(), endClip);
    for (int a=Math.max(0, startClip); a < end; a++) {
      Element child=children.get(a);
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
  public void clipRect(PGraphics g) {//override this!
    if (clipRect == null) {
      clipRect=new Rect();
    }
    clipRect.set(pos);
    KyUI.clipRect(g, clipRect);
  }
  public void removeClip(PGraphics g) {
    KyUI.removeClip(g);
  }
  synchronized boolean checkInvalid(Rect rect) {
    if (pos.contains(rect)) {
      if (children.size() == 0) {
        renderFlag=true;
      }
      int end=Math.min(children.size(), endClip);
      for (int a=Math.max(0, startClip); a < end; a++) {
        Element child=children.get(a);
        if (child.isVisible() && child.isEnabled()) {
          if (child.checkInvalid(rect)) {//child not contains rect.\
            renderFlag=true;
          }
        }
      }
      return false;
    }
    return true;
  }
  Element checkOverlay(float x, float y) {
    Element ret=null;
    for (Element child : children) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        Element ret_=child.checkOverlay(x, y);
        if (ret_ != null) {
          return ret_;
        }
      }
    }
    if (ret == null && (KyUI.dropEventsExternal.containsKey(getName()))) {
      ret=this;
    }
    return ret;
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
  synchronized final boolean mouseEvent_(MouseEvent e, int index, boolean trigger) {
    if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
      if (!entered) {
        entered=true;
        mouseEntered(e, index);
      }
    } else {
      if (entered) {
        entered=false;
        mouseExited(e, index);
      }
    }
    boolean ret=true;
    boolean childrenIntercept=false;
    if (trigger && (entered || KyUI.focus == this) && !mouseEventIntercept(e)) {
      ret=false;
      trigger=false;
    }
    int end=Math.min(children.size(), endClip);
    for (int a=Math.max(0, startClip); a < end; a++) {
      Element child=children.get(a);
      if (child.isEnabled() && child.isActive()) {
        if (!child.mouseEvent_(e, a, trigger)) {
          childrenIntercept=true;
        }
      }
    }
    if (childrenIntercept) {
      //System.out.println(getName() + " intercepted! " + KyUI.frameCount);
      ret=false;
      trigger=false;
    }
    if (trigger) {
      if ((entered || KyUI.focus == this)) {
        ret=mouseEvent(e, index);
      }
      if (!skipRelease && KyUI.focus == this && e.getAction() == MouseEvent.RELEASE) {
        releaseFocus();
      }
      if (e.getAction() == MouseEvent.PRESS) {
        if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
          if (e.getButton() == KyUI.Ref.LEFT) {
            pressedL=true;
          } else if (e.getButton() == KyUI.Ref.RIGHT) {
            pressedR=true;
          }
          requestFocus();
          invalidate();
        }
      }
    }
    if (e.getAction() == MouseEvent.RELEASE) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        KyUI.dropEnd(this, e, index);
        invalidate();
      }
      pressedL=false;
    }
    skipRelease=false;
    return ret;
  }
  public boolean mouseEventIntercept(MouseEvent e) {//override this!
    return true;
  }
  public boolean mouseEvent(MouseEvent e, int index) {//override this!
    return true;
  }
  public void mouseEntered(MouseEvent e, int index) {
    invalidate();
  }
  public void mouseExited(MouseEvent e, int index) {
    if (pressedL && droppableStart) {
      startDrop(e, index);
    }
    invalidate();
  }
  public void startDrop(MouseEvent e, int index) {
    KyUI.dropStart(this, e, index, "", getName());
  }
  //
  public final void requestFocus() {//make onRequestListener?
    KyUI.focus=this;
    //System.out.println("[KyUI] " + getName() + " gained focus at " + KyUI.Ref.frameCount);
  }
  public final void releaseFocus() {
    KyUI.focus=null;
    invalidate();
  }
  public Vector2 getPreferredSize() {
    return new Vector2(pos.right - pos.left, pos.bottom - pos.top);
  }
  public int size() {
    return children.size();
  }
}
