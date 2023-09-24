package ecdar.controllers;

import ecdar.abstractions.Edge;
import ecdar.abstractions.Decision;
import ecdar.presentations.DecisionPresentation;
import ecdar.utility.colors.Color;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller class for the right pane in the simulator
 */
public class RightSimPaneController implements Initializable {
    public StackPane root;
    public VBox availableDecisionsVBox;

    private Consumer<Decision> onDecisionSelected;
    private final ObservableList<DecisionPresentation> availableDecisions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeBackground();
        initializeLeftBorder();
        initializeDecisionHandling();
    }

    /**
     * Sets the background color of the ScrollPane Vbox
     */
    private void initializeBackground() {
        availableDecisionsVBox.setBackground(new Background(new BackgroundFill(
                Color.GREY.getColor(Color.Intensity.I200),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
    }

    /**
     * Initializes the thin border on the left side of the querypane toolbar
     */
    private void initializeLeftBorder() {
        root.setBorder(new Border(new BorderStroke(
                Color.GREY_BLUE.getColor(Color.Intensity.I900),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 1)
        )));
    }

    private void initializeDecisionHandling() {
        availableDecisions.addListener((ListChangeListener<DecisionPresentation>) c -> {
            while (c.next()) {
                c.getRemoved().forEach(decisionPresentation -> Platform.runLater(() -> availableDecisionsVBox.getChildren().remove(decisionPresentation)));

                c.getAddedSubList().forEach(decisionPresentation -> {
                    // Request next step when decision is clicked
                    decisionPresentation.setOnMouseClicked(event -> onDecisionSelected.accept(decisionPresentation.getController().getDecision()));
                    Platform.runLater(() -> availableDecisionsVBox.getChildren().add(decisionPresentation));
                });
            }
        });
    }

    public void setOnDecisionSelected(Consumer<Decision> decisionSelected) {
        onDecisionSelected = decisionSelected;
    }

    public void setDecisionsList(List<DecisionPresentation> decisions) {
        availableDecisions.setAll(decisions);
    }

    protected List<Decision> getDecisions() {
        return availableDecisions.stream()
                .map(decisionPresentation -> decisionPresentation.getController().getDecision())
                .collect(Collectors.toList());
    }
}