package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;

import java.io.File;
import java.util.function.Consumer;

import kyui.util.DoubleClickGestureDetector;
import processing.core.PGraphics;
import processing.event.MouseEvent;
import processing.core.PFont;
public class FileSelectorButton extends LinearList.SelectableButton {
  public File file;
  public Consumer<File> doubleClickListener;
  public static PFont fileTextFont = null;
  protected FileSelectorButton(String name) {//cannot be accessed by name!
    super("");
  }
  protected FileSelectorButton(String name, File file_, Consumer<File> doubleClickListener_) {
    super("");
    file = file_;
    if (file.isDirectory()) {
      text = "/" + file.getName();
    } else {
      text = file.getName();
    }
    doubleClickListener = doubleClickListener_;
  }
  DoubleClickGestureDetector doubleClick = new DoubleClickGestureDetector((Element e) -> {
    if (file != null) {
      if (file.isDirectory()) {
        listDirectory(Ref, file, doubleClickListener);
        Ref.onLayout();
        Ref.invalidate();
      } else {
        if (doubleClickListener != null) {
          doubleClickListener.accept(file);
        }
      }
    }
  });
  @Override
  public void addedTo(Element e) {
    super.addedTo(e);
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    boolean ret = super.mouseEvent(e, index);
    ret |= doubleClick.detect(this, e);
    return ret;
  }
  @Override
  public void render(PGraphics g) {
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    textOffsetX = (int)(-(pos.right - pos.left) / 2 + padding);
    super.render(g);
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  public static void listDirectory(LinearList list, File file, Consumer<File> doubleClickListener) {//file must be directory!
    if (!file.isDirectory()) {
      return;
    }
    File[] files = file.listFiles();
    if (files == null) {
      return;
    }
    if (fileTextFont == null) {
      fileTextFont = KyUI.fontText;
    }
    list.listLayout.children.clear();//you can clear because probably that list is file list!
    File parentFile = file.getParentFile();
    if (parentFile != null) {
      FileSelectorButton parent = new FileSelectorButton("", parentFile, doubleClickListener);
      parent.text = "/..";
      parent.textFont = fileTextFont;
      list.listLayout.addChild(parent);
    }
    for (File f : files) {
      FileSelectorButton btn = new FileSelectorButton("", f, doubleClickListener);
      btn.textFont = fileTextFont;
      list.listLayout.addChild(btn);
    }
    KyUI.taskManager.executeAll();
    list.afterModify();
  }
}
