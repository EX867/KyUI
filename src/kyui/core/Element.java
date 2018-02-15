package kyui.core;
import kyui.editor.Attribute;
import kyui.element.TreeGraph;
import kyui.event.ExtendedRenderer;
import kyui.event.TreeNodeAction;
import kyui.util.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
public class Element implements TreeNodeAction {
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
          KyUI.err("children.size() already reached max valueI.");
          //return;//some classes can behave strange, not refuse them only put error.
        }
        if (!data.element.name.isEmpty()) {
          KyUI.addElement(data.element);
        }
        parent.children.add(Math.min(data.index, parent.children.size()), data.element);
        data.element.parents.add(parent);
        data.element.addedTo(parent);//...bad thing one more...
      } else if (data_raw instanceof RemoveChildData) {
        RemoveChildData data=(RemoveChildData)data_raw;
        int index=parent.children.indexOf(data.element);
        if (index < 0) {
          return;
        }
        Element element=parent.children.get(index);
        element.parents.remove(parent);
        parent.children.remove(index);
        if (!element.name.isEmpty()) {
          if (element.parents.size() == 0) KyUI.removeElement(element.getName());
        }
      } else if (data_raw instanceof ReorderChildData) {
        ReorderChildData data=(ReorderChildData)data_raw;
        if (data.b > data.a) {
          data.b--;
        }
        if (data.a < 0 || data.b < 0 || data.a >= parent.children.size() || data.b >= parent.children.size()) {
          return;
        }
        //remove a and insert b
        Element e=parent.children.get(data.a);
        parent.children.remove(data.a);
        parent.children.add(data.b, e);
      } else if (data_raw == null) {
        parent.onLayout();
        parent.invalidate();
      }
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
    Element element;
    public RemoveChildData(Element element_) {
      element=element_;
    }
  }
  class ReorderChildData {
    int a;
    int b;
    public ReorderChildData(int a_, int b_) {
      a=a_;
      b=b_;
    }
  }//reorder is not nessessary to put to task...
  //
  @Attribute(setter="setName", getter="getName", type=Attribute.NAME)
  String name;//identifier.
  //attributes
  @Attribute(setter="setEnabled", getter="isEnabled", layout=Attribute.PARENT)
  private boolean enabled=true;// this parameter controls object's existence.
  @Attribute(setter="setVisible", getter="isVisible", layout=Attribute.NONE)
  private boolean visible=true;// this parameter controls object rendering
  @Attribute(setter="setActive", getter="isActive", layout=Attribute.NONE)
  private boolean active=true;// this parameter controls object control (use inputs)
  @Attribute(setter="setPosition", layout=Attribute.NONE)//setPosition includes layout.
  public Rect pos=new Rect(0, 0, 0, 0);
  Description description;
  @Attribute(setter="setDescription", getter="getDescription")
  private String desc="";//dummy var
  @Attribute(type=Attribute.COLOR, setter="setBgColor")
  public int bgColor=0;
  @Attribute(layout=Attribute.PARENT)
  public int margin=0;
  @Attribute(layout=Attribute.SELF)
  public int padding=0;
  @Attribute
  public int overlayOnDrag=0;
  protected ExtendedRenderer dropOverlayRenderer;
  //
  protected int startClip=0;//used in rendering or
  protected int endClip=KyUI.INF;
  //clip
  protected boolean clipping=false;
  public static LinkedList<Rect> clipArea=new LinkedList<>();
  //transform
  public static LinkedList<Transform> transforms=new LinkedList<>();
  protected Transform transform=Transform.identity;
  public static LinkedList<Transform> transformsAcc=new LinkedList<>();//stack...
  protected boolean relative=false;
  static {
    transforms.addLast(Transform.identity);
    transformsAcc.addLast(Transform.identity);
  }
  //dnd
  public DropMessenger.Visual dropVisual;//used when creating drop messenger
  public boolean droppableStart=false;//this element can be start of drag.
  public boolean droppableEnd=false;//this element can be end of drag.
  //temp vars
  boolean renderFlag=true;// this parameter makes calling renderlater() renders on parent's render().
  protected boolean entered=false;
  protected boolean pressedL=false;//this parameter indicates this element have been pressed left.
  protected boolean pressedR=false;//this parameter indicates this element have been pressed right.
  protected Rect clipRect;
  //control flow
  protected boolean skipPress=false;
  protected boolean skipRelease=false;
  //
  static Transform overlayCondition_transform=Transform.identity;
  //
  public Element(String name_) {
    name=name_;
    if (!name.isEmpty()) {
      KyUI.addElement(this);
    }
  }
  //children modify
  public final void addChild(Element object) {
    addChild(KyUI.INF, object);
  }
  public final void addChild(int index, Element element) {
    KyUI.taskManager.addTask(modifyChildrenTask, new AddChildData(index, element));
  }
  public final void removeChild(int index) {
    KyUI.taskManager.addTask(modifyChildrenTask, new RemoveChildData(children.get(index)));
  }
  public final void removeChild(String name) {
    removeChild(KyUI.get(name));
  }
  public final void removeChild(Element e) {
    KyUI.taskManager.addTask(modifyChildrenTask, new RemoveChildData(e));
  }
  public final void reorderChild(int a, int b) {
    KyUI.taskManager.addTask(modifyChildrenTask, new ReorderChildData(a, b));
  }
  protected void addedTo(Element e) {//override this!
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof Element) {
      return other == this;
      //      return ((Element)other).name.equals(name);
    } else if (other instanceof String) {
      return other.equals(name);
    }
    return false;
  }
  public void setBgColor(int c) {
    bgColor=c;
  }
  public void setPosition(Rect rect) {
    //System.out.println(getName() + " moved to " + rect.toString());
    invalidate(pos);
    pos=rect;
    //pos.set(rect);
    localLayout();
  }
  public void onLayout() {
    //update children.pos here.
    //default is recursive.
    for (Element child : children) {
      child.onLayout();
    }
  }
  public final void localLayout() {
    KyUI.taskManager.addTask(modifyChildrenTask, null);
  }
  public final String getName() {
    return name;
  }
  protected final void setName(String newName) {
    KyUI.rename(this, newName);
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
  public void setDescription(String text) {
    if (text.isEmpty()) {
      if (description != null) {
        if (!name.isEmpty()) {
          KyUI.removeElement(description.getName());
        }
        description=null;
      }
    }
    if (description == null) {
      description=new Description(this, text);
    } else {
      description.text=text;
    }
  }
  public String getDescription() {
    if (description == null) {
      return "";
    }
    return description.text;
  }
  //
  public final void update_() {
    if (clipping) {
      clipArea();
    }
    if (relative) {
      transformMouse();
    }
    for (int a=0; a < children.size(); a++) {
      if (children.get(a).isEnabled()) children.get(a).update_();
    }
    update();
    if (relative) {
      transformMouseAfter();
    }
    if (clipping) {
      clipAfter();
    }
  }
  public void update() {//override this!
  }
  public final void invalidate() {//far different from invalidate(Rect rect)...
    KyUI.invalidateElement(this);
  }
  public final void invalidate(Rect rect) {
    KyUI.invalidate(rect);
  }
  //===transform===//
  final void clipRectRender(PGraphics g) {
    if (clipRect == null) {
      clipRect=new Rect();
    }
    clipArea();
    g.imageMode(PApplet.CORNERS);
    g.clip(clipArea.getLast().left, clipArea.getLast().top, clipArea.getLast().right, clipArea.getLast().bottom);
  }
  final void removeClipRender(PGraphics g) {
    g.noClip();
    clipAfter();
    if (clipArea.size() != 0) {
      g.imageMode(PApplet.CORNERS);
      g.clip(clipArea.getLast().left, clipArea.getLast().top, clipArea.getLast().right, clipArea.getLast().bottom);
    }
  }
  protected void clipArea() {//override this!
    if (clipRect == null) {
      clipRect=new Rect();
    }
    clipRect.set(pos);//change this!
    if (clipArea.size() == 0) {
      clipArea.addLast(clipRect);
    } else {
      clipArea.addLast(Rect.getIntersection(clipArea.getLast(), clipRect, new Rect()));
    }
  }
  final void clipAfter() {
    clipArea.removeLast();//must exist.
  }
  final void transformRender(PGraphics g) {
    g.pushMatrix();
    g.translate(transform.center.x, transform.center.y);
    g.scale(transform.scale);
    transformMouse();
  }
  final void transformRenderAfter(PGraphics g) {
    g.popMatrix();
    transformMouseAfter();
  }
  final void transformMouse() {
    KyUI.mouseGlobal.addLast(transform.trans(transforms.getLast(), KyUI.mouseGlobal.getLast()));
    KyUI.mouseClick.addLast(transform.trans(transforms.getLast(), KyUI.mouseClick.getLast()));
    if (clipArea.size() > 0) {
      clipArea.addLast(transform.trans(transforms.getLast(), clipArea.getLast()));//just add.
    }
    transforms.add(transform);
    transformsAcc.addLast(Transform.add(transformsAcc.getLast(), transform));
  }
  final void transformMouseAfter() {
    KyUI.mouseGlobal.removeLast();
    KyUI.mouseClick.removeLast();
    if (clipArea.size() > 0) {
      clipArea.removeLast();
    }
    transforms.removeLast();
    transformsAcc.removeLast();
  }
  public boolean contains() {
    if (clipArea.isEmpty()) {
      if (pos.contains(KyUI.mouseGlobal.getLast().x, KyUI.mouseGlobal.getLast().y)) {
        return true;
      }
    } else {
      if (Rect.getIntersection(clipArea.getLast(), pos, new Rect()).contains(KyUI.mouseGlobal.getLast().x, KyUI.mouseGlobal.getLast().y)) {
        return true;
      }
    }
    return false;
  }
  //===rendering===//
  void render_(PGraphics g) {
    boolean dropEnd=KyUI.isDropEnd(this);
    renderFlag=renderFlag || dropEnd;
    if (clipping) {
      clipRectRender(g);
    }
    if (renderFlag) {
      render(g);
    }
    if (relative) {
      transformRender(g);
    }
    renderChildren(g);
    if (relative) {
      transformRenderAfter(g);
    }
    //    if (renderFlag_) {
    //      renderAfter(g);
    //    }
    if (dropEnd) {
      if (overlayOnDrag != 0) {
        g.fill(0, overlayOnDrag);
        pos.render(g);
      }
      if (dropOverlayRenderer != null) {
        dropOverlayRenderer.render(g);
      }
    }
    if (clipping) {
      removeClipRender(g);
    }
    renderFlag=false;
  }
  final void renderChildren(PGraphics g) {
    int end=Math.min(children.size(), endClip);
    for (int a=Math.max(0, startClip); a < end; a++) {
      Element child=children.get(a);
      if (child.isVisible() && child.isEnabled()) {
        if (renderFlag) {
          child.renderFlag=true;
        }
        child.render_(g);
      }
    }
  }
  public void render(PGraphics g) {//override this!
    if (bgColor != 0) {
      fill(g, bgColor);
      pos.render(g);
    }
  }
  public static void fill(PGraphics g, int c) {//overloading has many problem...
    ColorExt.fill(g, c);
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
  public Element checkOverlayCondition(Rect bounds, Vector2 position, Transform last, BiFunction<Element, Vector2, Boolean> cond) {
    bounds=Rect.getIntersection(bounds, pos, new Rect());
    if (!bounds.contains(position.x, position.y)) {
      return null;
    }
    Vector2 ppos=position;
    if (relative) {
      bounds=transform.trans(last, bounds);
      position=transform.trans(last, position);
      last=transform;
    }
    for (Element child : children) {
      if (child.isEnabled()) {
        Element ret_=child.checkOverlayCondition(bounds, position, last, cond);
        if (ret_ != null) {
          return ret_;
        }
      }
    }
    if (cond.apply(this, ppos)) {
      overlayCondition_transform=last;
      return this;
    }
    return null;
  }
  public void runRecursively(Consumer<Element> r) {
    r.accept(this);
    for (Element child : children) {
      child.runRecursively(r);
    }
  }
  synchronized boolean checkInvalid(Rect rect, Rect bounds, Transform last) {
    rect=Rect.getIntersection(rect, pos, new Rect());
    if (!bounds.contains(rect)) {
      return true;
    }
    if (relative) {
      bounds=transform.trans(last, bounds);
      rect=transform.trans(last, rect);
      last=transform;
    }
    for (Element child : children) {
      if (child.isEnabled() && child.isVisible()) {
        if (child.checkInvalid(rect, bounds, last)) {
          renderFlag=true;
        }
      }
    }
    return false;
  }
  boolean mouseEvent_(MouseEvent e, int index, boolean trigger) {
    boolean contains=contains();
    if (clipping) {
      clipArea();
    }
    if (contains) {
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
    if (e.getAction() == MouseEvent.PRESS) {//(2)
      if (contains) {
        if (!skipPress) {
          requestFocus();
        }
        invalidate();
      }
    }
    if (relative) {
      transformMouse();
    }
    for (int a=Math.max(0, startClip); a < Math.min(children.size(), endClip); a++) {
      Element child=children.get(a);
      if (child.isEnabled() && child.isActive()) {
        if (!child.mouseEvent_(e, a, trigger)) {
          childrenIntercept=true;
        }
      }
    }
    if (relative) {
      transformMouseAfter();
    }
    if (childrenIntercept) {
      //System.out.println(getName() + " intercepted! " + KyUI.frameCount);
      ret=false;
      trigger=false;
    }
    if (trigger) {
      if (entered && e.getAction() == MouseEvent.PRESS) {//(1)
        if (e.getButton() == KyUI.Ref.LEFT) {
          pressedL=true;
          //System.out.println(getName() + " set pressedL true " + KyUI.frameCount);
        } else if (e.getButton() == KyUI.Ref.RIGHT) {
          pressedR=true;
        }
      }
      if ((entered || KyUI.focus == this)) {
        ret=mouseEvent(e, index);
      }
      if (!skipRelease && KyUI.focus == this && e.getAction() == MouseEvent.RELEASE) {
        releaseFocus();
      }
    }
    if (e.getAction() == MouseEvent.RELEASE) {
      if (entered && enabled) {//??
        KyUI.dropEnd(this, e, index);
        if (trigger) invalidate();
      }
      if (e.getButton() == KyUI.Ref.LEFT) {
        pressedL=false;
      } else if (e.getButton() == KyUI.Ref.RIGHT) {
        pressedR=false;
      }
    }
    skipRelease=false;
    skipPress=false;
    if (clipping) {
      clipAfter();
    }
    return ret;
  }
  //
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
  //
  public final void requestFocus() {//make onRequestListener?
    KyUI.focus=this;
    //KyUI.log(" " + getName() + " gained focus at " + KyUI.Ref.frameCount);
  }
  public final void releaseFocus() {
    KyUI.focus=null;
    //KyUI.log(" " + getName() + " released focus at " + KyUI.Ref.frameCount);
    invalidate();
  }
  public Vector2 getPreferredSize() {
    return new Vector2(pos.right - pos.left, pos.bottom - pos.top);
  }
  public int size() {
    return children.size();
  }//???
  //=== Editor Action ===//
  //override these!!
  public boolean editorCheck(Element e) {
    return true;
  }
  public void editorAdd(Element e) {
    if (editorCheck(e)) {
      addChild(e);
    }
  }
  public void editorRemove(String name) {
    removeChild(name);
  }
  public boolean editorCheckTo(Element e) {
    return true;
  }
  public boolean editorIsChild(Element e) {
    return children.contains(e);
  }
  @Override
  public final boolean checkNodeAction(TreeGraph.Node n) {
    if (n.content != null && (children.size() < children_max) || editorIsChild((Element)n.content)) {//unstable...?
      return editorCheck((Element)n.content);
    }
    return false;
  }
  @Override
  public final void addNodeAction(TreeGraph.Node n) {
    if (n.content != null) {
      editorAdd((Element)(n.content));
      localLayout();
    }
  }
  @Override
  public final void removeNodeAction(TreeGraph.Node n) {
    if (n.content != null) {
      editorRemove(((Element)(n.content)).getName());
      localLayout();
    }
  }
  @Override
  public final boolean checkNodeToAction(TreeGraph.Node n) {
    if (n.content != null) {
      return editorCheckTo((Element)(n.content));
    }
    return false;
  }
  @Override
  public String toString() {
    return getName();
  }
}
