package kyui.util;
import kyui.core.Element;
import processing.event.MouseEvent;
public interface GestureDetector {
  public boolean detect(Element e,MouseEvent ev);
}
