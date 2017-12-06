package kyui.event;
import kyui.element.TreeGraph;
public interface TreeNodeAction {
  //other node is trying to do...
  boolean checkNodeAction(TreeGraph.Node n);
  void addNodeAction(TreeGraph.Node n);// no index!!
  void removeNodeAction(TreeGraph.Node n);
  //this node is trying to do action on other node...
  boolean checkNodeToAction(TreeGraph.Node n);
}
