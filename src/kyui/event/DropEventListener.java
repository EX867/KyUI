package kyui.event;
import kyui.core.DropMessenger;
import processing.event.MouseEvent;
public interface DropEventListener{
  public void onEvent(DropMessenger messenger, MouseEvent end,int endIndex);
}