package kyui.editor.inspectorItem;
import com.sun.istack.internal.Nullable;
import kyui.core.Element;
import kyui.event.EventListener;
import kyui.util.DataTransferable;
import kyui.util.TypeChanger;
public class InspectorButton1<DataType, DataElement extends Element & DataTransferable> extends InspectorButton implements DataTransferable {
  protected DataTransferable transferable;//A is DataType, B is ElementType
  @Nullable
  protected TypeChanger typeChanger;
  public InspectorButton1(String name) {
    super(name);
  }
  public InspectorButton1(String name, DataElement element) {
    super(name);
    transferable=element;
    addChild(element);
  }
  public InspectorButton1(String name, DataElement element, TypeChanger typeChanger_) {
    super(name);
    transferable=element;
    typeChanger=typeChanger_;
    addChild(element);
  }
  @Override
  public Object get() {
    if (typeChanger == null) {
      return transferable.get();
    } else {
      return typeChanger.changeBtoA(transferable.get());
    }
  }
  @Override
  public void set(Object value) {
    if (typeChanger == null) {
      transferable.set(value);
    } else {
      transferable.set(typeChanger.changeAtoB(value));
    }
  }
  @Override
  public void setDataChangeListener(EventListener event) {
    transferable.setDataChangeListener(event);
  }
}
