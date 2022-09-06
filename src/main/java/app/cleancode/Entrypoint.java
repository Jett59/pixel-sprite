package app.cleancode;

import java.util.function.Consumer;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Entrypoint extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  private static ChangeListener<String> getNumberTextChangeListener(IntegerProperty numberProperty,
      Consumer<String> textSetter) {
    return (observable, oldValue, newValue) -> {
      if (!newValue.isEmpty()) {
        try {
          numberProperty.set(Integer.parseInt(newValue));
        } catch (NumberFormatException e) {
          textSetter.accept(oldValue);
        }
      } else {
        numberProperty.set(0);
      }
    };
  }

  private static void createLabeledControl(Pane pane, String label, Node control) {
    Label controlLabel = new Label(label);
    controlLabel.setLabelFor(control);
    pane.getChildren().add(controlLabel);
    pane.getChildren().add(control);
  }

  private static void createNumberTextField(Pane pane, String label,
      IntegerProperty numberProperty) {
    TextField textField = new TextField();
    textField.textProperty()
        .addListener(getNumberTextChangeListener(numberProperty, textField::setText));
    createLabeledControl(pane, label, textField);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    IntegerProperty widthProperty = new SimpleIntegerProperty();
    IntegerProperty heightProperty = new SimpleIntegerProperty();
    TextField colorField = new TextField();
    var colorProperty = colorField.textProperty();
    var rootPane = new Group();
    var controlsPane = new FlowPane();
    controlsPane.setOrientation(Orientation.VERTICAL);
    rootPane.getChildren().add(controlsPane);
    createNumberTextField(controlsPane, "Width", widthProperty);
    createNumberTextField(controlsPane, "Height", heightProperty);
    createLabeledControl(controlsPane, "Color", colorField);

    DrawingGrid grid = new DrawingGrid(widthProperty, heightProperty, colorProperty);
    rootPane.getChildren().add(grid.getNode());
    Button saveButton = new Button("Save");
    saveButton.setOnAction(event -> grid.save());
    controlsPane.getChildren().add(saveButton);
    Button openButton = new Button("Open");
    openButton.setOnAction(event -> grid.open());
    controlsPane.getChildren().add(openButton);
    var scene = new Scene(rootPane);
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    primaryStage.show();
  }
}
