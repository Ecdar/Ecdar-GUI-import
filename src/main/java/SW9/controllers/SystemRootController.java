package SW9.controllers;

import SW9.Ecdar;
import SW9.abstractions.EcdarSystemEdge;
import SW9.abstractions.SystemModel;
import SW9.abstractions.SystemRoot;
import SW9.presentations.DropDownMenu;
import SW9.presentations.MenuElement;
import com.jfoenix.controls.JFXPopup;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for a system root.
 */
public class SystemRootController implements Initializable {
    public StackPane presentationRoot;
    public Polygon shape;
    public StackPane contextMenuContainer;
    public Group contextMenuSource;

    private SystemRoot systemRoot;
    private final ObjectProperty<SystemModel> system = new SimpleObjectProperty<>();

    private DropDownMenu contextMenu;
    private final BooleanProperty hasEdge = new SimpleBooleanProperty(false);


    // Properties

    public SystemRoot getSystemRoot() {
        return systemRoot;
    }

    public void setSystemRoot(final SystemRoot systemRoot) {
        this.systemRoot = systemRoot;
    }

    public SystemModel getSystem() {
        return system.get();
    }

    public void setSystem(final SystemModel system) {
        this.system.set(system);
    }


    // Initialization

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        system.addListener(((observable, oldValue, newValue) -> {
            initializeDropDownMenu(newValue);
        }));
    }

    private void initializeDropDownMenu(final SystemModel system) {
        contextMenu = new DropDownMenu(contextMenuContainer, contextMenuSource, 230, true);

        contextMenu.addMenuElement(new MenuElement("Draw Edge")
                .setClickable(() -> {
                    createNewSystemEdge();

                    contextMenu.close();
                })
                .setDisableable(hasEdge));
    }

    @FXML
    private void onMouseClicked(final MouseEvent event) {
        event.consume();

        final EcdarSystemEdge unfinishedEdge = getSystem().getUnfinishedEdge();

        if ((event.isShiftDown() && event.getButton().equals(MouseButton.PRIMARY)) || event.getButton().equals(MouseButton.MIDDLE)) {
            // If shift click or middle click a component instance, create a new edge

            // Component instance must not already have an edge and there cannot be any other unfinished edges in the system
            if (!hasEdge.get() && unfinishedEdge == null) {
                createNewSystemEdge();
            }
        }

        // if primary clicked and there is an unfinished edge, finish it with the system root as target
        if (unfinishedEdge != null && event.getButton().equals(MouseButton.PRIMARY)) {

            // If already has edge, give error
            if (hasEdge.get()) {
                Ecdar.showToast("This system root already has an edge.");
                return;
            }

            unfinishedEdge.setTarget(systemRoot);
            hasEdge.set(true);

            // Update state, if target changes,
            // so another edge can be created, if this component instance is no longer an edge target
            unfinishedEdge.getTargetProperty().addListener(((observable, oldValue, newValue) -> hasEdge.set(systemRoot.equals(newValue))));

            return;
        }

        // If secondary clicked, show context menu
        if (event.getButton().equals(MouseButton.SECONDARY)) {
            contextMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0, 0);
        }
    }

    /***
     * Helper method to create a new EcdarSystemEdge and add it to the current system and system root
     * @return The newly created EcdarSystemEdge
     */
    private EcdarSystemEdge createNewSystemEdge() {
        final EcdarSystemEdge edge = new EcdarSystemEdge(systemRoot);
        system.get().addEdge(edge);
        hasEdge.set(true);

        // If edge is removed or changes source, this instance updates it's hasEdge property so it can get a new edge
        edge.getSourceProperty().addListener(((observable, oldValue, newValue) -> hasEdge.set(systemRoot.equals(newValue))));
        return edge;
    }
}
