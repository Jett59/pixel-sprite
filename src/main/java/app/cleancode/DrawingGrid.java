package app.cleancode;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class DrawingGrid {
  private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  private static double GRID_LENGTH = Math.min(screenSize.width, screenSize.height);

  public static record Context(int width, int height) {

  }

  private static ObjectProperty<Context> createContext(IntegerProperty width,
      IntegerProperty height) {
    ObjectProperty<Context> result =
        new SimpleObjectProperty<Context>(new Context(width.get(), height.get()));
    width.addListener((observable, oldValue, newValue) -> {
      result.set(new Context(newValue.intValue(), height.get()));
    });
    height.addListener((observable, oldValue, newValue) -> {
      result.set(new Context(width.get(), newValue.intValue()));
    });
    return result;
  }

  private BufferedImage image;
  private BufferedImage opennedImage;
  private Graphics imageGraphics;
  private Group grid = new Group();
  private ObjectProperty<Context> context;

  public DrawingGrid(IntegerProperty width, IntegerProperty height, StringProperty color) {
    (context = createContext(width, height)).addListener((observable, oldContext, context) -> {
      if (imageGraphics != null) {
        imageGraphics.dispose();
        imageGraphics = null;
      }
      grid.getChildren().clear();
      if (context.width != 0 && context.height != 0) {
        if (opennedImage == null) {
          image = new BufferedImage(context.width, context.height, BufferedImage.TYPE_INT_ARGB);
        } else {
          image = opennedImage;
          opennedImage = null;
        }
        imageGraphics = image.getGraphics();
        double gridCellLength = Math.min(GRID_LENGTH / context.width, GRID_LENGTH / context.height);
        double startingX = (screenSize.width / 2d) - (gridCellLength * (context.width / 2d));
        double startingY = (screenSize.height / 2d) - (gridCellLength * (context.height / 2d));
        for (int pixelY = 0; pixelY < context.height; pixelY++) {
          double yOffset = pixelY * gridCellLength;
          for (int pixelX = 0; pixelX < context.width; pixelX++) {
            double xOffset = pixelX * gridCellLength;
            Rectangle cell = new Rectangle(startingX + xOffset, startingY + yOffset, gridCellLength,
                gridCellLength);
            grid.getChildren().add(cell);
            int initialColorARGB = image.getRGB(pixelX, pixelY);
            var awtColor = new java.awt.Color(initialColorARGB, true);
            Color initialColor = new Color(awtColor.getRed() / 255d, awtColor.getGreen() / 255d,
                awtColor.getBlue() / 255d, awtColor.getAlpha() / 255d);
            cell.setFill(initialColor);
            if (initialColor.getOpacity() > 0) {
              cell.setStroke(new Color(1 - initialColor.getRed(), 1 - initialColor.getGreen(),
                  1 - initialColor.getBlue(), 1));
            } else {
              cell.setStroke(Color.BLACK);
            }
            cell.setStrokeWidth(5);
            // To get rid of "local variable declared in an enclosing scope must be final or
            // effectively final" message.
            int pixelXCopy = pixelX;
            int pixelYCopy = pixelY;
            cell.setOnMouseClicked(event -> {
              Color fillColor = Color.valueOf(color.get());
              cell.setFill(fillColor);
              if (fillColor.getOpacity() > 0) {
                cell.setStroke(new Color(1 - fillColor.getRed(), 1 - fillColor.getGreen(),
                    1 - fillColor.getBlue(), 1));
              } else {
                cell.setStroke(Color.BLACK);
              }
              imageGraphics.setColor(new java.awt.Color((float) fillColor.getRed(),
                  (float) fillColor.getGreen(), (float) fillColor.getBlue()));
              imageGraphics.drawRect(pixelXCopy, pixelYCopy, 0, 0);
            });
          }
        }
      }
    });
  }

  public Group getNode() {
    return grid;
  }

  public void save() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG files", "*.PNG"));
    File output = fileChooser.showSaveDialog(new Stage());
    if (output != null) {
      try {
        ImageIO.write(image, "png", output);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void open() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG files", "*.PNG"));
    File output = fileChooser.showOpenDialog(new Stage());
    if (output != null) {
      try {
        BufferedImage image = ImageIO.read(output);
        opennedImage = image;
        context.set(new Context(image.getWidth(), image.getHeight()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
