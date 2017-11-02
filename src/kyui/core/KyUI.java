package kyui.core;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.Event;
import processing.event.MouseEvent;
import processing.event.KeyEvent;
public class KyUI {
  //
  protected static PApplet Ref;
  private static boolean ready=false;
  private static boolean end=false;
  // object control
  protected static final HashMap<String, Element> Elements=new HashMap<String, Element>();
  protected static Element root=new Element("root");//no support multi window.
  public static int focus=0;
  // events
  public static LinkedList<ModEvent> EventQueue=new LinkedList<ModEvent>();//items popped from update thread.
  //
  public static final int STATE_PRESS=1;
  public static final int STATE_PRESSED=2;
  public static final int STATE_RELEASE=3;
  public static final int STATE_RELEASED=4;
  public static int mouseState=STATE_RELEASED;//no multi touch
  public static int mouseTime=0;// this parameter indicates the time after mouse clicked.
  public static Vector2 mouseClick=new Vector2();// this parameter stores mouse click position.
  public static Vector2 mouseGlobal=new Vector2();// this parameter stores global mouse position.(scaled)
  //
  public static int KEY_INIT_DELAY=600;// you can change this value.
  public static int KEY_INTERVAL=50;
  public static boolean ctrlPressed=false;
  public static boolean shiftPressed=false;
  public static boolean altPressed=false;
  public static boolean keyState=false;
  public static long keyTime=0;
  public static boolean keyInit=false;// this used on textEdit and etc...
  protected static List<Long> reflectedPressedKeys;
  // graphics
  public static long drawStart=0;// these 3 parameters used to measure elapsed time.
  public static long drawEnd=0;
  public static long drawInterval=0;
  public static float scaleGlobal=1.0F;// this parameter stores global window scale.
  //thread
  public static Thread updater;
  public static int updater_interval;
  //public Thread animation;
  private KyUI() {
  }
  public static void start(PApplet ref) {
    start(ref, 30);
  }
  @SuppressWarnings("unchecked")
  public static void start(PApplet ref, int rate) {
    if (ready) return;// this makes setup() only called once.
    Ref=ref;
    try {
      Field pressedKeys;
      pressedKeys=PApplet.class.getDeclaredField("pressedKeys");
      pressedKeys.setAccessible(true);
      reflectedPressedKeys=(List<Long>)pressedKeys.get(Ref);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ready=true;
    updater=new Thread(new Updater());
    frameRate(rate);
    updater.start();
  }
  void end() {
    end=true;
  }
  public static void frameRate(int rate) {//update thread frame rate.
    updater_interval=1000 / rate;
  }
  protected static void addElement(Element object) {
    Elements.put(object.getName(), object);
  }
  protected static void removeElement(String name) {
    Elements.remove(name);
  }
  public static Element getRoot() {
    return root;
  }
  public static Element get(String name) {
    return Elements.get(name);
  }
  // called by processing animation thread
  public static void update() {
    root.update_();
  }
  public static void render(PGraphics g) {
    drawStart=drawEnd;
    g.rectMode(PApplet.CORNERS);
    root.render_(g);
    drawEnd=System.currentTimeMillis();
    drawInterval=drawEnd - drawStart;
  }
  static class Updater implements Runnable {
    @Override
    public void run() {
      while (!end) {
        try {
          Thread.sleep(updater_interval);
        } catch (InterruptedException e) {
        }
        //empty EventQueue.
        while (EventQueue.size() > 0) {
          ModEvent e=EventQueue.pollFirst();
          if (e.type == ModEvent.KEY_EVENT) {
            root.keyEvent_((KeyEvent)e.e);
          } else if (e.type == ModEvent.KEY_TYPED) {
            root.keyTyped_((KeyEvent)e.e);
          } else if (e.type == ModEvent.MOUSE_EVENT) {
            root.mouseEvent_((MouseEvent)e.e);
          }
        }
        //
        root.update_();
      }
    }
  }
  //
  static class ModEvent {
    static final int KEY_TYPED=1;
    static final int KEY_EVENT=2;
    static final int MOUSE_EVENT=3;
    int type;
    Event e;
    public ModEvent(int type_, Event e_) {
      type=type;
      e=e_;
    }
  }
  public static void handleKeyEvent(KeyEvent e) {// FIX >> This code is unstable. test and fix!
    if (Ref.key == PApplet.ESC) {
      Ref.key=0; // Fools! don't let them escape!
    }
    if (e.getKeyCode() == PApplet.CODED) {
      if (e.getAction() == KeyEvent.PRESS) {//handle first.
        if (e.getKey() == PApplet.CONTROL) ctrlPressed=true;//???
        else if (e.getKey() == PApplet.SHIFT) shiftPressed=true;
        else if (e.getKey() == PApplet.ALT) altPressed=true;
      } else if (e.getAction() == KeyEvent.RELEASE) {
        if (e.getKey() == PApplet.CONTROL) ctrlPressed=false;
        else if (e.getKey() == PApplet.SHIFT) shiftPressed=false;
        else if (e.getKey() == PApplet.ALT) altPressed=false;
      }
    }
    EventQueue.add(new ModEvent(ModEvent.KEY_EVENT, e));
    if (keyState == false) {
      EventQueue.add(new ModEvent(ModEvent.KEY_TYPED, e));
    }
    keyState=true;
    if (Ref.keyPressed == false) keyTime=System.currentTimeMillis();
    if (keyInit) {
      if (System.currentTimeMillis() - keyTime > KEY_INTERVAL) keyState=false;
    } else {
      if (System.currentTimeMillis() - keyTime > KEY_INIT_DELAY) {
        keyState=false;
        keyInit=true;
      }
    }
    if (e.getAction() == KeyEvent.RELEASE) {
      if (reflectedPressedKeys.isEmpty()) {
        keyState=false;
        keyInit=false;
      }
    }
    updater.interrupt();
  }
  public static synchronized void handleMouseEvent(MouseEvent e) {
    mouseGlobal.assign(Ref.mouseX / scaleGlobal, Ref.mouseY / scaleGlobal);
    if (Ref.mousePressed) {
      if (mouseState == STATE_PRESS) mouseState=STATE_PRESSED;
      if (mouseState == STATE_RELEASE || mouseState == STATE_RELEASED) {
        mouseState=STATE_PRESS;
        mouseClick.assign(mouseGlobal.x, mouseGlobal.y);
      }
    } else {
      if (mouseState == STATE_RELEASE) mouseState=STATE_RELEASED;
      if (mouseState == STATE_PRESS || mouseState == STATE_PRESSED) mouseState=STATE_RELEASE;
    }
    EventQueue.add(new ModEvent(ModEvent.MOUSE_EVENT, e));
    updater.interrupt();
  }
  //
  public static void invalidate(Rect rect){//adjust renderFlag.
    //asfasdfasasadfaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
  }

}
