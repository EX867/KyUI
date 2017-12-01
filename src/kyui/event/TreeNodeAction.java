package kyui.event;
import kyui.element.TreeGraph;
public interface TreeNodeAction {
  boolean checkNodeAction(TreeGraph.Node n);
  void addNodeAction(TreeGraph.Node n);// no index!!
  void removeNodeAction(TreeGraph.Node n);
}
