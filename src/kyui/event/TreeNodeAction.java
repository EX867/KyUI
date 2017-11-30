package kyui.event;
import kyui.element.TreeGraph;
public interface TreeNodeAction {
  boolean check(TreeGraph.Node n);
  void add(TreeGraph.Node n);// no index!!
  void remove(TreeGraph.Node n);
}
