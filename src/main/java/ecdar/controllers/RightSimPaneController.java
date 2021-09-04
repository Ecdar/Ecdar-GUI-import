package ecdar.controllers;

import ecdar.presentations.QueryPanePresentation;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the right pane in the simulator
 */
public class RightSimPaneController implements Initializable {
    public StackPane root;
    public VBox scrollPaneVbox;
    public QueryPanePresentation queryPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}