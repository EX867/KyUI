package kyui.editor.inspectorItem;
import kyui.element.ImageDrop;
import processing.core.PImage;
public class InspectorImageButton extends InspectorButton<PImage> {
  public ImageDrop imageDrop;//get this TextBox directly and you can modify this.
  public InspectorImageButton(String name) {
    super(name);
    init();
  }
  private void init() {
    imageDrop=new ImageDrop(getName() + ":imageDrop");
    addChild(imageDrop);
  }
  @Override
  public void set(PImage value) {
    imageDrop.display=value;
  }
  @Override
  public PImage get() {
    return imageDrop.display;
  }
}