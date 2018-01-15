package kyui.loader;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.editor.InspectorButton1;
import kyui.editor.InspectorColorVarButton;
import kyui.element.LinearList;
import kyui.element.TreeGraph;
import kyui.util.Rect;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.XML;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class LayoutLoader {
  static LinearList colorsList;//ElementLoader.variableList???!!?!
  public static XML saveXML(@Nullable XML startXml, TreeGraph.Node rootNode) {//export including root. root is not loaded in loadXML.
    if (startXml == null) {
      startXml=new XML("Data");
    } else {
      if (startXml.getChildren("layout").length != 0) {
        XML[] olds=startXml.getChildren("layout");
        for (XML old : olds) {
          startXml.removeChild(old);
        }
      }
      if (startXml.getChildren("color").length != 0) {
        XML[] olds=startXml.getChildren("color");
        for (XML old : olds) {
          startXml.removeChild(old);
        }
      }
    }
    XML layout=startXml.addChild("layout");
    XML color=startXml.addChild("color");
    for (LinearList.SelectableButton v : ElementLoader.variableList) {//use text...
      if (!v.text.equals("NONE")) {
        XML colorChild=color.addChild(v.text);
        colorChild.setContent(Long.toString((long)ElementLoader.vars.get(v.text).value));
      }
    }
    LinkedList<ElementSaveData> queue=new LinkedList<>();
    ElementSaveData root=new ElementSaveData(null, layout, rootNode);
    queue.add(root);
    while (queue.size() > 0) {
      ElementSaveData cur=queue.getFirst();
      //
      //create and set xml using node.content
      XML xml=layout;//added item will store in here
      if (cur.parent != null) {//not root
        try {
          XML base=cur.xml;
          Class type=cur.node.content.getClass();
          xml=base.addChild(new XML(type.getName()));
          //System.out.println("add " + xml.getName() + " to " + base.getName());
          ElementLoader.AttributeSet set=ElementLoader.attributes.get(cur.node.content.getClass());
          java.util.List<Attribute.Editor> attrs=set.attrs;
          for (Attribute.Editor attr : attrs) {
            Element e=(Element)cur.node.content;
            if ((attr.field.getType() == Integer.class || attr.field.getType() == int.class) && attr.attr.type() == Attribute.COLOR) {
              InspectorColorVarButton.ColorVariable v=ElementLoader.varsReverse.get(new InspectorColorVarButton.Reference(attr, (Element)cur.node.content));
              if (v == null || v.name.equals("NONE")) {
                xml.setString(attr.field.getName(), attr.getField(e).toString());
              } else {
                xml.setString(attr.field.getName(), v.name);
              }
            } else {
              xml.setString(attr.field.getName(), attr.getField(e).toString());
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      //add children
      ArrayList<TreeGraph.Node> children=cur.node.get();
      for (TreeGraph.Node n : children) {
        //System.out.println(cur.node.getName() + " has child " + n.getName());
        queue.addLast(new ElementSaveData(cur, xml, n));
      }
      //
      queue.removeFirst();
    }
    return startXml;
  }
  public static void loadXML(Element root, XML xml) {
    loadXML(root, xml, null, null);
  }
  public static void loadXML(Element root, @NotNull XML xml, @Nullable TreeGraph.Node treeRoot, @Nullable LinearList colorsList_) {
    if (xml.getChild("layout") == null) {
      KyUI.err("LayoutLoader - failed to load layout xml : xml has no layout section.");
      return;
    }
    if (colorsList_ == null && colorsList == null) {
      colorsList=new LinearList("KyUI:LayoutLoader:colorsList");
    } else if (colorsList_ == null) {
      colorsList_=colorsList;
    } else {
      colorsList=colorsList_;
    }
    XML color=xml.getChild("color");//if null, all variable name colors will set to default.
    xml=xml.getChild("layout");
    if (color != null) {//load color.
      XML[] colors=color.getChildren();
      for (XML item : colors) {
        if (!item.getName().equals("#text")) {
          if (InspectorColorVarButton.addVar(item.getName(), colorsList)) {
            KyUI.taskManager.executeAll();
            int val=(int)castFromString(int.class, item.getContent());
            ElementLoader.vars.get(item.getName()).value=val;
            if (colorsList != null) {
              java.util.List<Element> list=colorsList.getItems();
              for (Element el : list) {
                if (((LinearList.SelectableButton)el).text.equals(item.getName())) {
                  ((InspectorButton1)el).set(val);
                }
              }
            }
          } else {
            //not overwrite existing value!!!
          }
        }
      }
    }
    KyUI.log("LayoutLoader - load xml to  : " + root.getName());
    TreeGraph treeGraph=null;//saving in non-editor is unnessesary.
    if (treeRoot == null) {//(if TreeGraph is null, this is not an editor.)
      treeGraph=new TreeGraph("KyUI:LayoutLoader:treeGraph");
      treeRoot=treeGraph.getRoot();
      treeGraph.getRoot().content=root;
    }
    //set first state of queue
    LinkedList<ElementLoadData> queue=new LinkedList<>();
    ElementLoadData rootData=new ElementLoadData(null, xml, treeRoot);
    rootData.result=root;
    queue.addLast(rootData);
    while (queue.size() > 0) {
      ElementLoadData cur=queue.getFirst();
      //create and set result element using xml
      TreeGraph.Node node=treeRoot;//default
      if (cur.result == null) {
        String name="";
        if (!cur.xml.hasAttribute("name")) {
          name=System.nanoTime() + "";//use nanotime because two loops can be fater than 1ms!
        } else {
          name=cur.xml.getString("name");
        }
        //        try {
        //Class type=Class.forName(cur.xml.getName());
        Class type=ElementLoader.classes.get(cur.xml.getName());
        node=ElementLoader.addElement(cur.node, name, type);
        cur.result=(Element)node.content;
        if (cur.result == null) {
          KyUI.err("xml.result is null! element instantiation failed. : " + name);
          queue.removeFirst();
          continue;
        } else {
          String[] attrs=cur.xml.listAttributes();
          ElementLoader.AttributeSet set=ElementLoader.attributes.get(cur.result.getClass());
          for (String attrName : attrs) {
            if (!attrName.equals("name") && cur.xml.hasAttribute(attrName)) {
              Attribute.Editor e=set.getAttribute(attrName);
              if ((e.field.getType() == Integer.class || e.field.getType() == int.class) && e.attr.type() == Attribute.COLOR && color != null) {
                InspectorColorVarButton.ColorVariable v=ElementLoader.vars.get(cur.xml.getString(attrName));
                if (v != null) {
                  e.setField(cur.result, v.value);
                  v.addReference(e, cur.result);
                } else {
                  e.setField(cur.result, castFromString(e.field.getType(), cur.xml.getString(attrName)));
                }
              } else {
                String source=cur.xml.getString(attrName);
                Object val=castFromString(e.field.getType(), source);
                e.setField(cur.result, val);
              }
            }
          }
        }
        //        } catch (ClassNotFoundException ce) {
        //          ce.printStackTrace();
        //          System.err.println("no type found : " + cur.xml.getName());
        //          queue.removeFirst();
        //          continue;
        //        }
      }
      //add children
      XML[] children=cur.xml.getChildren();
      for (XML x : children) {
        //System.out.println(cur.xml.getName() + " has child " + x.getName());
        if (!x.getName().equals("#text")) {
          queue.addLast(new ElementLoadData(cur, x, node));
        }
      }
      //remove item
      queue.removeFirst();
    }
    if (treeGraph != null) {
      KyUI.removeElement(treeGraph.getName());
      treeGraph.getRoot().delete();
    }
  }
  static Object castFromString(Class c, String source) {
    //reverse function of toString() in some classes.
    if (c == int.class || c == Integer.class) {
      long l=Long.decode(source);//0xFFFFFFFF is -1. Integer.decode makes error.
      return (int)l;//make overflow!
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
      String fl="([+-]?([0-9]*[.])?[0-9]+)";
      Pattern pt=Pattern.compile(fl);
      Matcher mc=pt.matcher(source.replaceAll(" ", ""));
      LinkedList<String> list=new LinkedList<>();
      while (mc.find()) {
        list.add(mc.group());
      }
      return new Rect(Float.parseFloat(list.get(0)), Float.parseFloat(list.get(1)), Float.parseFloat(list.get(2)), Float.parseFloat(list.get(3)));
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
  static class ElementLoadData {
    //from these,
    ElementLoadData parent;//if parent==null, parent equals root.
    XML xml;
    TreeGraph.Node node;/// rsult will added to this.
    //makes this
    Element result;//produce this value with Task...
    //and use these to do next step.
    public ElementLoadData(@Nullable ElementLoadData parent_, @NotNull XML data_, TreeGraph.Node node_) {
      parent=parent_;
      xml=data_;
      node=node_;
    }
  }
  static class ElementSaveData {
    ElementSaveData parent;
    XML xml;//xml to add current element.
    TreeGraph.Node node;
    public ElementSaveData(@Nullable ElementSaveData parent_, @NotNull XML data_, TreeGraph.Node node_) {
      parent=parent_;
      xml=data_;
      node=node_;
    }
  }
/*
<Data>
  <layout>//xml
    <kyui.element.Button
      bgColor=0xFF323232(10)>
    </>
  </layout>
  <color>
    <VarName>0xFF323232</VarName>
  </color>
  <shortcut>
    <...>
  </shortcut>
</Data>
 */
}
