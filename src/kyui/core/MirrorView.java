package kyui.core;
import kyui.event.ExtendedRenderer;
import processing.core.PGraphics;

import java.util.ArrayList;
public class MirrorView extends RelativeFrame {//mirrorview do not get key event.
  //do nothing, but adjust transform values to show like view is in other position.
  private boolean scroll=false;//hide!!
  public ArrayList<ExtendedRenderer> overlays=new ArrayList<>();
  public MirrorView(String name) {
    super(name);
    children_max=1;
    super.scroll=false;
    modifyChildrenTask=new ModifyChildrenTask(this) {
      @Override
      public void execute(Object raw) {
        if (raw instanceof AddChildData) {
          AddChildData data=(AddChildData)raw;
          data.element.setPositionListener=(Element e)->{//e is moved element
            setOffset(pos.right-e.pos.right,pos.top-e.pos.top);//set offset with left top
          };
          setPositionListener=(Element e)->{//e is me
            setOffset(pos.right-data.element.pos.right,pos.top-data.element.pos.top);//set offset with left top
          };
          setOffset(pos.right-data.element.pos.right,pos.top-data.element.pos.top);//set offset with left top
        }
        super.execute(raw);
      }
    };
  }
  public void setChild(Element e) {//giving you handy method
    for (Element el : children) {//are there errors??
      removeChild(0);
    }
    addChild(e);
  }
  @Override
  public void render(PGraphics g){
    for(ExtendedRenderer e : overlays){
      e.render(g);
    }
    super.render(g);
  }
}
