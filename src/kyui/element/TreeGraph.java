package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.core.RelativeFrame;
import kyui.editor.Attribute;
import kyui.event.EventListener;
import kyui.event.TreeNodeAction;
import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Stack;
public class TreeGraph<Content extends TreeNodeAction> extends RelativeFrame {//includes scroll.
  Node root;
  ArrayList<Node<Content>> nodes;
  EventListener onSelectListener;
  public Node selection;
  //modifiable values
  @Attribute(layout=Attribute.SELF)
  public float intervalX=20;
  @Attribute(layout=Attribute.SELF)
  public float intervalY=5;
  @Attribute(type=Attribute.COLOR)
  public int selectionColor;
  @Attribute
  public int strokeWidth=6;
  @Attribute
  public int linkWidth=6;
  @Attribute
  public int textSize=15;
  //  protected float offsetX;
  //  protected float offsetY;
  //  protected float scale=1.0F;
  //temp vars
  int count=0;
  int maxDepth=0;
  //  private float clickOffsetX=0;
  //  private float clickOffsetY=0;
  private float childrenSizeX=0;
  private float childrenSizeY=0;
  private float clickScrollMaxSq=0;
  float selectionOffsetX=0;
  float selectionOffsetY=0;
  Rect defaultRootPos=new Rect(-60, -20, 60, 20);
  boolean selectionControl=false;
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
    nodes=new ArrayList<Node<Content>>();
    selection=null;
    root=new Node(getName() + ":root", 0);
    root.pos=(defaultRootPos.clone());
    root.text=rootText;
    nodes.add(root);
    addChild(root);
    bgColor=KyUI.Ref.color(127);
    selectionColor=0;//KyUI.Ref.color(0, 0, 127);
    KyUI.taskManager.executeAll();
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
      //n.textSize=(int)(scale * textSize);
      n.index=0;
    }
    float width=(defaultRootPos.right - defaultRootPos.left);
    float height=(defaultRootPos.bottom - defaultRootPos.top);
    Stack<Node<Content>> stack=new Stack<Node<Content>>();
    stack.push(root);
    root.pos.set(-width / 2, -height / 2, width / 2, height / 2);
    float top=0;
    while (!stack.isEmpty()) {
      while (stack.peek().index < stack.peek().localNodes.size()) {
        Node n=stack.peek().localNodes.get(stack.peek().index);
        stack.peek().index++;
        stack.push(n);
        float cx=(width + intervalX) * n.depth;
        n.pos.set(cx - width / 2, top - height / 2, cx + width / 2, top + height / 2);//not setPosition because will layout other elements later.
        if (n.related) {
          n.pos.set(n.pos.left + selectionOffsetX, n.pos.top + selectionOffsetY, n.pos.right + selectionOffsetX, n.pos.bottom + selectionOffsetY);
        }
      }
      top+=(height + intervalY);
      while (stack.size() > 0 && stack.peek().index >= stack.peek().localNodes.size()) {
        stack.pop();
      }
    }
    childrenSizeX=maxDepth * (width + intervalX);// * scale;
    childrenSizeY=(top - (height + intervalY));// * scale;
    root.onLayout();//this will layout elements except nodes.
    //setOffset(offsetX, offsetY);//range check
    invalidate();
  }
  public void setNodeSize(Rect rect) {
    root.pos.set(rect.left, rect.top, rect.right, rect.bottom);
    onLayout();
  }
  @Override
  public boolean mouseEventIntercept(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      selectionControl=false;
    }
    return true;
  }
  //removed setOffset range check.
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL && selectionControl && selection != null) {
        requestFocus();
        float valueX=(KyUI.mouseClick.getLast().x - KyUI.mouseGlobal.getLast().x) * KyUI.scaleGlobal / transform.scale;// / transformsAcc.getLast().scale;
        float valueY=(KyUI.mouseClick.getLast().y - KyUI.mouseGlobal.getLast().y) * KyUI.scaleGlobal / transform.scale;// / transformsAcc.getLast().scale;
        selectionOffsetX=-valueX;
        selectionOffsetY=-valueY;
        //selectionControl=true;
        onLayout();
        return false;
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      boolean ret=true;
      selectionOffsetX=0;
      selectionOffsetY=0;
      if (pressedL) {
        if (selection != null && selectionControl) {
          Node n=getNodeOver(transform.trans(transforms.getLast(), KyUI.mouseGlobal.getLast()), selection);
          if (n != null && selection != null && (!(selection.content == null && n.content == null)) && (selection.content == null || selection.content.checkNodeToAction(n)) && (n.content == null || n.content.checkNodeAction(selection))) {
            Node s=selection;
            s.unselect();
            s.parent.removeNode(s);
            Node result=n.addNode(s);
            if (result != null && result != n) {
              s.setDepth(n.depth + 1);
            }
            ret=false;
          }
          onLayout();
          invalidate();
        }
        if (!selectionControl && selection != null && !scrolled) {
          selection.unselect();
          selection=null;
          ret=false;
        }
        selectionControl=false;
        return ret;
      }
    }
    return super.mouseEvent(e, index);
  }
  public Node<Content> getNodeOverAbsolute() {
    return (Node<Content>)KyUI.checkOverlayCondition((Element e) -> {
      return e instanceof Node && ((Node)e).Ref == this;
    });
  }
  Node<Content> getNodeOver(Vector2 point, Node<Content> exclude) {
    for (Node n : nodes) {
      if (exclude != n && n.pos.contains(point.x, point.y)) {
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
      } else {
        System.err.println(e.getClass().getTypeName() + " can not added to TreeGraph.");
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
      if (Ref == null) {
        KyUI.taskManager.executeAll();
      }
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
      if (parent == null) {
        Ref.removeChild(this);
        Ref.nodes.remove(this);
      } else {
        parent.removeNode(this);
      }
      delete_();
    }
    public void delete_() {
      for (Node n : localNodes) {
        n.delete_();
        KyUI.taskManager.addTask((Object o) -> {
          removeNode(n);
        }, null);
      }
    }
    public Node get(int index) {
      return localNodes.get(index);
    }
    public ArrayList<Node> get() {
      return localNodes;
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
        g.strokeWeight(Ref.linkWidth * Ref.transform.scale);
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
        Ref.selectionControl=true;
        return true;
      } else if (e.getAction() == MouseEvent.RELEASE) {
        if (Ref.selectionControl) {
          return true;
        } else {
          return false;
        }
      }
      return super.mouseEvent(e, index);
    }
  }
}
