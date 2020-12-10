package com.auzeill.regex.graph;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import static java.lang.Integer.parseInt;

public class GeneratePng {

  public enum Format {
    L(stringLiteral -> RegexTreeGraph.transform(stringLiteral, true, true, true), ".legend.png"),
    P(PatternGraph::transform, ".pattern.png"),
    T(stringLiteral -> RegexTreeGraph.transform(stringLiteral, true, false, false), ".tree.png"),
    A(stringLiteral -> RegexTreeGraph.transform(stringLiteral, false, true, false),".state.png"),
    TA(stringLiteral -> RegexTreeGraph.transform(stringLiteral, true, true, false), ".tree-state.png");

    public final UnaryOperator<String> transform;
    public final String fileSuffix;

    Format(UnaryOperator<String> transform, String fileSuffix) {
      this.transform = transform;
      this.fileSuffix = fileSuffix;
    }

    public static List<Format> valuesWithoutLegend() {
      return Arrays.stream(values()).filter(e -> e != L).collect(Collectors.toList());
    }
  }

  private static final String MARGIN = ",(?<left>\\d++),(?<right>\\d++),(?<top>\\d++),(?<bottom>\\d++)";
  private static final Pattern IMAGE_DESC_REGEX = Pattern.compile("(?<filename>[^:]++):(?<format>L|P|T|A|TA)(" + MARGIN + ")?:(?<literal>.*+)", Pattern.DOTALL);

  public static void generate(Path workingDir, Path imageListPath, PrintStream out, PrintStream err) throws IOException, InterruptedException {
    for (String line : FileUtils.readFile(imageListPath).split("(?m)$")) {
      line = line.trim();
      if (!line.isEmpty() && !line.startsWith("#")) {
        Matcher matcher = IMAGE_DESC_REGEX.matcher(line);
        if (matcher.matches()) {
          Path pngFile = workingDir.resolve(matcher.group("filename"));
          createPng(out, pngFile, Format.valueOf(matcher.group("format")), matcher.group("literal"));
          if (matcher.group("left") != null) {
            cropImage(out, pngFile,
              parseInt(matcher.group("left")),
              parseInt(matcher.group("right")),
              parseInt(matcher.group("top")),
              parseInt(matcher.group("bottom")));
          }
        } else {
          err.println("[ERROR] Invalid format: " + line);
        }
      }
    }
  }

  public static void createPng(PrintStream out, Path pngFile, Format format, String regex) throws IOException, InterruptedException {
    out.println("[INFO] create png for " + regex + ", format: " + format + " target: " + pngFile.getFileName());
    String dot = format.transform.apply(regex);
    Files.write(pngFile, Dot.generate(dot, Dot.Type.PNG));
  }

  public static void cropImage(PrintStream out, Path pngFile, int marginLeft, int marginRight, int marginTop, int marginBottom) throws IOException {
    out.println("[INFO] crop " + pngFile.getFileName());
    BufferedImage srcImage = ImageIO.read(pngFile.toFile());
    int newWidth = srcImage.getWidth() - marginLeft - marginRight;
    int newHeight = srcImage.getHeight() - marginTop - marginBottom;
    BufferedImage dstImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    dstImage.getGraphics().drawImage(srcImage, 0, 0, newWidth, newHeight, marginLeft, marginTop, marginLeft + newWidth, marginTop + newHeight, null);
    ImageIO.write(dstImage, "png", pngFile.toFile());
  }

}
