package kyui.loader;
import kyui.core.KyUI;
import kyui.event.EventListener;
import processing.data.XML;
public class ShortcutLoader {
  //first load default data and then loading custom data will
  public static boolean loadXml(String path, boolean custom) {
    if (!new java.io.File(path).isFile()) {
      return false;
    }
    XML[] data=KyUI.Ref.loadXML(path).getChildren("shortcut");
    for (XML d : data) {
      KyUI.addShortcut(new KyUI.Shortcut(d.getContent(), b(d.getString("ctrl")), b(d.getString("alt")), b(d.getString("shift")), d.getInt("key"), null, custom));
    }
    return true;
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
}