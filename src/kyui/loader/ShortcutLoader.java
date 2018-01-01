package kyui.loader;
import com.sun.istack.internal.Nullable;
import kyui.core.KyUI;
import kyui.editor.InspectorButton1;
import kyui.element.KeyCatcher;
import kyui.element.LinearList;
import kyui.event.EventListener;
import processing.data.XML;

import java.util.List;
public class ShortcutLoader {
  //first load default data and then loading custom data will
  public static void loadXml(XML xml) {
    if (xml.getChild("shortcut") == null) {
      KyUI.err("ShortcutLoader - failed to load shortcut xml : xml has no shortcut section.");
      return;
    }
    xml=xml.getChild("shortcut");
    XML[] data=xml.getChildren("shortcut");
    for (XML d : data) {
      KyUI.addShortcut(new KyUI.Shortcut(d.getContent(), b(d.getString("ctrl")), b(d.getString("alt")), b(d.getString("shift")), d.getInt("key"), d.getInt("keyCode"), null));
    }
  }
  public static void loadXmlEditor(XML xml, LinearList shortcuts_list) {
    if (xml.getChild("shortcut") == null) {
      KyUI.err("ShortcutLoader - failed to load shortcut xml : xml has no shortcut section.");
      return;
    }
    xml=xml.getChild("shortcut");
    XML[] data=xml.getChildren("shortcut");
    for (XML d : data) {
      if (!shortcuts_list.getItems().contains("KyUI:shortcut:" + d.getContent())) {
        InspectorButton1 ib=addShortcut(d.getContent(), shortcuts_list);
        KyUI.Key key;
        ib.set(key=new KyUI.Key(b(d.getString("ctrl")), b(d.getString("alt")), b(d.getString("shift")), d.getInt("key"), d.getInt("keyCode")));
      }
    }
  }
  public static void attachTo(String name, EventListener event) {
    KyUI.Shortcut s=KyUI.shortcutsByName.get(name);
    if (s != null) {
      s.event=event;
    }
  }
  private static boolean b(String in) {
    return in.equals("true");
  }
  public static XML saveXML(@Nullable XML startXml, LinearList list) {
    if (startXml == null) {
      startXml=new XML("Data");
    } else {
      if (startXml.getChildren("shortcut").length != 0) {
        XML[] olds=startXml.getChildren("shortcut");
        for (XML old : olds) {
          startXml.removeChild(old);
        }
      }
    }
    XML shortcut=startXml.addChild("shortcut");
    for (kyui.core.Element ib_ : list.getItems()) {
      InspectorButton1 ib=(InspectorButton1)ib_;
      KyUI.Shortcut sc=new KyUI.Shortcut(ib.text, (KyUI.Key)ib.transferable.get(), null);
      shortcut.addChild(sc.toXML());
    }
    return startXml;
  }
  public static InspectorButton1 addShortcut(String name, LinearList shortcuts_list) {
    KeyCatcher catcher=new KeyCatcher("KyUI:" + name);
    InspectorButton1 ib=new InspectorButton1<KyUI.Key, KeyCatcher>("KyUI:shortcut:" + name, catcher);
    ib.ratio=6;
    ib.text=catcher.getName().substring(5);
    //ib.setDataChangeListener((Element el) -> {//no need...});
    shortcuts_list.addItem(ib);
    return ib;
  }
}