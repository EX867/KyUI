package kyui.editor;
import kyui.core.Element;
import kyui.element.ColorButton;
import kyui.element.DropDown;
import kyui.element.LinearList;
import kyui.loader.ElementLoader;
import processing.event.MouseEvent;

import java.util.LinkedList;
public class InspectorColorVarButton extends InspectorButton1<Integer, ColorButton> {
  public static class ColorVariable {
    public String name;
    public Integer value;
    public LinkedList<Reference> references=new LinkedList<>();
    public LinearList.SelectableButton selectableButton;
    public ColorVariable(String name_, Integer value_) {
      name=name_;
      value=value_;
      selectableButton=new LinearList.SelectableButton(name + ":listItem");
      ElementLoader.variableList.add(selectableButton);
      selectableButton.text=name;
    }
    public static class Reference {
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
  public DropDown vars;
  public int selection=0;//none
  public InspectorColorVarButton(String name) {
    super(name);
    System.err.println("can\'t instantiate this class with default element constructor! Attribute.Editor ref needed.");
  }
  public InspectorColorVarButton(String name, ColorButton cb, Attribute.Editor ed) {
    super(name, cb);
    vars=new DropDown(name + ":vars");
    vars.setSelectListener((int index) -> {
      selection=index;
    });
    vars.setPressListener((MouseEvent e, int index) -> {
      vars.getPicker().setItems(ElementLoader.variableList);
      for (LinearList.SelectableButton b : ElementLoader.variableList) {
        b.addedTo(vars.getPicker().listLayout);
      }
      return false;
    });
    vars.setSelectListener((int index) -> {
      if (index != selection) {
        ColorVariable oldVar=null;
        ColorVariable var=null;
        if (selection != 0) {
          oldVar=ElementLoader.vars.get(((LinearList.SelectableButton)vars.getPicker().getItems().get(selection)).text);
        }
        if (index != 0) {
          var=ElementLoader.vars.get(((LinearList.SelectableButton)vars.getPicker().getItems().get(index)).text);
        }
        ColorVariable.Reference ref=new ColorVariable.Reference(ed, kyui.editor.Main.selection);
        if (oldVar != null) {
          oldVar.references.remove(ref);
        }
        if (var != null) {
          var.references.add(ref);
          transferable.set(var.value);
          ref.attr.setField(ref.el, cb.c);
        }
        selection=index;
      }
    });
    addChild(vars);
    reorderChild(0, 1);
  }
  @Override
  public void onLayout() {
    padding2=(pos.bottom - pos.top) / 6;
    float left=padding2;
    float width=pos.bottom - pos.top;
    children.get(0).setPosition(children.get(0).pos.set(pos.right - left - width * 4, pos.top + padding2, pos.right - left - width, pos.bottom - padding2));//dropDown
    children.get(1).setPosition(children.get(1).pos.set(pos.right - left - width + padding2, pos.top + padding2, pos.right - left, pos.bottom - padding2));//colorButton
  }
}
