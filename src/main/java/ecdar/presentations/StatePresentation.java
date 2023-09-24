package ecdar.presentations;

import com.jfoenix.controls.JFXRippler;
import ecdar.abstractions.State;
import ecdar.controllers.StateController;
import ecdar.utility.colors.Color;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.function.BiConsumer;

/**
 * The presentation class for a state view element.
 * It represents a single state within the trace of the simulation
 */
public class StatePresentation extends AnchorPane {
    private final StateController controller;
    private FadeTransition transition;

    public StatePresentation() {
        controller = new EcdarFXMLLoader().loadAndGetController("StatePresentation.fxml", this);

        initializeRippler();
        initializeColors();
        initializeFadeAnimation();
    }

    public StatePresentation(State state) {
        this();
        controller.setState(state);
    }

    /**
     * Initializes the rippler.
     * Sets the color, mask and position of the rippler effect.
     */
    private void initializeRippler() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I400;

        controller.rippler.setMaskType(JFXRippler.RipplerMask.RECT);
        controller.rippler.setRipplerFill(color.getColor(colorIntensity));
        controller.rippler.setPosition(JFXRippler.RipplerPos.BACK);
    }

    /**
     * Initializes the colors of the view.
     * The background of the view changes colors depending on whether a mouse enters or exits the view.
     */
    private void initializeColors() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I50;

        final BiConsumer<Color, Color.Intensity> setBackground = (newColor, newIntensity) -> {
            setBackground(new Background(new BackgroundFill(
                    newColor.getColor(newIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            setBorder(new Border(new BorderStroke(
                    newColor.getColor(newIntensity.next(2)),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 0, 1, 0)
            )));
        };

        // Update the background when hovered
        setOnMouseEntered(event -> {
            setBackground.accept(color, colorIntensity.next());
            setCursor(Cursor.HAND);
        });

        // Update the background when the mouse exits
        setOnMouseExited(event -> {
            setBackground.accept(color, colorIntensity);
            setCursor(Cursor.DEFAULT);
        });

        // Update the background initially
        setBackground.accept(color, colorIntensity);
    }

    private void initializeFadeAnimation() {
        this.transition = new FadeTransition(Duration.millis(500), this);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.setInterpolator(Interpolator.EASE_IN);
    }

    public void playFadeAnimation() {
        this.transition.play();
    }

    public StateController getController() {
        return controller;
    }
}
