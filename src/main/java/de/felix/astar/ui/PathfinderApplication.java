/*
 * Developed by Felix on 09.03.19 12:37.
 *
 * Copyright (C) 2019. All rights reserved.
 */

package de.felix.astar.ui;

import de.felix.astar.algorithm.INodeUpdateListener;
import de.felix.astar.algorithm.Node;
import de.felix.astar.algorithm.NodeType;
import de.felix.astar.algorithm.Pathfinder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * A JavaFX Application that visualizes the interaction with the {@link Pathfinder} class.
 *
 * @author Felix
 */
public class PathfinderApplication extends Application {

    //Constants

    /**
     * The size of each rectangle representing a node.
     */
    private static final int RECT_SIZE = 50;

    /**
     * The default delay that will be waited after the update of each node to simulate the pathfinder.
     */
    private static final int DEFAULT_UPDATE_DELAY = 100;

    /**
     * The padding of the texts containing the f-, g- and h-cost.
     */
    private static final Insets COSTS_PADDING = new Insets(2);

    /**
     * The margin of each control-UI element.
     */
    private static final Insets CONTROL_MARGIN = new Insets(3);

    /**
     * The font for the g- and h-costs.
     */
    private static final Font COSTS_FONT = new Font("Arial", RECT_SIZE * 0.2);

    /**
     * The font for the g-cost.
     */
    private static final Font F_COST_FONT = Font.font(COSTS_FONT.getStyle(), FontWeight.BOLD, COSTS_FONT.getSize());

    /**
     * The Font for each control-UI element.
     */
    private static final Font CONTROL_FONT = Font.font("Arial", FontWeight.BOLD, 12);

    /**
     * The Font for each control-UI element.
     */
    private static final Font STATUS_FONT = Font.font("Arial", FontWeight.BOLD, 20);

    //Attributes

    /**
     * The Java-FX root containing all (JavaFX-)Nodes.
     */
    private Group root;

    /**
     * Stores the stage that is currently active.
     */
    private Stage stage;

    /**
     * The {@link Pathfinder} instance to use in order to execute the algorithm.
     */
    private Pathfinder pathfinder;

    /**
     * The thread in which the {@link Pathfinder#findPath()}-Method will be executed.
     */
    private Thread pathfinderThread;

    /**
     * Stores the latest key that is currently pressed.
     * This is used to check if the 'S' or 'E' key is pressed when click to set the start or end point.
     */
    private KeyCode pressingKey = null;

    /**
     * The delay to wait before each Node Update in order to simulate the A* algorithm.
     */
    private long nodeUpdateDelay = DEFAULT_UPDATE_DELAY;

    /**
     * Whether or not diagonal movement is allowed.
     */
    private boolean allowDiagonal = true;

    /**
     * Stores the current {@link Status} of the UI.
     */
    private Status status = Status.EDITING;

    /**
     * UI label representing the current {@link Status}.
     */
    private Label statusLabel;

    //Methods

    /**
     * Initializes the user interface and sets up keyboard and mouse listeners.
     *
     * @param primaryStage the window created by JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        //Set stage property
        this.stage = primaryStage;

        //Set title
        primaryStage.setTitle("A* Visualization");

        //Set size settings
        primaryStage.setWidth(960);
        primaryStage.setHeight(540);
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(360);
        primaryStage.setMaxWidth(2560);
        primaryStage.setMaxHeight(1440);
        primaryStage.setResizable(true);

        //Make pressing X forces the close and kills all threads
        primaryStage.setOnCloseRequest(event -> System.exit(0));

        //Initialize UI elements
        initialize();

        //Add key listeners to stage
        this.stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> this.pressingKey = event.getCode());
        this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> this.pressingKey = null);

        //Show the stage
        primaryStage.show();
    }

    /**
     * Starts the pathfinding algorithm or resumes it if it is paused.
     */
    private void startPathfinding() {
        if (status == Status.PAUSED) {
            pathfinderThread.resume();
            setStatus(Status.RUNNING);
            return;
        }

        pathfinderThread = new Thread(() -> {
            if (pathfinder.findPath() != null) {
                Platform.runLater(() -> setStatus(Status.COMPLETED));
            } else {
                Platform.runLater(() -> setStatus(Status.FAILED));
            }
        });
        pathfinderThread.start();
        setStatus(Status.RUNNING);
    }

    /**
     * Pauses the {@link PathfinderApplication#pathfinderThread}, if currently running.
     */
    private void pausePathfinding() {
        if (status == Status.RUNNING) {
            this.pathfinderThread.suspend();
            setStatus(Status.PAUSED);
        }
    }

    /**
     * Changes the status that is displayed in the UI.
     *
     * @param newStatus the new status.
     */
    private void setStatus(Status newStatus) {
        this.status = newStatus;
        this.statusLabel.setText(status.name);
    }

    /**
     * Resets the grid, while keeping start-, end and barrier-nodes.
     */
    private void reset() {
        Node[][] oldGrid = pathfinder.getGrid();
        reinitialize();

        for (int gridX = 0; gridX < pathfinder.getGrid().length; gridX++) {
            for (int gridY = 0; gridY < pathfinder.getGrid()[gridX].length; gridY++) {
                Node oldNode = oldGrid[gridX][gridY];
                Node newNode = pathfinder.getGrid()[gridX][gridY];

                if (!pathfinder.isInsideGrid(oldNode.getX(), oldNode.getY())) {
                    continue;
                }

                if (oldNode.getNodeType() == NodeType.BARRIER || oldNode.getNodeType() == NodeType.START || oldNode.getNodeType() == NodeType.END) {
                    pathfinder.setNodeType(newNode, oldNode.getNodeType());
                }
            }
        }
    }

    /**
     * Stops the pathfinder, removes all UI components and calls {@link PathfinderApplication#initialize()} again.
     */
    private void reinitialize() {
        //Dispose old objects
        if (this.pathfinderThread != null) {
            this.pathfinderThread.stop();
        }
        this.pathfinderThread = null;
        this.pathfinder = null;
        this.root.getChildren().clear();
        this.root = null;

        //Call garbage collector to avoid memory leak
        System.gc();

        //Load the UI again and set the settings correctly
        initialize();
        pathfinder.setDiagonal(this.allowDiagonal);
    }

    /**
     * Creates a new instance of {@link Pathfinder}, creates the visual grid and initializes the click ui.
     */
    private void initialize() {
        //Create the root group
        this.root = new Group();

        //Black background
        AnchorPane pane = new AnchorPane();
        pane.setPrefWidth(stage.getMaxWidth());
        pane.setPrefHeight(stage.getMaxHeight());
        pane.setStyle("-fx-background-color: #000000");
        this.root.getChildren().add(pane);

        //Add the scene
        this.stage.setScene(new Scene(root));

        //Create the pathfinder instance
        this.pathfinder = new Pathfinder((int) Math.ceil(stage.getMaxWidth() / RECT_SIZE), (int) Math.ceil(stage.getMaxHeight() / RECT_SIZE), true);

        //Draw nodes
        int visualX = 0;
        for (int gridX = 0; gridX < pathfinder.getGrid().length; gridX++) {
            int visualY = 0;
            for (int gridY = 0; gridY < pathfinder.getGrid()[gridX].length; gridY++) {
                Node node = pathfinder.getGrid()[gridX][gridY];
                createNodeVisual(node, visualX, visualY);
                visualY += RECT_SIZE;
            }
            visualX += RECT_SIZE;
        }

        //Add mouse listeners
        this.root.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseEvent);
        this.root.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseEvent);

        //Load Click UI
        initializeClickUI();
    }

    /**
     * Called when a mouse event occurs.
     *
     * @param event the {@link MouseEvent} instance.
     */
    private void handleMouseEvent(MouseEvent event) {
        if (status != Status.EDITING) return;

        //Calculate coordinates
        int sceneX = (int) event.getSceneX();
        int sceneY = (int) event.getSceneY();
        int x = (sceneX - (sceneX % RECT_SIZE)) / RECT_SIZE;
        int y = (sceneY - (sceneY % RECT_SIZE)) / RECT_SIZE;

        //Ignore if click was outside the grid
        if (!pathfinder.isInsideGrid(x, y)) {
            return;
        }

        //Get the current node
        Node node = pathfinder.getGrid()[x][y];

        //Take action according to button.
        if (event.getButton() == MouseButton.PRIMARY) {
            if (pressingKey != null) {
                if (pressingKey == KeyCode.S) {
                    //Set start node if S is pressed while clicking
                    pathfinder.setNodeType(node, NodeType.START);
                    return;
                } else if (pressingKey == KeyCode.E) {
                    //Set end node if E is pressed while clicking
                    pathfinder.setNodeType(node, NodeType.END);
                    return;
                }
            }
            //Set barrier if clicked
            pathfinder.setNodeType(node, NodeType.BARRIER);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            //Set to default if right clicked.
            pathfinder.setNodeType(node, NodeType.UNEVALUATED);
        }

    }

    /**
     * Creates a UI which allows the user to control the algorithm.
     * The user can use buttons and sliders in order to start, stop and
     * change the speed of the algorithm.
     */
    private void initializeClickUI() {
        //The transparent background
        Rectangle transparentBox = new Rectangle(230, 130);
        transparentBox.setLayoutX(5);
        transparentBox.setLayoutY(5);
        transparentBox.setFill(Color.rgb(0, 0, 0, 0.5));
        root.getChildren().add(transparentBox);

        //Pane holding all control elements
        VBox controlBox = new VBox();
        controlBox.setLayoutX(5);
        controlBox.setLayoutY(5);
        controlBox.setPadding(new Insets(5));
        root.getChildren().add(controlBox);

        //Diagonal checkbox
        CheckBox diagonalCheckbox = new CheckBox();

        //HBox with all control buttons (start,pause,reset)
        {
            HBox buttonBox = new HBox();

            //Start button
            Button startButton = new Button("Start");
            startButton.setFocusTraversable(false);
            startButton.setOnMouseClicked(event -> {
                if (status == Status.EDITING || status == Status.PAUSED) {
                    if (pathfinder.getStartNode() == null || pathfinder.getEndNode() == null) {
                        return;
                    }
                    startPathfinding();
                    diagonalCheckbox.setDisable(true);
                    startButton.setText("Pause");
                } else if (status == Status.RUNNING) {
                    pausePathfinding();
                    startButton.setText("Resume");
                }
            });
            HBox.setMargin(startButton, CONTROL_MARGIN);

            //Reset button
            Button resetButton = new Button("Reset");
            resetButton.setFocusTraversable(false);
            resetButton.setOnMouseClicked(event -> {
                reset();
                setStatus(Status.EDITING);
            });
            HBox.setMargin(resetButton, CONTROL_MARGIN);

            //Clear button
            Button clearButton = new Button("Clear");
            clearButton.setFocusTraversable(false);
            clearButton.setOnMouseClicked(event -> {
                reinitialize();
                setStatus(Status.EDITING);
            });
            HBox.setMargin(clearButton, CONTROL_MARGIN);

            //Add all buttons
            buttonBox.getChildren().addAll(startButton, resetButton, clearButton);
            VBox.setMargin(buttonBox, CONTROL_MARGIN);
            controlBox.getChildren().add(buttonBox);
        }

        //HBox with delay slider and label
        {
            HBox delayBox = new HBox();

            Label sliderLabel = new Label("Delay: ");
            sliderLabel.setTextFill(Color.WHITE);
            HBox.setMargin(sliderLabel, CONTROL_MARGIN);

            Slider slider = new Slider(0, 200, nodeUpdateDelay);
            slider.setFocusTraversable(false);
            slider.valueProperty().addListener((observable, oldValue, newValue) -> this.nodeUpdateDelay = newValue.longValue());
            HBox.setMargin(slider, CONTROL_MARGIN);

            delayBox.getChildren().addAll(sliderLabel, slider);
            VBox.setMargin(delayBox, CONTROL_MARGIN);
            controlBox.getChildren().add(delayBox);
        }

        //HBox with diagonal checkbox and label
        {
            HBox diagonalBox = new HBox();
            Label checkBoxLabel = new Label("Diagonal: ");
            checkBoxLabel.setFont(CONTROL_FONT);
            checkBoxLabel.setTextFill(Color.WHITE);
            HBox.setMargin(checkBoxLabel, CONTROL_MARGIN);
            diagonalCheckbox.setFocusTraversable(false);
            diagonalCheckbox.setSelected(allowDiagonal);
            diagonalCheckbox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                this.allowDiagonal = newValue;
                this.pathfinder.setDiagonal(newValue);
            }));

            diagonalBox.getChildren().addAll(checkBoxLabel, diagonalCheckbox);
            VBox.setMargin(diagonalBox, CONTROL_MARGIN);
            controlBox.getChildren().add(diagonalBox);
        }


        //HBox representing the current status
        {
            HBox statusBox = new HBox();

            Label statusPreLabel = new Label("Status: ");
            statusPreLabel.setTextFill(Color.WHITE);
            statusPreLabel.setFont(STATUS_FONT);
            HBox.setMargin(statusPreLabel, CONTROL_MARGIN);

            statusLabel = new Label(status.getName());
            statusLabel.setTextFill(Color.WHITE);
            statusLabel.setFont(STATUS_FONT);
            HBox.setMargin(statusLabel, CONTROL_MARGIN);

            statusBox.getChildren().addAll(statusPreLabel, statusLabel);
            VBox.setMargin(statusBox, CONTROL_MARGIN);
            controlBox.getChildren().add(statusBox);
        }
    }

    /**
     * Creates a visual representation of a node.
     *
     * @param node the node to represent.
     * @param x    the visual x-coordinate.
     * @param y    the visual y-coordinate.
     */
    private void createNodeVisual(Node node, int x, int y) {
        //Create pane that represents one node
        StackPane nodePane = new StackPane();

        //Rect for the color
        Rectangle nodeRect = new Rectangle(RECT_SIZE - 1, RECT_SIZE - 1, Color.LIGHTGRAY);

        //Texts for the f, g, and h costs
        Text fCostText = new Text();
        Text gCostText = new Text();
        Text hCostText = new Text();

        //V- and HBoxes to correctly align the texts
        VBox fVBox = new VBox();
        VBox hVBox = new VBox();
        HBox hHBox = new HBox();
        VBox gVBox = new VBox();
        HBox gHBox = new HBox();

        //Set coordinates of the pane
        nodePane.setLayoutX(x);
        nodePane.setLayoutY(y);

        //Set the padding of the texts
        fVBox.setPadding(COSTS_PADDING);
        gVBox.setPadding(COSTS_PADDING);
        hVBox.setPadding(COSTS_PADDING);


        //Set the fonts
        fCostText.setFont(F_COST_FONT);
        gCostText.setFont(COSTS_FONT);
        hCostText.setFont(COSTS_FONT);

        //Add all children and set alignments
        fVBox.getChildren().add(fCostText);
        fVBox.setAlignment(Pos.TOP_LEFT);

        gHBox.getChildren().add(gCostText);
        gVBox.setAlignment(Pos.BOTTOM_LEFT);
        gHBox.setAlignment(Pos.BOTTOM_LEFT);
        gVBox.getChildren().add(gHBox);

        hHBox.getChildren().add(hCostText);
        hVBox.setAlignment(Pos.BOTTOM_RIGHT);
        hHBox.setAlignment(Pos.BOTTOM_RIGHT);
        hVBox.getChildren().add(hHBox);

        nodePane.getChildren().add(nodeRect);
        nodePane.getChildren().addAll(fVBox, gVBox, hVBox);
        root.getChildren().add(nodePane);

        //Add the node update listener to change the text when the costs change
        node.setListener(new NodeUpdateListener(nodeRect, fCostText, gCostText, hCostText));
    }

    /**
     * The status enum represents the current status of the algorithm in the ui.
     *
     * @author Felix
     */
    private enum Status {
        EDITING("Editing"), PAUSED("Paused"), RUNNING("Running"), COMPLETED("Path found"), FAILED("No path found");

        /**
         * The display name of the status.
         */
        private String name;

        /**
         * Creates a status with a corresponding display name.
         *
         * @param name the display name of the status.
         */
        Status(String name) {
            this.name = name;
        }

        /**
         * Whether or not the algorithm is active, i.e. the status is {@link Status#RUNNING} or {@link Status#PAUSED}
         *
         * @return true, if active, otherwise false.
         */
        public boolean isActive() {
            return this == RUNNING || this == PAUSED;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Implementation of the {@link INodeUpdateListener} that updates the visual representations of every node if one is updated.
     * Note that this class is not in its own file because it needs access to the {@link PathfinderApplication#nodeUpdateDelay} and the {@link PathfinderApplication#status} fields.
     *
     * @author Felix
     */
    private class NodeUpdateListener implements INodeUpdateListener {
        private Rectangle rectangle;
        private Text fCostText;
        private Text gCostText;
        private Text hCostText;

        NodeUpdateListener(Rectangle rectangle, Text fCostText, Text gCostText, Text hCostText) {
            this.rectangle = rectangle;
            this.fCostText = fCostText;
            this.gCostText = gCostText;
            this.hCostText = hCostText;
        }

        @Override
        public void onUpdate(Node node) {
            //Sleep for the delay, if present and the algorithm is running
            if (nodeUpdateDelay > 0 && status.isActive()) {
                try {
                    Thread.sleep(nodeUpdateDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Change color of rectangle according to node type
            switch (node.getNodeType()) {
                case OPEN:
                    rectangle.setFill(Color.LIGHTGREEN);
                    break;
                case CLOSED:
                    rectangle.setFill(Color.RED);
                    break;
                case START:
                    rectangle.setFill(Color.GREEN);
                    return;
                case END:
                    rectangle.setFill(Color.BLUE);
                    return;
                case PATH:
                    rectangle.setFill(Color.YELLOW);
                    break;
                case BARRIER:
                    rectangle.setFill(Color.DARKGRAY);
                    return;
                case UNEVALUATED:
                    rectangle.setFill(Color.LIGHTGRAY);
                    return;
            }


            //Set the costs of the node if present
            if (node.getfCost() > 0) {
                this.fCostText.setText(String.valueOf(node.getfCost()));
                this.gCostText.setText(String.valueOf(node.getgCost()));
                this.hCostText.setText(String.valueOf(node.gethCost()));
            }
        }
    }
}
