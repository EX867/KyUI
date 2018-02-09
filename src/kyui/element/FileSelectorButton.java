package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;

import java.io.File;
import java.util.function.Consumer;

import processing.core.PGraphics;
import processing.event.MouseEvent;
public class FileSelectorButton extends LinearList.SelectableButton {
  long lastClicked=0;
  boolean doubleClickReady=false;
  public File file;
  public Consumer<File> doubleClickListener;
  protected FileSelectorButton(String name) {//cannot be accessed by name!
    super("");
  }
  protected FileSelectorButton(String name, File file_, Consumer<File> doubleClickListener_) {
    super("");
    file=file_;
    if (file.isDirectory()) {
      text="/" + file.getName();
    } else {
      text=file.getName();
    }
    doubleClickListener=doubleClickListener_;
  }
  @Override
  public void addedTo(Element e) {
    super.addedTo(e);
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    boolean ret=super.mouseEvent(e, index);
    if (e.getAction() == MouseEvent.PRESS) {
      long time=System.currentTimeMillis();
      if (doubleClickReady && time - lastClicked < KyUI.DOUBLE_CLICK_INTERVAL) {
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
      } else {
        doubleClickReady=true;
      }
      lastClicked=time;
    }
    return ret;
  }
  @Override
  public void render(PGraphics g) {
    g.textAlign(KyUI.Ref.LEFT, KyUI.Ref.CENTER);
    textOffsetX=(int)(-(pos.right - pos.left) / 2 + padding);
    super.render(g);
    g.textAlign(KyUI.Ref.CENTER, KyUI.Ref.CENTER);
  }
  public static void listDirectory(LinearList list, File file, Consumer<File> doubleClickListener) {//file must be directory!
    if (!file.isDirectory()) {
      return;
    }
    File[] files=file.listFiles();
    if (files == null) {
      return;
    }
    list.listLayout.children.clear();//you can clear because probably that list is file list!
    File parentFile=file.getParentFile();
    if (parentFile != null) {
      FileSelectorButton parent=new FileSelectorButton("", parentFile, doubleClickListener);
      parent.text="/..";
      list.listLayout.addChild(parent);
    }
    for (File f : files) {
      list.listLayout.addChild(new FileSelectorButton("", f, doubleClickListener));
    }
    KyUI.taskManager.executeAll();
    list.afterModify();
  }
}
