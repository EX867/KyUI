package kyui.core;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import kyui.util.Rect;
import kyui.util.Vector2;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.event.Event;
import processing.event.MouseEvent;
import processing.event.KeyEvent;
//===To ADD list===//
//ADD>>optimize mouseEvent and rendering chain!!
//ADD>>resizing functions**
//ADD>>name duplication error
public class KyUI {
  //
  public static PApplet Ref;
  private static boolean ready=false;
  private static boolean end=false;
  // object control
  protected static final HashMap<String, Element> Elements=new HashMap<String, Element>();
  protected static LinkedList<CachingFrame> roots=new LinkedList<CachingFrame>();//no support multi window.
  public static Element focus=null;
  // events
  public static LinkedList<Event> EventQueue=new LinkedList<Event>();//items popped from update thread.
  //
  public static final int STATE_PRESS=1;
  public static final int STATE_PRESSED=2;
  public static final int STATE_RELEASE=3;
  public static final int STATE_RELEASED=4;
  public static int mouseState=STATE_RELEASED;//no multi touch
  public static int mouseTime=0;// this parameter indicates the time after mouse clicked.
  public static Vector2 mouseClick=new Vector2();// this parameter stores mouse click position.
  public static Vector2 mouseGlobal=new Vector2();// this parameter stores global mouse position.(scaled)
  public static final int GESTURE_THRESHOLD=13;
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
  public static LinkedList<Rect> clipArea=new LinkedList<Rect>();
  public static long drawStart=0;// these 3 parameters used to measure elapsed time.
  public static long drawEnd=0;
  public static long drawInterval=0;
  public static float scaleGlobal=1.0F;// this parameter stores global window scale.
  public static PFont fontMain;//set manually! (so public)
  public static int INF=987654321;
  //thread
  public static Thread updater;
  public static int updater_interval;
  //public Thread animation;
  private KyUI() {//WARNING! names must not contains ':'.
  }
  public static void start(PApplet ref) {
    start(ref, 30);
  }
  @SuppressWarnings("unchecked")
  public static void start(PApplet ref, int rate) {
    if (ready) return;// this makes setup() only called once.
    Ref=ref;
    fontMain=KyUI.Ref.createFont(new java.io.File("data/SourceCodePro-Bold.ttf").getAbsolutePath(), 20);
    if (roots.size() == 0) addLayer(getNewLayer());
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
  public static void end() {
    end=true;
  }
  //  public static void resize() {
  //    roots.getLast().setPosition(new Rect(0, 0, Ref.width, Ref.height));
  //  }
  public static void frameRate(int rate) {//update thread frame rate.
    updater_interval=1000 / rate;
  }
  public static void addLayer(CachingFrame root) {
    roots.addLast(root);
  }
  public static void removeLayer() {
    roots.pollLast();
    roots.peekLast().renderFlag=true;
  }
  public static CachingFrame getNewLayer() {
    return new CachingFrame("KyUI:" + roots.size(), new Rect(0, 0, Ref.width, Ref.height));
  }
  protected static void addElement(Element object) {
    Elements.put(object.getName(), object);
  }
  public static void removeElement(String name) {
    Elements.remove(name);
  }
  public static void add(Element object) {
    roots.getLast().addChild(object);
  }
  public static Element get(String name) {
    return Elements.get(name);
  }
  // called by processing animation thread
  public static void update() {
    roots.getLast().update_();
  }
  public static void render(PGraphics g) {
    drawStart=drawEnd;
    g.imageMode(PApplet.CENTER);
    for (Element root : roots) {
      root.render_(g);//render all...
    }
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
          Event e=EventQueue.pollFirst();
          if (e instanceof KeyEvent) {
            keyEvent((KeyEvent)e);
          } else if (e instanceof MouseEvent) {
            mouseEvent((MouseEvent)e);
          }
        }
        //
        roots.getLast().update_();
      }
    }
  }
  //
  public static void handleEvent(Event e) {
    EventQueue.addLast(e);
  }
  static void keyEvent(KeyEvent e) {// FIX >> This code is unstable. test and fix!
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
    roots.getLast().keyEvent_((KeyEvent)e);
    if (!keyState) roots.getLast().keyTyped_((KeyEvent)e);
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
    //updater.interrupt();
  }
  static void mouseEvent(MouseEvent e) {
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
    if (e.getAction() == MouseEvent.EXIT) {
      mouseGlobal.assign(-1, -1);//make no element contains this.
    }
    roots.getLast().mouseEvent_(e, roots.size() - 1);
    //    int a=roots.size() - 1;
    //    while (a >= 0 && roots.get(a).mouseEvent_(e, a)) {
    //      a--;
    //    }
    //updater.interrupt();
  }
  //
  public static void changeLayout() {
    roots.getLast().onLayout();
  }
  public static void invalidate(Rect rect) {//adjust renderFlag.
    roots.getLast().checkInvalid(rect);
  }
  public static void clipRect(PGraphics g, Rect rect) {
    g.imageMode(PApplet.CORNERS);
    g.clip(rect.left, rect.top, rect.right, rect.bottom);
    g.imageMode(PApplet.CENTER);
    clipArea.add(rect);
  }
  public static void removeClip(PGraphics g) {
    if (clipArea.size() > 0) {
      clipArea.removeLast();
    }
    if (clipArea.size() == 0) {
      g.noClip();
    } else {
      g.clip(clipArea.getLast().left, clipArea.getLast().top, clipArea.getLast().right, clipArea.getLast().bottom);
    }
  }
}
