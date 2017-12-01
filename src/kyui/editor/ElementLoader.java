package kyui.editor;
import kyui.core.Element;
import org.reflections.Reflections;

import java.util.Set;
public class ElementLoader {
  private void load() {
    Reflections reflections=new Reflections("my.project.prefix");
    Set<Class<? extends Element>> allClasses=reflections.getSubTypesOf(Element.class);
  }
  public void loadInternal() {
  }
  public void loadExternal() {
  }
}
