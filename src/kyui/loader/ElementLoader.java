package kyui.loader;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.editor.Attribute;
import kyui.element.LinearLayout;
import kyui.element.LinearList;
import kyui.element.TextBox;
import kyui.element.ToggleButton;
import kyui.util.ColorExt;
import kyui.util.HideInEditor;
import kyui.util.Rect;
import kyui.editor.inspectorItem.*;
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
public class ElementLoader {
  static LinearList elementList;
  public static ArrayList<Class<? extends Element>> types=new ArrayList<>();
  public static HashMap<Class, AttributeSet> attributes=new HashMap<>();
  static PGraphics imager;
  public static void loadOnStart(LinearList list) {
    elementList=list;
    if (list == null) {
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
          if (new File(line).isFile()) {
            loadExternal(line);
          }
          line=read.readLine();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      paths.getParentFile().mkdirs();
      try {
        paths.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  public static void loadExternal(String path) {//https://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
    try {
      JarFile jarFile=new JarFile(path);
      Enumeration<JarEntry> e=jarFile.entries();
      URL[] urls={new URL("jar:file:" + path + "!/")};
      URLClassLoader cl=URLClassLoader.newInstance(urls);
      while (e.hasMoreElements()) {
        JarEntry je=e.nextElement();
        if (je.isDirectory() || !je.getName().endsWith(".class")) {
          continue;
        }
        String className=je.getName().substring(0, je.getName().length() - 6);// -6 because of .class
        className=className.replace('/', '.');
        try {
          Class.forName(className);
        } catch (ClassNotFoundException ee) {
          Class c=cl.loadClass(className);
          if (c.isAssignableFrom(Element.class)) {
            loadClass(c);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
      Set<Class<? extends kyui.editor.inspectorItem.InspectorButton>> set2=reflections.getSubTypesOf(kyui.editor.inspectorItem.InspectorButton.class);//????
      for (Class c : set2) {
        loadClass(c);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  static void loadClass(Class<? extends Element> c) throws Exception {//assert no duplication
    if (!Modifier.isAbstract(c.getModifiers()) && c.getAnnotation(HideInEditor.class) == null) {
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
  public static PImage loadImageResource(String filename) {
    InputStream input=Element.class.getResourceAsStream("/" + filename);
    if (input == null) {
    } else {
      System.out.println("[KyUI] image " + filename + " is loaded");
      byte[] bytes=KyUI.Ref.loadBytes(input);
      if (bytes == null) {
      } else {
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
    }
    return null;
  }
  public static class ElementImage extends LinearList.SelectableButton {
    public Class<? extends Element> element;
    PImage image;
    public ElementImage(Class<? extends Element> c) {
      super(c.getTypeName());
      element=c;
      try {//recommended size of image is 120x120, max is 150x150.
        String className=c.getTypeName();
        text=c.getSimpleName();
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
        System.out.println("[KyUI] class load failed : " + c.getTypeName());
        e.printStackTrace();
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
  public static class AttributeSet {
    public ArrayList<Attribute.Editor> attrs;
    public ArrayList<InspectorButton> items;
    public AttributeSet(ArrayList<Attribute.Editor> attrs_) {
      attrs=attrs_;
      items=new ArrayList<>();
      items.ensureCapacity(attrs.size());
      LinearLayout inspector=KyUI.<LinearList>get2("layout_inspector").listLayout;
      for (Attribute.Editor a : attrs) {
        InspectorButton i=null;
        String name=a.c.getTypeName() + "." + a.field.getName();
        if (a.field.getType() == int.class || a.field.getType() == Integer.class) {
          if (a.attr.type() == Attribute.COLOR) {
            i=new InspectorColorButton(name);//FIX>> this to new class InspectorColorButton + Variable
          } else {
            i=new InspectorTextButton<Integer>(name, Integer.class);
          }
        } else if (a.field.getType() == float.class || a.field.getType() == Float.class) {
          i=new InspectorTextButton<Float>(name, Float.class);
        } else if (a.field.getType() == boolean.class || a.field.getType() == Boolean.class) {
          i=new InspectorToggleButton(name);
        } else if (a.field.getType() == Rect.class) {
          i=new InspectorRectButton(name);
        } else if (a.field.getType() == String.class) {
          i=new InspectorTextButton<String>(name, String.class);
        } else if (a.field.getType() == PImage.class) {
          i=new InspectorImageButton(name);
        } else if (a.field.getType() == PFont.class) {
          i=new InspectorFontButton(name);
        } else if (a.field.getType().isEnum()) {
          i=new InspectorDropDownButton(name, a.field.getType());
          Object[] enumConstants=a.field.getType().getEnumConstants();
          for (Object o : enumConstants) {
            ((InspectorDropDownButton)i).dropDown.addItem(o.toString());
          }
        } else {
          System.err.println(a.field.getType().getTypeName() + " is not handled in ElementLoader.");
          continue;
        }
        i.text=a.field.getName();
        i.addedTo(inspector);
        a.setRef(i);
        items.add(i);
      }
    }
    public void setAttribute(Element e) {
      try {
        for (Attribute.Editor a : attrs) {
          if (a.ref != null) {
            a.ref.set(a.getField(e));
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
