

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class FontLineMetricsDump {
  private static final float EPSILON = 1e-6f;
  
  private static final int[] SIZES = {
      6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30,
      32, 34, 36, 40, 44, 48, 56, 64, 72, 94, 144, 288};

  private void run(PrintWriter writer) throws IOException {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Image img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) img.getGraphics();
    FontRenderContext frc = g2.getFontRenderContext();
    writer.println("{");

    Font[] fonts = ge.getAllFonts();
    for (int i = 0; i < fonts.length; i++) {
      Font font = fonts[i];
      String name = font.getFontName(Locale.US);
      float height = 0;
      float leading = 0;
      float ascent = 0;
      float descent = 0;
      float leadingSizeRatio = 0;
      float ascentSizeRatio = 0;
      float descentSizeRatio = 0;
      float heightSizeRatio = 0;
      for (int j = 0; j < SIZES.length; j++) {
        int size = SIZES[j];
  
        Font f = font.deriveFont((float) size);
        LineMetrics lm = f.getLineMetrics("A", frc);
        if (j == 0) {
          height = lm.getHeight();
          leading = lm.getLeading();
          ascent = lm.getAscent();
          descent = lm.getDescent();
          leadingSizeRatio = leading / size;
          ascentSizeRatio = ascent / size;
          descentSizeRatio = descent / size;
          heightSizeRatio = height / size;
          
          if (!equal(height, leading + ascent + descent)) {
            System.err.println("name=" + name + ", size=" + size + ", height=" + lm.getHeight() +
                ", ascent=" + lm.getAscent() + ", descent=" + lm.getDescent() +
                ", leading=" + lm.getLeading() + ", height != ascent + descent + leading.");
          }
          String sep = i < fonts.length - 1 ? "," : "";
//          writer.println("\"" + name + "\":{a:" + format(lm.getAscent()) + ",d:" + format(lm.getDescent()) +
//              ",l:" + format(lm.getLeading()) + ",hs:" + format(heightSizeRatio) + "}" + sep);
          writer.println("\"" + name + "\":{h:" + format(heightSizeRatio) + ",a:" + format(ascentSizeRatio) +
              ",d:" + format(descentSizeRatio) + "}" + sep);
        }
        else {
          if (!equal(lm.getLeading() / size, leadingSizeRatio) ||
            !equal(lm.getAscent() / size, ascentSizeRatio) ||
            !equal(lm.getDescent() / size, descentSizeRatio)) {
            System.err.println("name=" + name + ", size=" + size + ", height=" + lm.getHeight() +
                ", ascent=" + lm.getAscent() + ", descent=" + lm.getDescent() +
                ", leading=" + lm.getLeading() + ", different ratio for different size!");
          }

          if (!equal(lm.getHeight() / size, heightSizeRatio)) {
            System.err.println("name=" + name + ", size=" + size + ", height=" + lm.getHeight() +
                ", different height/size for different size!");
          }
        }
      }
    }
    writer.println("}");
  }

  private boolean equal(float v1, float v2) {
    return Math.abs(v1 - v2) < EPSILON;
  }

  private String format(float value) {
    String result = String.format("%f", value).replaceFirst("\\.?0*$", "");
    return "-0".equals(result) ? "0" : result;
  }

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("fontmetrics.js")));
    try {
      new FontLineMetricsDump().run(writer);
    } finally {
      writer.close();
    }
  }
}
