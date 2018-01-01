package kyui.loader;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.editor.InspectorButton1;
import kyui.editor.InspectorColorVarButton;
import kyui.editor.InspectorRectButton;
import kyui.element.LinearLayout;
import kyui.element.LinearList;
import kyui.element.TextBox;
import kyui.element.ToggleButton;
import kyui.util.*;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import kyui.element.*;
import processing.event.MouseEvent;
public class ElementLoader {
  public static boolean isEditor=false;
  static ArrayList<String> loadedExternals=new ArrayList<>();
  static LinearList elementList;
  static LinearList inspectorList;
  public static ArrayList<Class<? extends Element>> types=new ArrayList<>();
  public static HashMap<Class, AttributeSet> attributes=new HashMap<>();
  static PGraphics imager;
  public static HashMap<String, InspectorColorVarButton.ColorVariable> vars=new HashMap<>(1000);
  public static HashMap<InspectorColorVarButton.Reference, InspectorColorVarButton.ColorVariable> varsReverse=new HashMap<>(1000);
  public static LinkedList<LinearList.SelectableButton> variableList=new LinkedList<>();//this is used to change picker! managed by colorVariable.
  public static void loadOnStart() {//program calling this method will probably not an editor.
    loadOnStart(new LinearList("KyUI:ElementLoader:elementList"), new LinearList("KyUI:ElementLoader:inspectorList"));
  }
  public static void loadOnStart(LinearList elementList_, LinearList inspectorList_) {
    elementList=elementList_;
    inspectorList=inspectorList_;
    if (elementList == null || inspectorList == null) {
      return;
    }
    loadInternal();
    KyUI.taskManager.executeAll();
    Collections.sort(elementList.getItems(), new Comparator<Element>() {
      @Override
      public int compare(Element o1, Element o2) {
        return ((ElementImage)o1).text.compareTo(((ElementImage)o2).text);
      }
    });
    elementList.localLayout();
    File paths=new File(getAppData() + "/externals.txt");
    if (paths.isFile()) {
      BufferedReader read=KyUI.Ref.createReader(paths.getAbsolutePath());
      try {
        String line=read.readLine();//line means one path.
        while (line != null) {
          if (line.length() != 0) {
            loadExternal(line);
          }
          line=read.readLine();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      KyUI.log("ElementLoader - create path file");
      paths.getParentFile().mkdirs();
      try {
        paths.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  static void exportExternals() {
    String externalDataPath=getAppData() + "/externals.txt";
    PrintWriter write=KyUI.Ref.createWriter(externalDataPath);
    for (String path : loadedExternals) {
      write.write(path + "\n");
    }
    write.flush();
    write.close();
    KyUI.log("ElementLoader - stored external jar data to " + externalDataPath + " successfully!");
  }
  public static void loadExternal(String path) {//https://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
    KyUI.log("ElementLoader - load start : " + path);
    if (!new File(path).isFile()) {
      KyUI.err("ElementLoader : load failed : " + path + " : file not exists!");
      return;
    }
    if (!loadedExternals.contains(path)) {
      loadedExternals.add(path);
      exportExternals();
    }
    try {
      JarFile jarFile=new JarFile(path);
      Enumeration<JarEntry> e=jarFile.entries();
      URL[] urls={new URL("jar:file:" + path + "!/")};
      URLClassLoader cl=URLClassLoader.newInstance(urls);
      while (e.hasMoreElements()) {
        JarEntry je=e.nextElement();
        KyUI.log("ElementLoader - checking... " + je.getName());
        if (je.isDirectory() || !je.getName().endsWith(".class")) {
          continue;
        }
        String className=je.getName().substring(0, je.getName().length() - 6);// -6 because of .class
        className=className.replace('/', '.').replace('\\', '.');
        try {
          Class.forName(className);
        } catch (ClassNotFoundException ee) {
          Class c=cl.loadClass(className);
          if (Element.class.isAssignableFrom(c)) {
            loadClass(c);
          } else {
            Class cc=c;
            while (cc != Object.class) {
              KyUI.log(c.getTypeName());
              cc=cc.getSuperclass();
            }
            KyUI.log("ElementLoader - " + c.getTypeName() + " is not assignable to " + Element.class.getTypeName() + ".");
          }
        }
      }
      KyUI.log("ElementLoader - " + path + " is successfully loaded!");
    } catch (Exception e) {
      e.printStackTrace();
    }
    KyUI.log("");
  }
  public static void loadInternal() {
    try {
      loadClass(Element.class);
      Reflections reflections=new Reflections("kyui.element");
      Set<Class<? extends Element>> set=reflections.getSubTypesOf(Element.class);
      for (Class c : set) {
        loadClass(c);
      }
      reflections=new Reflections("kyui.editor.inspectorItem");
      Set<Class<? extends InspectorButton>> set2=reflections.getSubTypesOf(InspectorButton.class);//????
      for (Class c : set2) {
        loadClass(c);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    KyUI.log("");
  }
  static void loadClass(Class<? extends Element> c) throws Exception {//assert no duplication
    if (!Modifier.isAbstract(c.getModifiers()) && c.getAnnotation(HideInEditor.class) == null) {
      KyUI.log("ElementLoader - loading... " + c.getTypeName());
      types.add(c);
      elementList.addItem(new ElementImage(c));
      attributes.put(c, new AttributeSet(getAttributeFields(c)));
    }
  }
  static String getAppData() {
    if (KyUI.Ref.platform == KyUI.Ref.WINDOWS) {
      return System.getenv("LOCALAPPDATA") + "/KyUIEditor";
    } else if (KyUI.Ref.platform == KyUI.Ref.LINUX) {
      return System.getProperty("user.home") + "/.local/share/KyUIEditor";
    } else {
      return System.getProperty("user.home") + "/KyUIEditor";//do not support!!
    }
  }
  public static PImage loadImageResource(String filename) {//check external paths too!
    InputStream input=Element.class.getResourceAsStream("/" + filename);
    if (input == null) {//no resources in internal directory.
      for (String path : loadedExternals) {
        try {
          JarFile jarFile=new JarFile(path);
          InputStream is=jarFile.getInputStream(jarFile.getEntry(filename));
          if (is != null) {
            return loadImage(is, filename);
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    } else {
      return loadImage(input, filename);
    }
    return null;
  }
  static PImage loadImage(InputStream input, String filename) {
    byte[] bytes=KyUI.Ref.loadBytes(input);
    if (bytes == null) {
    } else {
      KyUI.log("image " + filename + " is loaded");
      //PApplet has no loadImage function for InputStream.
      Image awtImage=(new ImageIcon(bytes)).getImage();
      PImage image=new PImage(awtImage);
      if (image.pixels != null) {//from PImage.checkAlpha().
        for (int i=0; i < image.pixels.length; ++i) {
          if ((image.pixels[i] & -16777216) != -16777216) {
            image.format=2;
            break;
          }
        }
      }
      image.parent=KyUI.Ref;
      return image;
    }
    return null;
  }
  public static class ElementImage extends LinearList.SelectableButton {
    public Class<? extends Element> element;
    PImage image;
    public ElementImage(Class<? extends Element> c) {
      super(c.getTypeName());
      element=c;
      String className=c.getTypeName();
      text=c.getSimpleName();
      if (ElementLoader.isEditor) {
        try {//recommended size of image is 120x120, max is 150x150.
          image=loadImageResource(className + ".png");
          if (image == null) {
            if (imager == null) {
              imager=KyUI.Ref.createGraphics(120, 120);
            }
            imager.beginDraw();
            imager.textFont(KyUI.fontMain);
            imager.background(127);
            imager.textSize(20);
            imager.fill(50);
            imager.textAlign(PApplet.CENTER, PApplet.CENTER);
            imager.text(c.getSimpleName(), 0, 0, 120, 120);
            imager.endDraw();
            image=imager.copy();
          }
        } catch (Exception e) {
          KyUI.log("class load failed : " + c.getTypeName());
          e.printStackTrace();
        }
      }
    }
    @Override
    protected void drawContent(PGraphics g, float overlap) {
      if (image == null) return;
      g.imageMode(KyUI.Ref.CENTER);
      g.pushMatrix();
      g.translate((pos.left + pos.right) / 2, (pos.top + pos.bottom) / 2);
      g.image(image, 0, 0);
      g.popMatrix();
    }
  }
  public static ArrayList<Attribute.Editor> getAttributeFields(Class<?> c) throws Exception {
    Class originalC=c;
    LinkedList<Attribute.Editor> fields=new LinkedList<>();
    while (c != Object.class) {
      ArrayList<Field> fieldArray=new ArrayList<Field>();
      Field[] fieldArray_=c.getDeclaredFields();
      for (Field f : fieldArray_) {
        fieldArray.add(f);
      }
      Collections.sort(fieldArray, new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
          return -o1.getName().compareTo(o2.getName());
        }//because I added reversely.
      });
      for (Field f : fieldArray) {
        f.setAccessible(true);
        Attribute a=f.getAnnotation(Attribute.class);
        if (a != null) {
          fields.addFirst(new Attribute.Editor(a, originalC, f));
        }
      }
      c=c.getSuperclass();
    }
    return new ArrayList<Attribute.Editor>(fields);
  }
  public static Method getMethod(Class<?> c, String name, Class... paramType) throws Exception {
    Class originalC=c;
    while (c != Object.class) {
      Method[] methodArray=c.getDeclaredMethods();
      for (Method m : methodArray) {
        if (m.getName().equals(name) && arrayEquals(paramType, m.getParameterTypes())) {
          return m;
        }
      }
      c=c.getSuperclass();
    }
    return null;
  }
  static boolean arrayEquals(Object[] a, Object[] b) {
    if (a.length != b.length) {
      return false;
    }
    for (int c=0; c < a.length; c++) {
      if (!a[c].equals(b[c])) {
        return false;
      }
    }
    return true;
  }
  public static class AttributeSet {
    public ArrayList<Attribute.Editor> attrs;
    public ArrayList<InspectorButton> items;
    public AttributeSet(ArrayList<Attribute.Editor> attrs_) {
      attrs=attrs_;
      items=new ArrayList<>();
      items.ensureCapacity(attrs.size());
      LinearLayout inspector=inspectorList.listLayout;
      if (isEditor) {
        for (Attribute.Editor a : attrs) {
          InspectorButton i=null;
          String name=a.c.getTypeName() + "." + a.field.getName();
          String name1=name + ":element";
          if (a.field.getType() == int.class || a.field.getType() == Integer.class) {//FIX>> optimize reuse Element class attributes.
            if (a.attr.type() == Attribute.COLOR) {
              ColorButton e=new ColorButton(name1);
              ColorButton.OpenColorPickerEvent event=new ColorButton.OpenColorPickerEvent(e);
              e.setPressListener(event);
              //i=new InspectorButton1<Integer, ColorButton>(name, e);
              i=new InspectorColorVarButton(name, e, a);
            } else {
              i=new InspectorButton1<Integer, TextBox>(name, new TextBox(name1).setNumberOnly(TextBox.NumberType.INTEGER));
            }
          } else if (a.field.getType() == float.class || a.field.getType() == Float.class) {
            i=new InspectorButton1<Float, TextBox>(name, new TextBox(name1).setNumberOnly(TextBox.NumberType.FLOAT));
          } else if (a.field.getType() == boolean.class || a.field.getType() == Boolean.class) {
            i=new InspectorButton1<Boolean, ToggleButton>(name, new ToggleButton(name1));
          } else if (a.field.getType() == Rect.class) {
            i=new InspectorRectButton(name);
          } else if (a.field.getType() == String.class) {
            i=new InspectorButton1<String, TextBox>(name, new TextBox(name1).setNumberOnly(TextBox.NumberType.NONE));
          } else if (a.field.getType() == PImage.class) {
            i=new InspectorButton1<PImage, ImageDrop>(name, new ImageDrop(name1));
          } else if (a.field.getType() == PFont.class) {
            i=new InspectorButton1<PFont, FontDrop>(name, new FontDrop(name1));
          } else if (a.field.getType().isEnum()) {
            DropDown dd=new DropDown(name1);
            Object[] enumConstants=a.field.getType().getEnumConstants();
            for (Object o : enumConstants) {
              dd.addItem(o.toString());
            }
            dd.text="NONE";
            i=new InspectorButton1<Enum, DropDown>(name, dd, new TypeChanger<Enum, Integer>() {
              @Override
              public Enum changeBtoA(Integer in) {
                return (Enum)enumConstants[in];
              }
              @Override
              public Integer changeAtoB(Enum in) {
                for (int index=0; index < enumConstants.length; index++) {
                  if (enumConstants[index].equals(in)) {
                    return index;
                  }
                }
                return -1;
              }
            });
          } else {
            KyUI.err(a.field.getType().getTypeName() + " is not handled in ElementLoader.");
            continue;
          }
          i.text=a.field.getName();
          i.addedTo(inspector);
          a.setRef((DataTransferable)i);
          items.add(i);
        }
      }
    }
    public void setAttribute(Element e) {
      try {
        for (Attribute.Editor a : attrs) {
          if (a.ref != null) {
            ((DataTransferable)a.ref).set(a.getField(e));
            if ((a.field.getType() == Integer.class || a.field.getType() == int.class) && a.attr.type() == Attribute.COLOR) {
              InspectorColorVarButton.ColorVariable v=ElementLoader.varsReverse.get(new InspectorColorVarButton.Reference(a, e));
              if (v == null || v.name.equals("NONE")) {
                ((InspectorColorVarButton)a.ref).vars.text="NONE";
              } else {
                ((InspectorColorVarButton)a.ref).vars.text=v.name;
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    public Attribute.Editor getAttribute(String attrName) {
      for (Attribute.Editor attr : attrs) {
        if (attr.field.getName().equals(attrName)) {
          //if two classes with extend relation has same name attribute, this method returns derived class's attribute.
          //this makes malfunction in loading/saving layout into xml, so make two classes has no same name attributes!
          return attr;
        }
      }
      return null;
    }
  }
  public static TreeGraph.Node<Element> addElement(TreeGraph.Node<Element> node, String name, Class type) {//class extends Element
    try {
      Constructor<? extends Element> c=type.getDeclaredConstructor(String.class);
      c.setAccessible(true);
      TreeGraph.Node<Element> n=node.addNode(name, c.newInstance(name));
      //for if name is changed...
      n.text=n.content.getName();
      return n;
    } catch (Exception ee) {
      ee.printStackTrace();
    }
    return null;
  }
}
