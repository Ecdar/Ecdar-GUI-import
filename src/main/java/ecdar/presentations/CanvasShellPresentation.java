package ecdar.presentations;

import com.jfoenix.controls.JFXRippler;
import ecdar.controllers.CanvasShellController;
import ecdar.utility.colors.Color;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

public class CanvasShellPresentation extends StackPane {
    private final CanvasShellController controller;

    public CanvasShellPresentation() {
        controller = new EcdarFXMLLoader().loadAndGetController("CanvasShellPresentation.fxml", this);

        initializeToolbar();
    }

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I700;

        // Set the background for the top toolbar
        controller.toolbar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));

        initializeToolbarButton(controller.zoomIn);
        initializeToolbarButton(controller.zoomOut);
        initializeToolbarButton(controller.zoomToFit);
        initializeToolbarButton(controller.resetZoom);
    }

    private void initializeToolbarButton(final JFXRippler button) {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        button.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        button.setRipplerFill(color.getTextColor(colorIntensity));
        button.setPosition(JFXRippler.RipplerPos.BACK);
    }

    public CanvasShellController getController() {
        return controller;
    }
}
