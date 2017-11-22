package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.listeners.MouseEventListener;
import kyui.util.Rect;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
public class TreeGraph extends Element {//includes scroll. for now, I only need horizontal one.
  NodeI root;
  ArrayList<NodeI> nodes;
  NodeI selection;
  //modifiable values
  public int selectionColor;
  public int strokeWidth=2;
  public float intervalX=20;
  public float intervalY=20;
  //
  protected float offsetX;
  protected float offsetY;
  //temp vars
  int count=0;
  int maxDepth=0;
  int[] depthCount;
  private float clickOffsetX=0;
  private float clickOffsetY=0;
  private float childrenSizeX=0;
  private float childrenSizeY=0;
  private float clickScrollMaxSq=0;
  float selectionOffsetX=0;
  float selectionOffsetY=0;
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
    nodes=new ArrayList<NodeI>();
    selection=null;
    root=new NodeI(getName() + ":root", 0, this);
    root.pos=(defaultRootPos);
    root.text=rootText;
    nodes.add(root);
    addChild(root);
    bgColor=KyUI.Ref.color(127);
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
    for (NodeI n : nodes) {//first calculate depth.
      if (maxDepth < n.depth) {
        maxDepth=n.depth;
      }
    }
    int maxCount=0;
    float centerX=(pos.left + pos.right) / 2;
    float centerY=(pos.top + pos.bottom) / 2;
    depthCount=new int[maxDepth + 1];
    for (NodeI n : nodes) {//then calculate count.
      n.index=depthCount[n.depth];
      depthCount[n.depth]++;
      if (maxCount < depthCount[n.depth]) {
        maxCount=depthCount[n.depth];
      }
    }
    float width=(root.pos.right - root.pos.left);
    float height=(root.pos.bottom - root.pos.top);
    for (NodeI n : nodes) {//now update position by formula...
      float top=-(depthCount[n.depth] - 1) * (height + intervalY) / 2 + n.index * (height + intervalY);
      n.pos.set((width + intervalX) * n.depth + centerX - offsetX, top + centerY - offsetY, (width + intervalX) * n.depth + width + centerX - offsetX, top + height + centerY - offsetY);//not setPosition because will layout other elements later.
    }
    childrenSizeX=(maxDepth - 1) * width;
    childrenSizeY=(maxCount - 1) * height;
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
    //KyUI.clipRect(g, pos);
    //ADD>> draw grid
  }
  @Override
  public void overlay(PGraphics g) {
    //KyUI.removeClip(g);
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
    if (offsetY > childrenSizeY / 2) {//y is different because TreeGraph is aligned LEFT,CENTER.
      offsetY=childrenSizeY / 2;
    }
    if (offsetY < -childrenSizeY / 2) {
      offsetY=-childrenSizeY / 2;
    }
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
        float valueY=(KyUI.mouseClick.y - KyUI.mouseGlobal.y) * KyUI.scaleGlobal;
        float value=valueX * valueX + valueY * valueY;
        clickScrollMaxSq=Math.max(value, clickScrollMaxSq);
        if (selection != null && pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
          selectionOffsetX=valueX;
          selectionOffsetY=valueY;
          onLayout();
          return false;
        } else {
          setOffset(clickOffsetX + valueX, clickOffsetY - valueY);
          localLayout();
        }
        if (clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
          return false;
        } else {
          offsetX=clickOffsetX;
          offsetY=clickOffsetY;
        }
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      selectionOffsetX=0;
      selectionOffsetY=0;
      if (selection != null) {
        for (NodeI n : nodes) {
          if (n != selection && n.pos.contains(KyUI.mouseGlobal.x, KyUI.mouseGlobal.y)) {
            selection.parent.removeNode(selection);
            root.addNode(selection);
            return false;
          }
        }
      } else if (pressedL && clickScrollMaxSq > KyUI.GESTURE_THRESHOLD * KyUI.GESTURE_THRESHOLD) {
        return false;
      }
    }
    return true;
  }
  public static interface Node {
    public Node addNode(String text);
    public Node get(int index);
    public void removeNode(Node node);
  }
  public class NodeI extends Button implements Node {
    TreeGraph Ref;
    NodeI parent;
    ArrayList<NodeI> localNodes;//because children not only includes nodes. added node using addNode() puts new node into this.
    int depth;
    boolean related=false;//if related, selection's position will be added when layout.
    int index;//temp var when used in layout.
    public NodeI(String name, int depth_, TreeGraph Ref_) {
      super(name);
      depth=depth_;
      Ref=Ref_;
      localNodes=new ArrayList<NodeI>();
    }
    public boolean selected=false;
    @Override
    public Node addNode(String text) {
      NodeI n=new NodeI(Ref.getName() + ":" + count, depth + 1, Ref);
      n.parent=this;
      n.text=text;
      n.setPressListener(new NodeClickListener(n));
      localNodes.add(n);
      Ref.nodes.add(n);
      Ref.addChild(n);
      count++;
      return n;
    }
    protected Node addNode(NodeI n) {//no child adding
      n.text=text;
      n.parent=this;
      n.setPressListener(new NodeClickListener(n));
      localNodes.add(n);
      Ref.nodes.add(n);
      count++;
      return n;
    }
    @Override
    public void removeNode(Node node) {
      localNodes.remove(localNodes.indexOf(node));
    }
    @Override
    public Node get(int index) {
      return localNodes.get(index);
    }
    @Override
    public void render(PGraphics g) {
      super.render(g);
      g.noFill();
      g.stroke(Ref.bgColor);//FIX>>to selectionColor
      g.strokeWeight(strokeWidth);
      if (selected) {
        pos.render(g, -strokeWidth / 2);
      }
      g.noStroke();
    }
    void select() {
      selected=true;
      select_();
      selection=this;
    }
    private void select_() {
      for (NodeI child : localNodes) {//recursive select.
        child.select_();
      }
      related=true;
    }
    void unselect() {
      selected=false;
      unselect_();
      selection=null;
    }
    private void unselect_() {
      for (NodeI child : localNodes) {//recursive select.
        child.unselect_();
      }
      related=false;
    }
    @Override
    public void onLayout() {
      for (Element e : children) {
        e.setPosition(new Rect(pos.left + e.margin, pos.top + e.margin, pos.right - e.margin, pos.bottom - e.margin));
      }
    }
    class NodeClickListener implements MouseEventListener {
      NodeI n;
      public NodeClickListener(NodeI n_) {
        n=n_;
      }
      @Override
      public boolean onEvent(MouseEvent e, int index) {//select node, and set selection's children node to "not related"
        if (selection != null) selection.unselect();
        n.select();
        return false;
      }
    }
  }
}
