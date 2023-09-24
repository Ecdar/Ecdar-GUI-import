package ecdar.utility.helpers;

import ecdar.Ecdar;
import ecdar.controllers.EcdarController;
import ecdar.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class MouseCircular implements Circular {
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty originalX = new SimpleDoubleProperty(0d);
    private final DoubleProperty originalY = new SimpleDoubleProperty(0d);
    private final DoubleProperty originalMouseX = new SimpleDoubleProperty(0d);
    private final DoubleProperty originalMouseY = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(10);
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(1d);
    private final MouseTracker mouseTracker = Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasPresentation().mouseTracker;

    public MouseCircular(Circular initLocation) {
        //Set the initial x and y coordinates of the circular
        originalX.set(initLocation.getX());
        originalY.set(initLocation.getY());
        x.set(initLocation.getX());
        y.set(initLocation.getY());
        originalMouseX.set(mouseTracker.xProperty().get());
        originalMouseY.set(mouseTracker.yProperty().get());

        mouseTracker.registerOnMouseMovedEventHandler(event -> updatePosition());
        mouseTracker.registerOnMouseDraggedEventHandler(event -> updatePosition());

        // If the component is dragged while we are drawing an edge, update the coordinates accordingly
        Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasPresentation().getController().modelPane.translateXProperty().addListener((observable, oldValue, newValue) -> originalX.set(
                originalX.get() - (newValue.doubleValue() - oldValue.doubleValue()) / Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasZoomFactor().get()));
        Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasPresentation().getController().modelPane.translateYProperty().addListener((observable, oldValue, newValue) -> originalY.set(
                originalY.get() - (newValue.doubleValue() - oldValue.doubleValue()) / Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasZoomFactor().get()));
    }

    private void updatePosition() {
        final double dragDistanceX = mouseTracker.xProperty().get() - originalMouseX.get();
        final double dragDistanceY = mouseTracker.yProperty().get() - originalMouseY.get();

        x.set(originalX.get() + dragDistanceX / Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasZoomFactor().get());
        y.set(originalY.get() + dragDistanceY / Ecdar.getPresentation().getController().getEditorPresentation().getController().getActiveCanvasZoomFactor().get());
    }

    @Override
    public DoubleProperty radiusProperty() {
        return radius;
    }

    @Override
    public DoubleProperty scaleProperty() {
        return scale;
    }

    @Override
    public DoubleProperty xProperty() {
        return x;
    }

    @Override
    public DoubleProperty yProperty() {
        return y;
    }

    @Override
    public double getX() {
        return x.get();
    }

    @Override
    public double getY() {
        return y.get();
    }
}
