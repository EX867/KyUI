package kyui.element;
import kyui.core.DropMessenger;
import kyui.core.Element;
import kyui.core.KyUI;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
public class ListConnector extends Element {
  LinearList left;
  LinearList right;
  ArrayList<Link> links = new ArrayList<>();
  public BiConsumer<Button, Button> addListener = (a, b) -> {
  };
  public BiFunction<Button, Button, Runnable> newRemoveListener = (a, b) -> {
    return () -> {
    };
  };
  public int fgColor = 0xFF000000;
  public ListConnector(String name_) {
    super(name_);
    clipping = true;
  }
  public void set(LinearList left_, LinearList right_) {
    left = left_;
    right = right_;
    //always add left to right.
    KyUI.addDragAndDrop(left, right, (DropMessenger messenger, MouseEvent end, int endIndex) -> {
      addLink((Button)left.getItems().get(messenger.startIndex), (Button)right.getItems().get(endIndex));
      invalidate();
    });
    KyUI.addDragAndDrop(right, left, (DropMessenger messenger, MouseEvent end, int endIndex) -> {
      addLink((Button)left.getItems().get(endIndex), (Button)right.getItems().get(messenger.startIndex));
      invalidate();
    });
  }
  //if list items change, call moveToSameNames() to reattach. remove previous itmes first before adding new items.
  class Link {
    Button before;
    Button after;
    Runnable removeListener;
    Link(Button before_, Button after_, Runnable removeListener_) {
      before = before_;
      after = after_;
      removeListener = removeListener_;
    }
    boolean moveToSameNames(List<Button> lefts, List<Button> rights) {
      boolean leftExist = false;
      boolean rightExist = false;
      for (Button b : lefts) {
        if (b.getName().equals(before.getName())) {
          before = b;
          leftExist = true;
          break;
        }
      }
      for (Button b : rights) {
        if (b.getName().equals(after.getName())) {
          after = b;
          rightExist = true;
          break;
        }
      }
      return leftExist && rightExist;
    }
  }
  @Override public void render(PGraphics g) {
    for (Link l : links) {
      g.line((l.before.pos.left + l.before.pos.right) / 2, (l.before.pos.top + l.before.pos.bottom) / 2, (l.after.pos.left + l.after.pos.right) / 2, (l.after.pos.top + l.after.pos.bottom) / 2);
    }
  }
  public void addLink(Button a, Button b) {
    addListener.accept(a, b);
    links.add(new Link(a, b, newRemoveListener.apply(a, b)));
  }
  void moveToSameTexts() {
    List<Button> lefts = (List)left.getItems();
    List<Button> rights = (List)right.getItems();
    for (int a = 0; a < links.size(); a++) {
      if (!links.get(a).moveToSameNames(lefts, rights)) {
        links.get(a).removeListener.run();
        links.remove(a);
        a--;
      }
    }
  }
}
