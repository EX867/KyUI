package kyui.core;

import kyui.event.listeners.OnPrepareListener;

/**
 * Frame is acted like ViewGroup.
 * @author EX867
 *
 */
public class Frame extends Element {// Element...?
  protected OnPrepareListener onPrepareListener;
  public Frame(String name) {
    super(name);
  }
  public void setOnPrepareListener(OnPrepareListener onPrepareListener_) {
    onPrepareListener = onPrepareListener_;
  }
  public void prepare() {
    if (onPrepareListener != null) onPrepareListener.onPrepare();
  }
}
