package kyui.loader;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.element.LinearList;
import kyui.util.ColorExt;
import kyui.util.Rect;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
public class ElementLoader {
  static LinearList elementList;
  public static ArrayList<Class<? extends Element>> types=new ArrayList<>();
  static PGraphics imager;
  public static void loadOnStart(LinearList list) {
    elementList=list;
    if (list == null) {
      return;
    }
    loadInternal();
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
            addElement(c);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static void loadInternal() {
    addElement(Element.class);
    Reflections reflections=new Reflections("kyui.element");
    Set<Class<? extends Element>> set=reflections.getSubTypesOf(Element.class);
    for (Class c : set) {
      addElement(c);
    }
  }
  static void addElement(Class<? extends Element> c) {
    types.add(c);
    elementList.addItem(new ElementImage(c, elementList));
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
    public ElementImage(Class<? extends Element> c, LinearList Ref_) {
      super(c.getTypeName(), Ref_);
      element=c;
      try {//recommended size of image is 120x120, max is 150x150.
        String className=c.getTypeName();
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
}
