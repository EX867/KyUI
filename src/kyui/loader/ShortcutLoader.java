package kyui.loader;
import kyui.core.KyUI;
import kyui.event.EventListener;
import processing.data.XML;
public class ShortcutLoader {
  //first load default data and then loading custom data will
  public static void loadXml(String path, boolean custom) {
    XML[] data=KyUI.Ref.loadXML(path).getChildren("shortcut");
    for (XML d : data) {
      KyUI.addShortcut(new KyUI.Shortcut(d.getContent(), b(d.getString("ctrl")), b(d.getString("alt")), b(d.getString("shift")), d.getInt("key"), null, custom));
    }
  }
  public static void attachTo(String name, EventListener event) {
    KyUI.shortcutsByName.get(name).event=event;
  }
  private static boolean b(String in) {
    return in.equals("true");
  }
}