package kyui.loader;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.element.TreeGraph;
import kyui.util.Rect;
import kyui.util.TaskManager;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.XML;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class LayoutLoader {
  public static void loadXML(Element root, XML xml) {
    loadXML(root, xml, null);
  }
  public static void loadXML(Element root, XML xml, @Nullable TreeGraph.Node treeRoot) {
    if (xml == null) {
      System.err.println("input xml is null!");
      return;
    }
    TreeGraph treeGraph=null;
    if (treeRoot == null) {
      treeGraph=new TreeGraph("KyUI:LayoutLoader:treeGraph");
      treeRoot=treeGraph.getRoot();
      treeGraph.getRoot().content=root;
    }
    LinkedList<ElementAddData> queue=new LinkedList<>();
    ElementAddData addData=new ElementAddData(null, xml);
    addData.result=root;
    addData.node=treeRoot;
    queue.addLast(addData);
    TaskManager tm=new TaskManager();
    Element curRoot=root;
    while (queue.size() > 0) {
      ElementAddData cur=queue.getFirst();
      XML[] ch=cur.data.getChildren();
      for (XML x : ch) {
        ElementAddData data=new ElementAddData(cur, x);
        queue.addLast(data);
        TreeGraph.Node treeRoot_=treeRoot;//final
        //
        System.out.println("data : " + data.data.getName());
        if (data.result != null) {
          System.out.println("result : " + data.result.getName());
          continue;
        }
        if (data.data == null) {
          System.out.println("data.data is null");
        }
        //        if (!(data.data.hasAttribute("name"))) {FIX>>nullpointer
        //          System.err.println("[KyUI] data.data has no attribute named \"name\".");
        //          return;
        //        }
        Element parent=data.parent.result;
        TreeGraph.Node parentNode=data.node;
        try {
          Class type=Class.forName(data.data.getName());
          data.node=ElementLoader.addElement(parentNode, data.data.getString("name"), type);
          data.result=(Element)data.node.content;
          if (data.result == null) {
            System.err.println("[KyUI] data.result is null! element instantiation failed. : " + data.data.getString("name"));
            continue;
          } else {
            String[] attrs=data.data.listAttributes();
            ElementLoader.AttributeSet set=ElementLoader.attributes.get(data.result.getClass());
            for (String attrName : attrs) {
              Attribute.Editor e=set.getAttribute(attrName);
              e.setField(data.result, castFromString(e.field.getType(), data.data.getString(attrName)));
            }
          }
        } catch (ClassNotFoundException e) {
          System.err.println("no type found : " + data.data.getName());
          continue;
        }
      }
      queue.removeFirst();
    }
    if (treeGraph != null) {
      KyUI.removeElement(treeGraph.getName());
      treeGraph.getRoot().delete();
    }
  }
  static Object castFromString(Class c, String source) {//source
    //reverse function of toString() in some classes.
    if (c == int.class || c == Integer.class) {
      return Integer.parseInt(source);
    } else if (c == float.class || c == Float.class) {
      return Float.parseFloat(source);
    } else if (c == String.class) {
      return source;
    } else if (c == boolean.class || c == Boolean.class) {
      if (source.equals("true")) {
        return true;
      } else {
        return false;
      }
    } else if (c == Rect.class) {//Format = [(x1, y1)~(x2, y2)]
      Pattern pt=Pattern.compile("(-?\\d*\\.?\\d*)");
      Matcher mc=pt.matcher(source);
      return new Rect(Float.parseFloat(mc.group(0)), Float.parseFloat(mc.group(1)), Float.parseFloat(mc.group(2)), Float.parseFloat(mc.group(3)));
    } else if (c == PImage.class) {//Format = path
      return KyUI.Ref.loadImage(source);
    } else if (c == PFont.class) {//Format = path
      return KyUI.Ref.createFont(source, 20);
    } else if (c.isEnum()) {
      Object[] constants=c.getEnumConstants();
      for (Object e : constants) {
        if (((Enum)e).name().equals(source)) {
          return e;
        }
      }
    }
    return null;//NullPointerException!
  }
  static class ElementAddData {
    //from these,
    ElementAddData parent;//if parent==null, parent equals root.
    XML data;
    //makes this
    Element result;//produce this value with Task...
    TreeGraph.Node node;//used to connect elements.
    //and use these to do next step.
    public ElementAddData(@Nullable ElementAddData parent_, @NotNull XML data_) {
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
