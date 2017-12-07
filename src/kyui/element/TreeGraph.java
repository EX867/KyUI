package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import kyui.event.TreeNodeAction;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Stack;
public class TreeGraph<Content extends TreeNodeAction> extends Element {//includes scroll. for now, I only need horizontal one.
  Node root;
  ArrayList<Node<Content>> nodes;
  EventListener onSelectListener;
  Node selection;
  //modifiable values
  public int selectionColor;
  public int strokeWidth=6;
  public int linkWidth=6;
  public float intervalX=20;
  public float intervalY=20;
  public float scaleMin=0.3F;
  public float scaleMax=2.0F;
  public int textSize=15;
  //
  protected float offsetX;
  protected float offsetY;
  protected float scale=1.0F;
  //temp vars
  int count=0;
  int maxDepth=0;
  private float clickOffsetX=0;
  private float clickOffsetY=0;
  private float childrenSizeX=0;
  private float childrenSizeY=0;
  private float clickScrollMaxSq=0;
  float selectionOffsetX=0;
  float selectionOffsetY=0;
  boolean selectionControl;
  Rect defaultRootPos=new Rect(-60, -30, 60, 30);
  boolean dragged=false;
  public TreeGraph(String name) {
    super(name);
    init("[root]");
  }
  public TreeGraph(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init("[root]");
  }
  public TreeGraph(String name, String rootText) {
    super(name);
    init(rootText);
  }
  public TreeGraph(String name, Rect pos_, String rootText) {
    super(name);
    pos=pos_;
    init(rootText);
  }
  private void init(String rootText) {
    clipping=true;
    nodes=new ArrayList<Node<Content>>();
    selection=null;
    root=new Node(getName() + ":root", 0);
    root.pos=(defaultRootPos.clone());
    root.text=rootText;
    nodes.add(root);
    addChild(root);
    bgColor=KyUI.Ref.color(127);
    selectionColor=0;//KyUI.Ref.color(0, 0, 127);
  }
  public Node addNode(String text, Content content) {
    return root.addNode(text, content);
    //no layout!
  }
  public Node get(int index) {
    return root.get(index);
  }
  public Node getRoot() {
    return root;
  }
  public void setSelectListener(EventListener e) {
    onSelectListener=e;
  }
  @Override
  public void onLayout() {
    maxDepth=0;
    for (Node n : nodes) {//first calculate depth.
      if (maxDepth < n.depth) {
        maxDepth=n.depth;
      }
      n.textSize=(int)(scale * textSize);
      n.index=0;
    }
    float centerX=(pos.left + pos.right) / 2 - offsetX;
    float centerY=(pos.top + pos.bottom) / 2 - offsetY;
    float width=(defaultRootPos.right - defaultRootPos.left);
    float height=(defaultRootPos.bottom - defaultRootPos.top);
    Stack<Node<Content>> stack=new Stack<Node<Content>>();
    stack.push(root);
    root.pos.set((-width / 2) * scale + centerX, (-height / 2) * scale + centerY, (width / 2) * scale + centerX, (height / 2) * scale + centerY);
    float top=0;
    while (!stack.isEmpty()) {
      while (stack.peek().index < stack.peek().localNodes.size()) {
        Node n=stack.peek().localNodes.get(stack.peek().index);
        stack.peek().index++;
        stack.push(n);
        float cx=(width + intervalX) * n.depth;
        n.pos.set((cx - width / 2) * scale + centerX, (top - height / 2) * scale + centerY, (cx + width / 2) * scale + centerX, (top + height / 2) * scale + centerY);//not setPosition because will layout other elements later.
        if (n.related) {
          n.pos.set(n.pos.left + selectionOffsetX, n.pos.top + selectionOffsetY, n.pos.right + selectionOffsetX, n.pos.bottom + selectionOffsetY);
        }
      }
      top+=(height + intervalY);
      while (stack.size() > 0 && stack.peek().index >= stack.peek().localNodes.size()) {
        stack.pop();
      }
    }
    childrenSizeX=maxDepth * (width + intervalX) * scale;
    childrenSizeY=(top - (height + intervalY)) * scale;
    root.onLayout();//this will layout elements except nodes.
    setOffset(offsetX, offsetY);//range check
    invalidate();
  }
  public void setNodeSize(Rect rect) {
    root.pos.set(rect.left, rect.top, rect.right, rect.bottom);
    onLayout();
  }
  //offset 0 is root in center.
  public void setOffset(float valueX, float valueY) {
    float size=pos.right - pos.left;
    offsetX=valueX;
    offsetY=valueY;
    if (offsetX > childrenSizeX) {
      offsetX=childrenSizeX;// - size;
    }
    if (offsetX < 0) {
      offsetX=0;
    }
    if (offsetY > childrenSizeY) {
      offsetY=childrenSizeY;
    }
    if (offsetY < 0) {
      offsetY=0;
    }
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.RELEASE) {
      if (!dragged && selection != null) {
        selection.unselect();
        return false;
      }
    }
    return true;
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {//from LinearLayout.
    if (e.getAction() == MouseEvent.PRESS) {
      clickOffsetX=offsetX;
      clickOffsetY=offsetY;
      clickScrollMaxSq=0;
      dragged=false;
    } else if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL) {
        requestFocus();
        float valueX=(KyUI.mouseClick.x - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        float valueY=-(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        float value=valueX * valueX + valueY * valueY;
        clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
        if (selection != null) {
          selectionControl=(selection.pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y) || selectionControl);
        }
        if (selection != null && selectionControl) {
          selectionOffsetX=-valueX;//????
          selectionOffsetY=valueY;
          onLayout();
          return false;
        } else {
          setOffset(clickOffsetX + valueX, clickOffsetY - valueY);
          localLayout();
          if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
            dragged=true;
            return false;
          } else {
            offsetX=clickOffsetX;
            offsetY=clickOffsetY;
          }
        }
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        if (selection != null) {
          selectionOffsetX=0;
          selectionOffsetY=0;
          selectionControl=selection.pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y) || selectionControl;
          Node n=getNodeOver(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y, selection);
          if (selectionControl && n != null) {
            if ((!(selection.content == null && n.content == null)) && (selection.content == null || selection.content.checkNodeToAction(n)) && (n.content == null || n.content.checkNodeAction(selection))) {
              Node s=selection;
              selection.unselect();
              s.parent.removeNode(s);
              Node result=n.addNode(s);
              if (result != null && result != n) {
                s.setDepth(n.depth + 1);
              }
              onLayout();
              invalidate();
              selectionControl=false;
              return false;
            }
          }
          onLayout();
          invalidate();
          selectionControl=false;
        } else if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
          return false;
        }
      }
    } else if (e.getAction() == MouseEvent.WHEEL) {
      if (pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
        float centerX=(pos.left + pos.right) / 2;
        float centerY=(pos.top + pos.bottom) / 2;
        float valueX=(centerX - KyUI.mouseGlobal.x) * KyUI.scaleGlobal;
        float valueY=(centerY - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        offsetX+=valueX * scale;
        offsetY+=valueY * scale;
        scale-=(float)e.getCount() * 5 / 100;//only real scale on pointercount 2.
        if (scale < scaleMin) {
          scale=scaleMin;
        }
        if (scale > scaleMax) {
          scale=scaleMax;
        }
        offsetX-=valueX * scale;
        offsetY-=valueY * scale;
        setOffset(offsetX, offsetY);
        onLayout();
        invalidate();
        return false;
      }
    }
    return true;
  }
  public Node<Content> getNodeOver(float x, float y) {
    for (Node n : nodes) {
      if (n.pos.contains(x, y)) {
        return n;
      }
    }
    return null;
  }
  Node<Content> getNodeOver(float x, float y, Node<Content> exclude) {
    for (Node n : nodes) {
      if (exclude != n && n.pos.contains(x, y)) {
        return n;
      }
    }
    return null;
  }
  @Override
  public void editorAdd(Element e) {
    root.editorAdd(e);
  }
  @Override
  public void editorRemove(String name) {
    root.editorRemove(name);
  }
  @Override
  public boolean editorCheck(Element e) {
    return root.editorCheck(e);
  }
  //===Node===//
  public static class Node<Content extends TreeNodeAction> extends Button {
    TreeGraph Ref;
    Node parent;
    ArrayList<Node> localNodes;//because children not only includes nodes. added node using addNode() puts new node into this.
    int depth;
    boolean related=false;//if related, selection's position will be added when layout.
    int index;//temp var when used in layout.
    public boolean selected=false;
    //modifiable values
    public Content content;//!!!
    private Node(String name) {
      super(name);
      depth=-1;//no depth.(still, this node is not connected to any node. use by reflection only. (and this will call addedTo().)
      localNodes=new ArrayList<Node>();
    }
    public Node(String name, int depth_) {
      super(name);
      depth=depth_;
      localNodes=new ArrayList<Node>();
    }
    @Override
    protected void addedTo(Element e) {
      if (e instanceof TreeGraph) {
        Ref=(TreeGraph)e;
      } else if (e instanceof Node) {
        Ref=((Node)e).Ref;
      } else {
        throw new RuntimeException("[KyUI] TreeGraph : tried to add TreeGraph.Node to " + e.getClass().getTypeName());
      }
    }
    @Override
    public void editorAdd(Element e) {
      if (editorCheck(e)) {
        if (e instanceof Node) {
          addNode((Node)e);
          Ref.localLayout();
          ((Node)e).depth=depth + 1;
        } else {
          addChild(e);
          localLayout();
        }
      }
    }
    @Override
    public void editorRemove(String name) {
      Element e=KyUI.get(name);
      if (e instanceof Node) {
        removeNode((Node)e);
      } else {
        removeChild(e);
      }
    }
    @Override
    public boolean editorCheckTo(Element e) {
      return e instanceof TreeGraph || e instanceof Node;
    }
    public Node addNode(String text, Content content_) {
      Node n=new Node(Ref.getName() + ":" + Ref.count, depth + 1);
      Ref.count++;
      n.text=text;
      n.content=content_;
      return addNode(n);
    }
    protected Node addNode(Node n) {
      return addNode(localNodes.size(), n);
    }
    protected Node addNode(int index, Node n) {
      if (n == this) {
        return n;
      }
      n.parent=this;
      if ((content == null || content.checkNodeAction(n) && (n.content == null || n.content.checkNodeToAction(this)))) {
        localNodes.add(n);
        Ref.nodes.add(n);
        Ref.addChild(n);
        if (content != null) {
          content.addNodeAction(n);//!!!
        }
        return n;
      }
      return null;
    }
    public void removeNode(Node node) {
      localNodes.remove(node);
      if (content != null) {
        content.removeNodeAction(node);
      }
      Ref.removeChild(node);
      Ref.nodes.remove(node);
    }
    public void delete() {
      parent.removeNode(this);
      delete_();
    }
    public void delete_() {
      for (Node n : localNodes) {
        delete_();
      }
    }
    public Node get(int index) {
      return localNodes.get(index);
    }
    @Override
    public void render(PGraphics g) {
      super.render(g);
      g.noFill();
      if (selected) {
        g.stroke(Ref.selectionColor);
        g.strokeWeight(Ref.strokeWidth);
        pos.render(g, -Ref.strokeWidth / 2);
      }
      if (this != Ref.root) {
        g.strokeWeight(Ref.linkWidth * Ref.scale);
        g.stroke(bgColor);
        float xdist=Math.abs(pos.left - parent.pos.right); //* 3 / 4;//from kpm...
        g.bezier(pos.left, (pos.top + pos.bottom) / 2, pos.left - xdist, (pos.top + pos.bottom) / 2, parent.pos.right + xdist, (parent.pos.top + parent.pos.bottom) / 2, parent.pos.right, (parent.pos.top + parent.pos.bottom) / 2);
      }
      g.noStroke();
    }
    void select() {
      if (Ref.root == this) {
        return;
      }
      selected=true;
      select_();
      Ref.selection=this;
    }
    private void select_() {
      for (Node child : localNodes) {//recursive select.
        child.select_();
      }
      related=true;
    }
    void unselect() {
      selected=false;
      unselect_();
      Ref.selection=null;
    }
    private void unselect_() {
      for (Node child : localNodes) {//recursive select.
        child.unselect_();
      }
      related=false;
    }
    void setDepth(int depth_) {
      depth=depth_;
      for (Node child : localNodes) {
        child.setDepth(depth + 1);
      }
    }
    @Override
    public void onLayout() {
      for (Element e : children) {
        e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin, pos.bottom - e.margin));
      }
    }
    @Override
    public boolean mouseEvent(MouseEvent e, int index) {
      if (e.getAction() == MouseEvent.PRESS) {//select node, and set selection's children node to "not related"
        if (Ref.selection != null) Ref.selection.unselect();
        select();
        if (Ref.onSelectListener != null) {
          Ref.onSelectListener.onEvent(this);
        }
        return true;
      }
      return super.mouseEvent(e, index);
    }
  }
}
