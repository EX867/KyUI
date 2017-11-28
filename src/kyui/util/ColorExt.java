package kyui.util;
import kyui.core.KyUI;
import processing.core.PGraphics;

import java.awt.*;
public class ColorExt {
  public static int brighter(int a, int brighter) {
    if (KyUI.Ref.red(a) == 0 && KyUI.Ref.green(a) == 0 && KyUI.Ref.blue(a) == 0) return KyUI.Ref.color((int)Math.max(0, Math.min(255, brighter)));
    int b=Color.HSBtoRGB((float)KyUI.Ref.hue(a) / 255, (float)KyUI.Ref.saturation(a) / 255, (float)KyUI.Ref.max(0, KyUI.Ref.min(255, KyUI.Ref.brightness(a) + brighter)) / 255);
    return KyUI.Ref.color(KyUI.Ref.red(b), KyUI.Ref.green(b), KyUI.Ref.blue(b), KyUI.Ref.alpha(a));
  }
  public static int scale(int a, float black_ratio) {
    if (KyUI.Ref.red(a) == 0 && KyUI.Ref.green(a) == 0 && KyUI.Ref.blue(a) == 0) return (KyUI.Ref.floor(KyUI.Ref.max(0, KyUI.Ref.min(255, black_ratio))));
    int b=Color.HSBtoRGB((float)KyUI.Ref.hue(a) / 255, (float)KyUI.Ref.saturation(a) / 255, (float)KyUI.Ref.max(0, KyUI.Ref.min(255, KyUI.Ref.brightness(a) * black_ratio)) / 255);
    return KyUI.Ref.color(KyUI.Ref.red(b), KyUI.Ref.green(b), KyUI.Ref.blue(b), KyUI.Ref.alpha(a));
  }
  public static void fill(PGraphics g, int c) {
    g.fill(KyUI.Ref.red(c), KyUI.Ref.green(c), KyUI.Ref.blue(c), KyUI.Ref.alpha(c));
  }
}
