package kyui.util;
import kyui.event.EventListener;
public interface DataTransferable<Type> {
  public Type get();
  public void set(Type value);
  public void setDataChangeListener(EventListener event);
}
