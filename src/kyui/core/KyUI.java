package kyui.core;
import kyui.event.DropEventListener;
import kyui.event.EventListener;
import kyui.event.FileDropEventListener;
import kyui.event.ResizeListener;
import kyui.loader.ElementLoader;
import kyui.util.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.data.XML;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import sojamo.drop.DropEvent;
import sojamo.drop.DropListener;
import sojamo.drop.SDrop;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
//===To ADD list===//
//ADD>>optimize mouseEvent and rendering chain!! especially clipping...
//[Editor]ADD>>search elements in editor (later)
//ADD>>match_parent and layout attribute
//FIX>>refactor loaders!!! generalize colorVariable to value.
public class KyUI {
  //
  public static PApplet Ref;
  private static boolean ready = false;
  private static boolean end = false;
  public static boolean log = true;
  public static boolean externalLog = false;
  static boolean MONO_THREAD = false;
  // object control
  protected static final HashMap<String, Element> Elements = new HashMap<String, Element>();
  protected static LinkedList<CachingFrame> roots = new LinkedList<CachingFrame>();//no support multi window.
  public static LinkedList<ImageElement> imageElements = new LinkedList<>();
  public static Element focus = null;
  // events
  public static LinkedList<Event> EventQueue = new LinkedList<Event>();//items popped from update thread.
  public static TaskManager taskManager = new TaskManager();//do it in here... add task directly to here. (in Element)
  //
  static class ModifyLayerTask implements Task {
    @Override
    public void execute(Object data_raw) {
      if (data_raw == null) {//remove
        if (roots.size() <= 1) {
          System.err.println("there is some error!");
        } else {
          roots.pollLast();
          roots.peekLast().renderFlag = true;
        }
      } else {
        CachingFrame root = (CachingFrame)data_raw;
        roots.addLast(root);
        //if (!root.pos.getSize().equals(new Vector2(Ref.width, Ref.height))) {
        //  root.resize(Ref.width, Ref.height);
        //}
        addElement(root);
        root.invalidate();
        //root.localLayout();
      }
    }
  }
  static ModifyLayerTask modifyLayerTask = new ModifyLayerTask();//task for this object.
  static LinkedList<ResizeListener> resizeListeners = new LinkedList<>();
  public static Consumer<String> logEvent;
  //mouse
  public static final int STATE_PRESS = 1;
  public static final int STATE_PRESSED = 2;
  public static final int STATE_RELEASE = 3;
  public static final int STATE_RELEASED = 4;
  public static int mouseState = STATE_RELEASED;//no multi touch
  public static int mouseClickAfterTime = 0;
  static long mouseEventTime = Long.MAX_VALUE;//this is last mouse event's time.
  public static LinkedList<Vector2> mouseClick = new LinkedList<>();// this parameter stores mouse click position.
  public static LinkedList<Vector2> mouseGlobal = new LinkedList<>();// this parameter stores global mouse position.
  static {
    mouseClick.add(new Vector2());
    mouseGlobal.add(new Vector2());
  }
  public static int DOUBLE_CLICK_INTERVAL = 400;
  public static int GESTURE_THRESHOLD = 13;
  public static int WHEEL_COUNT = 25;
  public static HashMap<ElementPair, DropEventListener> dropEvents = new HashMap<>();//dnd - internal
  public static DropMessenger dropMessenger;//
  public static CachingFrame dropLayer;
  public static HashMap<Element, FileDropEventListener> dropEventsExternal = new HashMap<>();//dnd - external
  static CachingFrame dropLayerExternal;
  static SDrop drop;//drop from outside is handled not like drop between elements...this is my limit of abstraction...
  public static boolean draggingExternal = false;//used when checking this moueEvent is external drag and drop.
  static class CheckOverlayTask implements Task {
    @Override
    public void execute(Object data_) {
      if (data_ instanceof DropEvent) {
        DropEvent de = (DropEvent)data_;
        mouseGlobal.getLast().set(de.x() / scaleGlobal, de.y() / scaleGlobal);
        Element target = roots.getLast().checkOverlayCondition(roots.getLast().pos, mouseGlobal.getLast(), Transform.identity, (Element e, Vector2 pos) -> {
          return (KyUI.dropEventsExternal.containsKey(e));
        });//if overlay, setup overlay.
        if (target != null) {
          KyUI.log("drop target : " + target.getName());
          dropEventsExternal.get(target).onEvent(de);
        }
      }
    }
  }
  static CheckOverlayTask checkOverlayTask = new CheckOverlayTask();
  //key
  public static int KEY_INIT_DELAY = 1000;// you can change this valueI.
  public static int KEY_INTERVAL = 300;
  public static boolean ctrlPressed = false;
  public static boolean shiftPressed = false;
  public static boolean altPressed = false;
  public static boolean keyState = false;
  public static long keyEventTime = 0;
  public static boolean keyInit = false;// this used on textEdit and etc...
  protected static List<Long> reflectedPressedKeys;
  // shortcuts
  public static HashMap<String, Shortcut> shortcutsByName = new HashMap<String, Shortcut>(199);
  public static LinkedList<Shortcut> shortcuts = new LinkedList<Shortcut>();
  // graphics
  //public static boolean DRAW_ALL_LAYERS=false;
  public static PGraphics cacheGraphics;
  public static long drawStart = 0;// these 3 parameters used to measure elapsed time.
  public static long drawEnd = 0;
  public static long drawInterval = 0;
  public static float scaleGlobal = 1.0F;// this parameter stores global window scale.
  public static PFont fontMain;//set manually! (so public)
  public static PFont fontText;//set manually! (2)
  public static int INF = 987654321;
  static int DESCRIPTION_THRESHOLD = 500;
  static CachingFrame descriptionLayer;
  static Description currentDescription = null;
  static boolean canShowDescription = true;
  static Description descriptionDefault;
  static BiFunction<Element, Vector2, Boolean> descriptionCheck = (Element e, Vector2 pos) -> {
    return e.description != null;
  };
  //thread
  public static Updater updater;
  public static Thread updaterThread;
  public static int updater_interval;
  public static long frameCount;
  //public Thread animation;
  //temp
  private static int pwidth;
  private static int pheight;
  private static int count = 0;
  private KyUI() {//WARNING! names must not contains ':' and "->".
  }
  static java.awt.Panel panel = null;
  public static void start(PApplet ref) {
    start(ref, 30, true);
  }
  @SuppressWarnings("unchecked")
  public static void start(PApplet ref, int rate, boolean mono) {
    if (ready) return;// this makes setup() only called once.
    MONO_THREAD = mono;
    Ref = ref;
    try {
      //      //keyRepeat
      //      Field f=Ref.getClass().getDeclaredField("keyRepeatEnabled");
      //      f.setAccessible(true);//Very important, this allows the setting to work.
      //      f.set(Ref, false);
      //canvas
      if (Ref.getSurface() instanceof processing.awt.PSurfaceAWT) {
        Field f = Ref.getSurface().getClass().getDeclaredField("canvas");
        f.setAccessible(true);//Very important, this allows the setting to work.
        java.awt.Component cp = (java.awt.Component)f.get(Ref.getSurface());
        drop = new SDrop(cp);
        cp.addComponentListener(new ComponentListener() {
          @Override
          public void componentResized(ComponentEvent e) {
            //System.out.println(0);
            if (updater == null) {
              return;
            }
            int w = e.getComponent().getSize().width;
            int h = e.getComponent().getSize().height;
            class ResizeTask implements Task {
              public void execute(Object o) {
                synchronized (updater.resizeLock) {
                  for (CachingFrame root : roots) {
                    root.pos.set(0, 0, w, h);
                    root.resize(w, h);
                  }
                  if (!roots.contains(descriptionLayer)) {
                    descriptionLayer.pos.set(0, 0, w, h);
                    descriptionLayer.resize(w, h);
                  }
                }
                for (ResizeListener resizeListener : resizeListeners) {
                  resizeListener.onEvent(w, h);
                }
                //System.out.println(2);
              }
            }
            synchronized (taskManager) {
              ListIterator<TaskManager.TaskSet> iter = taskManager.getTaskSet().listIterator();
              while (iter.hasNext()) {
                if (iter.next().task instanceof ResizeTask) {
                  iter.remove();
                }
              }
            }
            taskManager.addTask(new ResizeTask(), null);
            //System.out.println(1);
          }
          @Override
          public void componentMoved(ComponentEvent e) {
          }
          @Override
          public void componentShown(ComponentEvent e) {
          }
          @Override
          public void componentHidden(ComponentEvent e) {
          }
        });
      } else if (Ref.getSurface() instanceof processing.opengl.PSurfaceJOGL) {
        //surface have component called canvas but sadly primary graphics is not awt based.
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (drop != null) {
      drop.addDropListener(new DropListener() {
        @Override
        public void dragExit() {
          draggingExternal = false;
          handleEvent(new MouseEvent(null, 0, MouseEvent.MOVE, 0, -1, -1, PApplet.LEFT, 1));
        }
        @Override
        public void update(float x, float y) {
          draggingExternal = true;
          handleEvent(new MouseEvent(null, 0, MouseEvent.MOVE, 0, (int)(x / scaleGlobal), (int)(y / scaleGlobal), PApplet.LEFT, 1));
        }
        @Override
        public void dropEvent(DropEvent de) {
          draggingExternal = false;//not synchronized with updater so release don't works, but it is real time!
          handleEvent(new MouseEvent(null, 0, MouseEvent.RELEASE, 0, (int)(de.x() / scaleGlobal), (int)(de.y() / scaleGlobal), PApplet.LEFT, 1));
          taskManager.addTask(checkOverlayTask, de);
        }
      });
    }
    pwidth = Ref.width;
    pheight = Ref.height;
    //other things
    fontMain = ElementLoader.loadFontResource("SourceCodePro-Bold.ttf", 20);
    fontText = ElementLoader.loadFontResource("The160.ttf", 20);
    cacheGraphics = KyUI.Ref.createGraphics(10, 10, KyUI.Ref.sketchRenderer());//small graphics...used for some functions
    dropLayer = new CachingFrame("KyUI:dropLayer", new Rect(0, 0, Ref.width, Ref.height));
    descriptionLayer = new CachingFrame("KyUI:descriptionLayer", new Rect(0, 0, Ref.width, Ref.height));
    descriptionDefault = new Description("KyUI:description:start");
    descriptionLayer.addChild(descriptionDefault);
    if (roots.size() == 0)
      addLayer(getNewLayer());
    try {
      Field pressedKeys;
      pressedKeys = PApplet.class.getDeclaredField("pressedKeys");
      pressedKeys.setAccessible(true);
      reflectedPressedKeys = (List<Long>)pressedKeys.get(Ref);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ready = true;
    updater = new Updater();
    updaterThread = new Thread(updater);
    frameRate(rate);
    frameCount = 0;
    cacheGraphics.beginDraw();
    if (!MONO_THREAD) {
      updaterThread.start();
    }
  }
  public static void end() {
    end = true;
  }
  public static void addResizeListener(ResizeListener e) {
    resizeListeners.add(e);
  }
  public static boolean isRootPresent(CachingFrame e) {
    return roots.contains(e);
  }
  public static void frameRate(int rate) {//update thread frame rate.
    updater_interval = 1000 / rate;
  }
  public static void addLayer(CachingFrame root) {
    root.clear();
    taskManager.addTask(modifyLayerTask, root);
  }
  public static void removeLayer() {
    taskManager.executeAll();
    taskManager.addTask(modifyLayerTask, null);
  }
  public static CachingFrame getNewLayer() {
    return new CachingFrame("KyUI:" + (count++), new Rect(0, 0, Ref.width, Ref.height));
  }
  protected static void addElement(Element e) {
    if (e instanceof ImageElement) {
      imageElements.addLast((ImageElement)e);
    }
    if (!Elements.containsKey(e.name)) {
      Elements.put(e.name, e);
    } else {
      if (e != Elements.get(e.name) && !e.name.equals("KyUI:messenger")) {//messenger always share same name...
        KyUI.err("try to add existing name. (" + e.name + ") type : " + e.getClass().getTypeName() + ", exists : " + Elements.get(e.name).getClass().getTypeName());
        int a = 0;
        while (Elements.containsKey(e.name + a)) {//add number to avoid duplication
          a++;
        }
        KyUI.log("renamed new element. name :" + e.name + " -> " + (e.name + a));
        e.name = e.name + a;
        Elements.put(e.name, e);
      }
    }
  }
  public static boolean rename(Element e, String name) {
    if (e.name.equals(name)) {
      return true;
    }
    if (!Elements.containsKey(name)) {
      Elements.remove(e.name);
      e.name = name;
      Elements.put(name, e);
      return true;
    } else {
      err("try to rename to existing name. (" + e.name + " to " + name + ")");
      return false;
    }
  }
  public static void removeElement(String name) {
    Element e = Elements.remove(name);
    if (e != null && e instanceof ImageElement) {
      imageElements.remove(e);
    }
  }
  public static void add(Element object) {
    taskManager.executeAll();//because this need latest state.
    roots.getLast().addChild(object);
  }
  public static Element get(String name) {
    taskManager.executeAll();//because this need latest state.
    return Elements.get(name);
  }
  public static <Type> Type get2(String name) {
    taskManager.executeAll();//because this need latest state.
    return (Type)Elements.get(name);
  }
  public static CachingFrame getRoot() {
    return roots.getLast();
  }
  // called by processing animation thread
  public static void render(PGraphics g) {
    drawStart = drawEnd;
    g.imageMode(PApplet.CENTER);
    g.rectMode(PApplet.CORNERS);
    g.scale(KyUI.scaleGlobal);
    //synchronized (updater) {//no multi thread!!!
    boolean started = false;
    for (int a = 0; a < roots.size(); a++) {
      CachingFrame root = roots.get(a);
      if (started) {
        if (root.alpha != 0) {
          g.fill(0, root.alpha);
          g.rect(0, 0, g.width, g.height);
        }
      }
      root.renderReal(g);//render all...
      started = true;
      //}
      descriptionLayer.renderReal(g);
    }
    drawEnd = System.currentTimeMillis();
    drawInterval = drawEnd - drawStart;
    if (MONO_THREAD) {
      KyUI.updater.loop();
    }
  }
  public static class Updater implements Runnable {
    public Object resizeLock = new Object();
    @Override
    public void run() {
      while (!end) {
        loop();
        try {
          Thread.sleep(updater_interval);
        } catch (InterruptedException e) {
        }
      }
      cacheGraphics.endDraw();
    }
    public void loop() {
      synchronized (this) {
        synchronized (resizeLock) {
          //System.out.println("start "+frameCount);
          //empty EventQueue.
          while (EventQueue.size() > 0) {
            Event e = EventQueue.pollFirst();
            if (e instanceof KeyEvent) {
              keyEvent((KeyEvent)e);
            } else if (e instanceof MouseEvent) {
              mouseEvent((MouseEvent)e);
            }
          }
          KyUI.taskManager.executeAll();
          roots.getLast().update_();
          //rendering
          for (int a = 0; a < roots.size(); a++) {
            roots.get(a).render_(null);
          }
          //description
          if (currentDescription == null && canShowDescription && System.currentTimeMillis() - mouseEventTime > DESCRIPTION_THRESHOLD) {
            Element el = roots.getLast().checkOverlayCondition(roots.getLast().pos, mouseGlobal.getLast(), Transform.identity, descriptionCheck);
            if (el != null) {
              descriptionLayer.setTransform(Element.overlayCondition_transform);
              currentDescription = el.description;
              descriptionLayer.children.set(0, currentDescription);
              descriptionLayer.invalidate();
              currentDescription.onShow();
              descriptionLayer.render_(null);
            }
            canShowDescription = false;
          } else if (currentDescription != null && System.currentTimeMillis() - mouseEventTime < DESCRIPTION_THRESHOLD) {
            descriptionLayer.children.set(0, descriptionDefault);
            descriptionLayer.clear();
            currentDescription = null;
          }
        }
      }
      //System.out.println("end "+frameCount);
      frameCount++;
    }
  }
  //
  public static void preventFromExit(PApplet sketch, KeyEvent event) {
    if (event.getAction() == KeyEvent.PRESS) {
      if (sketch.key == PApplet.ESC) {
        sketch.key = 0;
      }
    }
  }
  public static void handleEvent(Event e) {
    EventQueue.addLast(e);
  }
  static void keyEvent(KeyEvent e) {
    if (Ref.key == PApplet.ESC) {
      Ref.key = 0; // Fools! don't let them escape!
    }
    if (e.getKey() == PApplet.CODED) {
      if (e.getAction() == KeyEvent.PRESS) {//handle first.
        if (e.getKeyCode() == PApplet.CONTROL) ctrlPressed = true;//???
        else if (e.getKeyCode() == PApplet.SHIFT) shiftPressed = true;
        else if (e.getKeyCode() == PApplet.ALT) altPressed = true;
      } else if (e.getAction() == KeyEvent.RELEASE) {
        if (e.getKeyCode() == PApplet.CONTROL) ctrlPressed = false;
        else if (e.getKeyCode() == PApplet.SHIFT) shiftPressed = false;
        else if (e.getKeyCode() == PApplet.ALT) altPressed = false;
      }
    }
    Long hash = (long)e.getKeyCode() << 16 | (long)e.getKey();
    if (e.getAction() == MouseEvent.PRESS) {//FIX>>meke only key Press executed once!!
      for (Shortcut shortcut : shortcuts) {
        if (shortcut.isPressed(e)) {
          if (shortcut.event != null) {
            shortcut.event.onEvent(focus);//??? nothing to send...
          }
        }
      }
    }
    roots.getLast().keyEvent_((KeyEvent)e);
    if (e.getAction() == KeyEvent.PRESS) {
      roots.getLast().keyTyped_((KeyEvent)e);
      keyInit = false;
      keyState = true;
      keyEventTime = System.currentTimeMillis();
    } else {
      if (!keyState) {
        roots.getLast().keyTyped_((KeyEvent)e);
        keyState = true;
      }
    }
    if (keyInit) {
      if (System.currentTimeMillis() - keyEventTime > KEY_INTERVAL) {
        keyState = false;
        keyEventTime = System.currentTimeMillis();
      }
    } else {
      if (System.currentTimeMillis() - keyEventTime > KEY_INIT_DELAY) {
        keyInit = true;
        keyState = false;
        keyEventTime = System.currentTimeMillis();
      }
    }
    if (e.getAction() == KeyEvent.RELEASE) {
      if (reflectedPressedKeys.isEmpty()) {
        keyState = false;
        keyInit = false;
        keyEventTime = 0;
      }
    }
    //updater.interrupt();
  }
  static void mouseEvent(MouseEvent e) {
    mouseGlobal.getLast().set(e.getX() / scaleGlobal, e.getY() / scaleGlobal);
    if (Ref.mousePressed) {
      if (mouseState == STATE_PRESS) mouseState = STATE_PRESSED;
      if (mouseState == STATE_RELEASE || mouseState == STATE_RELEASED) {
        mouseState = STATE_PRESS;
        mouseClick.getLast().set(mouseGlobal.getLast().x, mouseGlobal.getLast().y);
      }
    } else {
      if (mouseState == STATE_RELEASE) mouseState = STATE_RELEASED;
      if (mouseState == STATE_PRESS || mouseState == STATE_PRESSED) mouseState = STATE_RELEASE;
    }
    if (e.getAction() == MouseEvent.EXIT) {
      mouseGlobal.getLast().set(-1, -1);//make no element contains this.
    }
    if (roots.size() > 0) {
      roots.getLast().mouseEvent_(e, roots.size() - 1, true);
    }
    if (e.getAction() == MouseEvent.MOVE) {
      canShowDescription = true;
    } else {
      canShowDescription = false;
    }
    mouseEventTime = System.currentTimeMillis();
    //updater.interrupt();
  }
  public static int getKeyCount() {
    return reflectedPressedKeys.size();
  }
  //
  public static Element checkOverlayCondition(BiFunction<Element, Vector2, Boolean> cond) {
    return roots.getLast().checkOverlayCondition(roots.getLast().pos, mouseGlobal.getFirst(), Transform.identity, cond);
  }
  public static void changeLayout() {
    taskManager.executeAll();
    roots.getLast().onLayout();
    roots.getLast().invalidate();
    taskManager.executeAll();
  }
  public static void invalidate(Rect rect) {//adjust renderFlag.
    taskManager.addTask((n) -> {
      if (roots.isEmpty()) return;
      roots.getLast().checkInvalid(rect, roots.getLast().pos, Transform.identity);
    }, null);
  }
  public static void invalidateElement(Element e) {//adjust renderFlag.
    taskManager.addTask((n) -> {
      if (roots.isEmpty()) return;
      roots.getLast().invalidated = true;
      e.renderFlag = true;
    }, null);
  }
  public static void dropStart(Element start_, MouseEvent startEvent_, int startIndex_, String message_, String displayText_) {
    dropMessenger = new DropMessenger("KyUI:messenger", start_, startEvent_, startIndex_, message_, displayText_, roots.getLast());
    dropMessenger.setVisual(start_.dropVisual);
    dropLayer.addChild(dropMessenger);
    addLayer(dropLayer);
    taskManager.addTask((n) -> {
      handleEvent(startEvent_);
    }, null);
  }
  public static boolean isDropEnd(Element end) {
    if (!end.droppableEnd) return false;
    return getDropEvent(end) != null;
  }
  public static void dropEnd(Element end, MouseEvent endEvent, int endIndex) {
    if (!end.droppableEnd) return;//this is : ignoring. so please check this thing
    if (dropMessenger != null) {
      DropEventListener de = getDropEvent(end);
      if (de != null) {
        DropMessenger dm = dropMessenger;
        taskManager.addTask((n) -> {
          de.onEvent(dm, endEvent, endIndex);
        }, null);
      }
      dropMessenger = null;
    }
  }
  static class ElementPair {
    Element start;
    Element end;
    ElementPair(Element start_, Element end_) {
      start = start_;
      end = end_;
    }
    @Override
    public int hashCode() {
      return start.hashCode() + end.hashCode() * 17;
    }
    @Override
    public boolean equals(Object other) {
      if (other instanceof ElementPair) {
        ElementPair e = (ElementPair)other;
        return e.start == start && e.end == end;
      }
      return false;
    }
  }
  public static void addDragAndDrop(Element start, Element end, DropEventListener listener) {
    dropEvents.put(new ElementPair(start, end), listener);
    start.droppableStart = true;
    end.droppableEnd = true;
  }
  public static void addDragAndDrop(Element end, FileDropEventListener listener) {
    dropEventsExternal.put(end, listener);
  }
  public static DropEventListener getDropEvent(Element end) {
    if (dropMessenger == null) return null;
    return dropEvents.get(new ElementPair(dropMessenger.start, end));
  }
  public static class Shortcut {
    public Key key;
    public EventListener event;
    public String name = "nothing";
    public Shortcut(String name_, boolean ctrl_, boolean alt_, boolean shift_, int key_, int keyCode_, EventListener event_) {
      name = name_;
      key = new Key(ctrl_, alt_, shift_, key_, keyCode_);
      event = event_;
    }
    public Shortcut(String name_, Key key_, EventListener event_) {
      name = name_;
      set(key_);
      event = event_;
    }
    public void set(Key key_) {
      //set(key.ctrl, key.alt, key.shift, key.key, key.keyCode, event);
      key = key_;
    }
    public void set(boolean ctrl_, boolean alt_, boolean shift_, int key_, int keyCode_, EventListener event_) {
      key.set(ctrl_, alt_, shift_, key_, keyCode_);
      event = event_;
    }
    boolean isPressed(KeyEvent e) {
      return key.isPressed(e);
    }
    @Override
    public String toString() {
      return name + "   " + key.toString();
    }
    public XML toXML() {
      if (key == null) {
        return null;
      }
      XML data = new XML("shortcut");
      data.setString("ctrl", "" + key.ctrl);
      data.setString("alt", "" + key.alt);
      data.setString("shift", "" + key.shift);
      data.setString("key", "" + key.key);
      data.setString("keyCode", "" + key.keyCode);
      data.setContent(name);
      return data;
    }
  }
  public static class Key {
    public boolean ctrl = false;
    public boolean alt = false;
    public boolean shift = false;
    public int key = -1;
    public int keyCode = 0;
    public Key(boolean ctrl_, boolean alt_, boolean shift_, int key_, int keyCode_) {
      set(ctrl_, alt_, shift_, key_, keyCode_);
    }
    public void set(boolean ctrl_, boolean alt_, boolean shift_, int key_, int keyCode_) {
      ctrl = ctrl_;
      alt = alt_;
      shift = shift_;
      key = key_;
      keyCode = keyCode_;
    }
    boolean isPressed(KeyEvent e) {
      return (ctrl == ctrlPressed && alt == altPressed && shift == shiftPressed && e.getKey() == key && e.getKeyCode() == keyCode);
    }
    @Override
    public String toString() {
      String ret = "[";
      if (ctrl) ret += "Ctrl+";
      if (alt) ret += "Alt+";
      if (shift) ret += "Shift+";
      if (key != 0 && (key != PApplet.CODED || (key == PApplet.CODED && keyCode != PApplet.CONTROL && keyCode != PApplet.ALT && keyCode != PApplet.SHIFT))) {
        //set key.
        String realKey = "" + (char)key;
        if (key == '\t') ret += "Tab";
        else if (key == ' ') ret += "Space";
        else if (key == PApplet.ENTER) ret += "Enter";
        else if (key == PApplet.BACKSPACE) ret += "Backspace";
        else if (key == PApplet.DELETE) ret += "Delete";
        else if (key == PApplet.ESC) ret += "ESC (None)";
        else if (key == 65535 && keyCode == 54) {// Exception!(really, not needed.)
          ret += "6";
        } else if (ctrl || alt || key == PApplet.CODED) {
          ret += java.awt.event.KeyEvent.getKeyText(keyCode);
        } else {
          ret += realKey;
        }
      }
      return ret + "]";
    }
  }
  public static void addShortcut(Shortcut shortcut) {
    shortcutsByName.put(shortcut.name, shortcut);//overwrited.
    shortcuts.add(shortcut);
  }
  static java.io.PrintWriter write;
  public static void log(String text) {
    if (!log) return;
    System.out.println("[KyUI] " + text);
    if (logEvent != null) {
      logEvent.accept(text);
    }
    if (externalLog) {
      if (write == null) {
        write = Ref.createWriter("log.txt");
      }
      write.write("[out] " + text + "\n");
      write.flush();
    }
  }
  public static void err(String text) {
    if (!log) return;
    System.err.println("[KyUI : " + frameCount + "] " + text);
    if (logEvent != null) {
      logEvent.accept(text);
    }
    if (externalLog) {
      if (write == null) {
        write = Ref.createWriter("log.txt");
      }
      write.write("[err] " + text + "\n");
      write.flush();
    }
  }
}
