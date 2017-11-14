package kyui.element;
import kyui.core.Element;
import kyui.core.KyUI;
import kyui.util.EditorString;
import kyui.util.Rect;
import processing.core.PFont;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.EventListener;
public class TextEdit extends Element{
  RangeSlider slider;//if not null, it will work.
  EditorString content;
  PFont textFont;
  EventListener onTextChangeListener;
  ArrayList<Filter> filters;
  class Filter {
    String filter;
    public Filter(String filter_) {//filter is regex.
      filter=filter_;
    }
    String filter(String in) {
      if (in.contains(filter)) {
        return in.replaceAll(filter, "");
      } else {
        return in;
      }
    }
  }
  //modifiable values
  public String hint="";//this is one-line hint if text is empty.
  public int textSize=20;
  //temp values
  int clickLine=0;
  int clickPoint=0;
  float defaultOffsetY=0;
  float defaultOffsetX=0;
  float offsetY=0;
  public TextEdit(String name) {
    super(name);
    init();
  }
  public TextEdit(String name, Rect pos_) {
    super(name);
    pos=pos_;
    init();
  }
  private void init() {
    content=new EditorString();
    filters=new ArrayList<Filter>();
  }
  @Override
  public boolean mouseEvent(MouseEvent e, int index) {
    if (e.getAction() == MouseEvent.PRESS) {
      adjustCursor();//move cursor
      if(KyUI.shiftPressed){
        //ADD>>shift selection action
      }else {
        clickPoint=content.point;
        clickLine=content.line;
        content.resetSelection();
      }
    } else if (e.getAction() == MouseEvent.DRAG) {
      if (pressedL) {
        requestFocus();
        adjustCursor();
        content.select(clickLine, clickPoint, content.line, content.point);
      }
    } else if (e.getAction() == MouseEvent.RELEASE) {
      if (pressedL) {
        requestFocus();//because release action releases focus automatically.
      }
    }
  }
  void adjustCursor() {
    KyUI.cacheGraphics.textFont(textFont);
    KyUI.Ref.textSize(Math.max(1, textSize));//just using function...
    //set line...
    //set point...
    KyUI.cacheGraphics.textFont(KyUI.fontMain);
  }
  @Override
  public void keyEvent(KeyEvent e) {
    if (KyUI.focus == this) {
      //process shortcuts and insert!!
    }
  }
  public void insert(
  )
  public void updateSlider(){
    slider.setLength(listLayout.getTotalSize(), pos.bottom - pos.top);
    slider.setOffset(listLayout.getTotalSize(), listLayout.offset);
  }
    void insert(int point_, int line_, String text) {
      current.insert(point_, line_, text);
      moveTo(line_);
      updateSlider(sliderPos);
    }
    void addLine(int line_, String text) {
      current.addLine(line_, text);
      moveTo(line_);
      updateSlider(sliderPos);
    }
    void moveTo(int line) {
      float Yoffset=textSize/2-(sliderPos-(position.y-size.y+sliderLength))*(max(1, (current.lines()+10)*textSize-size.y*2)/max(1, size.y*2-sliderLength*2));
      int start=floor(max(0, min(current.lines()-1, (-Yoffset)/textSize)));//Yoffset+a*textSize>-textSize
      int end=floor((size.y*2-Yoffset)/textSize-3/2);
      if (line<start) {
        Yoffset=-(line-1/2)*textSize;
        sliderPos=(position.y-size.y+sliderLength)-(Yoffset-textSize/2)/(max(1, (current.lines()+10)*textSize-size.y*2)/max(1, size.y*2-sliderLength*2));
      } else if (line>end) {
        Yoffset=-(line+5/2)*textSize+size.y*2;
        sliderPos=(position.y-size.y+sliderLength)-(Yoffset-textSize/2)/(max(1, (current.lines()+10)*textSize-size.y*2)/max(1, size.y*2-sliderLength*2));
      }
      sliderPos=position.y+min(max(-size.y+sliderLength, sliderPos-position.y), size.y-sliderLength);//FIX
    }
    void moveToCursor() {
      moveTo(current.line);
    }
    //cursor controls
    void cursorLeft(boolean ctrl, boolean shift) {
      if (shiftPressed) current.selectionLeft(ctrl);
      else current.cursorLeft(ctrl, false);
    }
    void cursorRight(boolean ctrl, boolean shift) {
      if (shiftPressed) current.selectionRight(ctrl);
      else current.cursorRight(ctrl, false);
    }
    void cursorUp(boolean shift) {
      if (shift)current.selectionUp();
      else current.cursorUp(false);
    }
    void cursorUp(boolean ctrl, boolean shift) {
      if (ctrl) {
        int line=max(0, current.processer.DelayPoint.get(current.processer.getFrame(current.line)));
        while (current.line>line) {
          current.selectionUp();
        }
      } else cursorUp(shift);
    }
    void cursorDown(boolean shift) {
      if (shift)current.selectionDown();
      else current.cursorDown(false);
    }
    void cursorDown(boolean ctrl, boolean shift) {
      if (ctrl) {
        int index=current.processer.getFrame(current.line)+1;
        int line=current.lines();
        if (current.getCommands().get(current.line) instanceof DelayCommand)line=current.processer.DelayPoint.get(min(current.processer.DelayPoint.size()-1, index+1));
        else if (index<current.processer.DelayPoint.size())line=current.processer.DelayPoint.get(index);
        while (current.line<line) {
          cursorDown(shift);
        }
      } else cursorDown(shift);
    }
    void mouseWheel(MouseEvent e) {
      sliderPos=position.y+min(max(-size.y+sliderLength, sliderPos-position.y+e.getCount()*2000/max(current.lines()+8, size.y*2/textSize)), size.y-sliderLength);//HARDCODED!!!
    }
    @Override void render() {
      if (skip)return;
      textSize=((TextBox)UI[getUIid("I_TEXTSIZE")]).value;
      //draw basic form
      fill(UIcolors[I_TEXTBACKGROUND]);
      rect(position.x, position.y, size.x, size.y);
      fill(UIcolors[I_FOREGROUND]);
      rect(position.x-size.x+textSize*3/2-3, position.y, textSize*3/2-3, size.y);
      //setup text
      textAlign(LEFT, CENTER);
      textFont(fontRegular);
      textSize(max(1, textSize));
      textLeading(textSize/2);
      //iterate lines
      float Yoffset=textSize/2-(sliderPos-(position.y-size.y+sliderLength))*(max(1, (current.lines()+10)*textSize-size.y*2)/max(1, size.y*2-sliderLength*2));
      int start=floor(max(0, min(current.lines()-1, (-Yoffset)/textSize)));
      int end=(int)(size.y*2-textSize*3/2-Yoffset)/textSize;
      for (int a=start; a<current.lines()&&a<end; a++) {
        //draw selection
        if (current.hasSelection()) {
          fill(UIcolors[I_TEXTBOXSELECTION]);
          String selectionPart=current.getSelectionPart(a);
          if (selectionPart.equals("")==false) {
            if (selectionPart.charAt(selectionPart.length()-1)=='\n') {
              selectionPart=selectionPart.substring(0, selectionPart.length()-1);
              rect(position.x+textWidth(current.getSelectionPartBefore(a))/2+textSize+5, position.y-size.y+(a+1)*textSize+Yoffset+5, size.x-textWidth(current.getSelectionPartBefore(a))/2-textSize*2, textSize/2);
            } else rect(position.x-size.x+textSize*3+textWidth(selectionPart)/2+textWidth(current.getSelectionPartBefore(a))+5, position.y-size.y+(a+1)*textSize+3+Yoffset, textWidth(selectionPart)/2, textSize/2);
          }
        }
        //draw text
        String line=current.getLine(a);
        int commentPoint=line.length();//split comment
        for (int b=1; b<line.length(); b++) {
          if (line.charAt(b-1)=='/'&&line.charAt(b)=='/') {
            commentPoint=b-1;
            break;
          }
        }
        //split to tokens
        fill(UIcolors[I_GENERALTEXT]);
        text(content.getLine(a), position.x-size.x+textSize*3+textWidth(count)+5, position.y-size.y+(a+1)*textSize+Yoffset);
        if (a<current.lines()/*WARNING!!!*/&&commentPoint!=current.getLine(a).length()) {
          fill(UIcolors[I_COMMENTTEXT]);
          if (commentPoint<0)return;//warning!!!
          String showText=current.getLine(a).substring(commentPoint, current.getLine(a).length());
          text(showText, position.x-size.x+textSize*3+textWidth(current.getLine(a).substring(0, commentPoint))+5, position.y-size.y+(a+1)*textSize+Yoffset);
        }
      }
      fill(UIcolors[I_GENERALTEXT]);
      if (ID==focus)if (frameCount%54<36)if (Yoffset+current.line*textSize>-textSize&&Yoffset+current.line*textSize<size.y*2-textSize*3/2)text("|", position.x-size.x+textSize*3+textWidth(current.getLine(current.line).substring(0, min(current.getLine(current.line).length(), current.point)))+2, position.y-size.y+(current.line+1)*textSize+Yoffset);
      textAlign(RIGHT, CENTER);
      textFont(fontBold);
      textSize(max(1, textSize));
      textLeading(textSize/2);
      int a=floor(max(0, (-Yoffset)/textSize));//Yoffset+a*textSize>-textSize
      int loopstart=current.processer.getFrameByTime((int)frameSliderLoop.valueS);
      int loopend=current.processer.getFrameByTime((int)frameSliderLoop.valueE);
      int linecnt=current.lines();
      while (Yoffset+a*textSize<size.y*2-textSize*3/2) {//a<current.lines()+30) {
        if (a<linecnt) {
          float tempY=position.y-size.y+(a+1)*textSize+Yoffset;
          tempY=tempY-min(textSize/4, (tempY-position.y+size.y)/2)+textSize/4;
          tempY=tempY+min(textSize/4, (position.y+size.y-tempY)/2)-textSize/4;
          float tempSY= min(min(textSize/2, (tempY-position.y+size.y)), (position.y+size.y-tempY));
          if (frameSliderLoop.bypass==false) {
            if (current.processer.DelayPoint.get(loopstart)<a&&((loopstart<current.processer.DelayPoint.size()-1&&a<=current.processer.DelayPoint.get(loopstart+1))||loopstart>=current.processer.DelayPoint.size()-1)) {
              fill(255, 0, 0, 40);
              rect(position.x-size.x+textSize*3/2-3, tempY, textSize*3/2-3, tempSY);
            } else if (current.processer.DelayPoint.get(loopend)<a&&((loopend<current.processer.DelayPoint.size()-1&&a<=current.processer.DelayPoint.get(loopend+1))||loopend>=current.processer.DelayPoint.size()-1)) {
              fill(0, 0, 255, 40);
              rect(position.x-size.x+textSize*3/2-3, tempY, textSize*3/2-3, tempSY);
            }
          }
          if (current.processer.displayFrame<current.processer.DelayPoint.size()) {
            if ((current.processer.DelayPoint.get(current.processer.displayFrame)<a&&((current.processer.displayFrame<current.processer.DelayPoint.size()-1&&a<=current.processer.DelayPoint.get(current.processer.displayFrame+1)))||(current.processer.displayFrame==current.processer.DelayPoint.size()-1&&a>current.processer.DelayPoint.get(current.processer.displayFrame)))) {
              fill(0, 40);
              rect(position.x-size.x+textSize*3/2-3, tempY, textSize*3/2-3, tempSY);
            }
          }
          fill(brighter(UIcolors[I_BACKGROUND], -10));
        } else fill(brighter(UIcolors[I_BACKGROUND], 100));
        text(str(a), position.x-size.x+textSize*5/2, position.y-size.y+(a+1)*textSize+Yoffset);
        a=a+1;
      }
      textAlign(CENTER, CENTER);
      //draw rect
      color fillcolor=UIcolors[I_TEXTBACKGROUND];
      if (isMouseOn (position.x, position.y, size.x, size.y)&&mousePressed) {
        fillcolor=(brighter(UIcolors[I_TEXTBACKGROUND], -20));
        stroke(brighter(UIcolors [I_FOREGROUND], -40));
      } else {
        if (ID==focus) {
          fillcolor=(brighter(UIcolors[I_TEXTBACKGROUND], -20));
          stroke(brighter(UIcolors [I_FOREGROUND], -20));
        } else {
          stroke(UIcolors[I_FOREGROUND]);
        }
      }
      noFill();
      strokeWeight(3);
      rect(position.x, position.y, size.x, size.y);//second
      fill(fillcolor);
      rect(position.x+size.x-SLIDER_HALFWIDTH, position.y, SLIDER_HALFWIDTH, size.y);//slider holder
      fill(UIcolors[I_TEXTBACKGROUND]);
      rect(position.x+size.x-SLIDER_HALFWIDTH, sliderPos, SLIDER_HALFWIDTH-2, max(sliderLength-2, 2));
      line(position.x+size.x-SLIDER_HALFWIDTH*2, sliderPos, position.x+size.x, sliderPos);
}
