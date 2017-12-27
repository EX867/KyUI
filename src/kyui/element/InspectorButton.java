package kyui.element;
import kyui.core.Attributes;
import kyui.core.Element;
import kyui.core.KyUI;
import processing.core.PGraphics;
public class InspectorButton extends LinearList.SelectableButton {
  //modifiable values
  public float ratio=3.0F;//this is inspection button width by height.
  public float padding2;
  public InspectorButton(String name) {
    super(name);
  }
  @Override
  public void render(PGraphics g) {
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    textOffsetX=(int)(-(pos.right - pos.left) / 2 + padding);
    super.render(g);
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  @Override
  public void onLayout() {
    padding2=(pos.bottom - pos.top) / 6;
    float left=padding2;
    float width=Math.min((pos.bottom - pos.top) * ratio, (pos.right - pos.left) * 0.5F);//FIX>>default valueI??
    for (int a=children.size() - 1; a >= 0; a--) {//just works like horizontal LinearList...
      Element child=children.get(a);
      child.setPosition(child.pos.set(pos.right - left - width + padding2, pos.top + padding2, pos.right - left, pos.bottom - padding2));
      left+=width;
    }
    if (Ref.direction == Attributes.Direction.HORIZONTAL) {//FIX>> not horizontal
      for (Element child : children) {
        child.setActive(false);
        child.setVisible(false);
      }
    }
  }
}