package kyui.element;
import kyui.core.Element;

import java.util.ArrayList;
import java.util.List;
public class ListConnector extends Element {
  public LinearList left;
  public LinearList right;
  ArrayList<Link> links = new ArrayList<>();
  public ListConnector(String name_) {
    super(name_);
  }
  //if list items change, call moveToSameTexts() to reattach. so if you use this do not make duplicate text on it.
  class Link {
    Button before;
    Button after;
    Runnable removeListener;
    Link(Button before_, Button after_, Runnable removeListener_) {
      before = before_;
      after = after_;
      removeListener = removeListener_;
    }
    boolean moveToSameTexts(List<Button> lefts, List<Button> rights) {
      boolean lextExist=false;
      boolean rightExist=false;
      for (Button b : lefts) {
        if (b.text.equals(before.text)) {
          before = b;
          lextExist=true;
          break;
        }
      }
      for (Button b : rights) {
        if (b.text.equals(after.text)) {
          after = b;
          rightExist=true;
          break;
        }
      }
      return leftEXist&&rightEXist;
    }
  }
  void moveToSameTexts() {
    List<Button> lefts = (List)left.getItems();
    List<Button> rights = (List)right.getItems();
    for (int a = 0; a < links.size(); a++) {
      if (!links.get(a).moveToSameTexts(lefts, rights)) {
        links.get(a).removeListener.run();
        links.remove(a);
        a--;
      }
    }
  }
}
