package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Stack;
public class TreeGraph extends Element {//includes scroll. for now, I only need horizontal one.
  Node root;
  ArrayList<Node> nodes;
  Node selection;
  //modifiable values
  public int selectionColor;
  public int strokeWidth=6;
  public int linkWidth=6;
  public float intervalX=20;
  public float intervalY=20;
  public float scaleMin=0.3F;
  public float scaleMax=2.0F;
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
    nodes=new ArrayList<Node>();
    selection=null;
    root=new Node(getName() + ":root", 0, this);
    root.pos=(defaultRootPos.clone());
    root.text=rootText;
    nodes.add(root);
    addChild(root);
    bgColor=KyUI.Ref.color(127);
    selectionColor=0;//KyUI.Ref.color(0, 0, 127);
  }
  public Node addNode(String text) {
    return root.addNode(text);
    //no layout!
  }
  public Node get(int index) {
    return root.get(index);
  }
  @Override
  public void onLayout() {
    maxDepth=0;
    for (Node n : nodes) {//first calculate depth.
      if (maxDepth < n.depth) {
        maxDepth=n.depth;
      }
      n.index=0;
    }
    float centerX=(pos.left + pos.right) / 2 - offsetX;
    float centerY=(pos.top + pos.bottom) / 2 - offsetY;
    float width=(defaultRootPos.right - defaultRootPos.left);
    float height=(defaultRootPos.bottom - defaultRootPos.top);
    Stack<Node> stack=new Stack<Node>();
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
  @Override
  public void render(PGraphics g) {
    super.render(g);
    KyUI.clipRect(g, pos);
  }
  @Override
  public void overlay(PGraphics g) {
    KyUI.removeClip(g);
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
      if (selection != null) {
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
            return false;
          } else {
            offsetX=clickOffsetX;
            offsetY=clickOffsetY;
          }
        }
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (selection != null) {
        selectionOffsetX=0;
        selectionOffsetY=0;
        for (Node n : nodes) {//ADD>>check is node can move to new node.
          selectionControl=(selection.pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y) || selectionControl);
          if (n != selection && n.pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y) && selectionControl) {//move node
            Node s=selection;
            selection.unselect();
            s.parent.removeNode(s);
            if (n.addNode(s) != n) {
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
      } else if (pressedL && clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
        return false;
      }
    } else if (e.getAction() == MouseEvent.WHEEL) {
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
    }
    return true;
  }
  public static class Node extends Button {
    TreeGraph Ref;
    Node parent;
    ArrayList<Node> localNodes;//because children not only includes nodes. added node using addNode() puts new node into this.
    int depth;
    boolean related=false;//if related, selection's position will be added when layout.
    int index;//temp var when used in layout.
    public Node(String name, int depth_, TreeGraph Ref_) {
      super(name);
      depth=depth_;
      Ref=Ref_;
      localNodes=new ArrayList<Node>();
    }
    public boolean selected=false;
    public Node addNode(String text) {
      Node n=new Node(Ref.getName() + ":" + Ref.count, depth + 1, Ref);
      n.parent=this;
      n.text=text;
      localNodes.add(n);
      Ref.nodes.add(n);
      Ref.count++;
      Ref.addChild(n);
      return n;
    }
    protected Node addNode(Node n) {//no child adding and create listener
      if (n == this) {
        return n;
      }
      n.parent=this;
      localNodes.add(n);
      Ref.nodes.add(n);
      return n;
    }
    public void removeNode(Node node) {
      localNodes.remove(localNodes.indexOf(node));
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
        g.strokeWeight(Ref.linkWidth);
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
        return true;
      }
      return super.mouseEvent(e, index);
    }
  }
}
