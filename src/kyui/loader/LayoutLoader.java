package kyui.loader;
import com.sun.istack.internal.Nullable;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.element.TreeGraph;
import kyui.util.Rect;
import kyui.util.TaskManager;
import processing.data.XML;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
public class LayoutLoader {
  public static void loadXML(Element root, XML xml, @Nullable TreeGraph.Node treeRoot) {
    TreeGraph treeGraph=null;
    if (treeRoot == null) {
      treeGraph=new TreeGraph("KyUI:LayoutLoader:treeGraph");
      treeRoot=treeGraph.addNode("root", root);
    }
    LinkedList<ElementAddData> queue=new LinkedList<>();
    queue.addLast(new ElementAddData(null, xml));
    TaskManager tm=new TaskManager();
    Element curRoot=root;
    while (queue.size() > 0) {
      ElementAddData cur=queue.getFirst();
      for (XML x : cur.data.getChildren()) {
        queue.addLast(new ElementAddData(cur, x));
        TreeGraph.Node treeRoot_=treeRoot;//final
        tm.addTask((Object data_) -> {
          ElementAddData data=(ElementAddData)data_;
          Element parent=root;
          TreeGraph.Node parentNode=treeRoot_;
          if (data.parent != null) {
            parent=data.parent.result;
            parentNode=data.node;
          }
          try {
            Class type=Class.forName(data.data.getLocalName());
            data.result=ElementLoader.addElement(parentNode, data.data.getString("name"), type);
            if (data.result == null) {
              throw new Exception();
            } else {
              String[] attrs=data.data.listAttributes();
              for (String attrName : attrs) {
                //ADD>>load attr by attrName, into data.result.
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.err.println("no type found : " + data.data.getLocalName());
            System.exit(1);
          }
        }, queue.getLast());
      }
    }
    tm.executeAll();
    if (treeGraph != null) {
      KyUI.removeElement(treeGraph.getName());
      treeGraph.getRoot().delete();
    }
  }
  static class ElementAddData {
    //from these,
    ElementAddData parent;//if parent==null, parent equals root.
    XML data;
    //makes this
    Element result;//produce this value with Task...
    TreeGraph.Node node;//used to connect elements.
    //and use these to do next step.
    public ElementAddData(@Nullable ElementAddData parent_, XML data_) {
      parent=parent_;
      data=data_;
    }
  }
/*
<Data>//xml
  <kyui.element.Button
    bgColor=0xFF323232(10진)>
  </>
</Data>
 */
}
