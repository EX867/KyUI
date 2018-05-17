package kyui.util;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.event.EventListener;
import processing.event.MouseEvent;
public class DoubleClickGestureDetector implements GestureDetector{
  public EventListener listener;
  long lastClicked=0;
  boolean doubleClickReady=false;
  public DoubleClickGestureDetector(EventListener listener_){
    listener=listener_;
  }
  @Override public boolean detect(Element e, MouseEvent ev) {
    boolean ret=true;
    if (ev.getAction() == MouseEvent.PRESS) {
      long time=System.currentTimeMillis();
      if (doubleClickReady && time - lastClicked < KyUI.DOUBLE_CLICK_INTERVAL) {
        listener.onEvent(e);
        doubleClickReady=false;
        ret=false;
      } else {
        doubleClickReady=true;
      }
      lastClicked=time;
    }
    return ret;
  }
}
