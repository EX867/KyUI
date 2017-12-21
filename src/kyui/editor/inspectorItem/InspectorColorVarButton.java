package kyui.editor.inspectorItem;
import kyui.core.Element;
import kyui.editor.Attribute;
import kyui.element.ColorButton;
import kyui.element.DropDown;
import kyui.element.LinearList;
import kyui.event.EventListener;
import kyui.loader.ElementLoader;
import kyui.util.DataTransferable;

import java.util.LinkedList;
public class InspectorColorVarButton extends InspectorButton implements DataTransferable<Integer> {
  public static class ColorVariable {
    public String name;
    public Integer value;
    public LinkedList<Reference> references=new LinkedList<>();
    public LinearList.SelectableButton selectableButton;
    public ColorVariable(String name_, Integer value_) {
      name=name_;
      value=value_;
      selectableButton=new LinearList.SelectableButton(name + ":listItem");
      variableList.add(selectableButton);
    }
    public class Reference {
      public Attribute.Editor attr;
      public Element el;
      public Reference(Attribute.Editor attr_, Element el_) {
        attr=attr_;
        el=el_;
      }
      @Override
      public boolean equals(Object obj) {
        if (obj instanceof Reference) {
          Reference ref=(Reference)obj;
          return el.equals(ref.el) && attr.equals(ref.attr);
        }
        return false;
      }
    }
  }
  public ColorButton colorButton;
  public DropDown vars;
  public int selection=0;//none
  public InspectorColorVarButton(String name) {
    super(name);
    colorButton=new ColorButton(name + ":colorButton");
    vars=new DropDown(name + ":vars");
    vars.setSelectListener((int index) -> {
      selection=index;
    });
  }
  @Override
  public Integer get() {
    ColorVariable var=ElementLoader.vars.get(selection);
    if (var == null) {
      return colorButton.c;
    } else {
      return var.value;
    }
  }
  @Override
  public void set(Integer value) {
    if(ElementLoader.vars.get().references.contains())
  }
  @Override
  public void setDataChangeListener(EventListener event) {
  }
}
