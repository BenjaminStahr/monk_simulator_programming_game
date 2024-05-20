package view;

import controller.LanguageController;
import controller.MainController;
import controller.PropertiesController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.ModelTerritory;
import java.util.Optional;

public class MainView
{
    Stage stage;

    private BorderPane borderPane;
    private BorderPane downBorderPane;
    private ScrollPane scrollPaneForMap;
    private TerritoryPanel territoryPanel;
    private MonkPopUpMenu monkPopUpMenu;

    private MenuBar menuBar;

    //editor menu
    private Menu menuEditor;
    private MenuItem menuOpenFile;
    private MenuItem menuNewFile;
    private MenuItem menuCompileFile;
    private MenuItem menuPrintFile;
    private MenuItem menuCloseFile;

    //territory menu
    private Menu menuTerritory;
    //submenu for saving the territory
    private Menu subMenuSaveTerritory;
    private MenuItem menuSaveAsXML;
    private MenuItem menuSaveAsSerialized;

    private Menu subMenuLoadTerritory;
    private MenuItem menuLoadXML;
    private MenuItem menuDeserialize;
    private MenuItem menuResizeTerritory;
    private RadioMenuItem menuPlaceMonk;
    private RadioMenuItem menuPlaceGoodKarma;
    private RadioMenuItem menuPlaceBadKarma;
    private RadioMenuItem menuPlaceGround;
    private RadioMenuItem menuPlaceHole;

    //monk menu
    private Menu menuMonk;
    private MenuItem menuTurnLeft;
    private MenuItem menuForward;
    private MenuItem menuTake;
    private MenuItem menuNeutralize;

    //control menu

    private Menu menuControl;
    private MenuItem menuStart;
    private MenuItem menuPause;
    private MenuItem menuStop;

    private Menu menuExamples;
    private MenuItem menuExamplesSave;
    private MenuItem menuExamplesLoad;

    // just two of the items get displayed regarding to the role of the user
    private Menu menuNetWork;
    private MenuItem menuSendRequestToTutor;
    private MenuItem menuGetTutorAnswer;
    private MenuItem menuGetStudentRequest;
    private MenuItem menuSendAnswerToStudent;

    private ToggleGroup languageToggleGroup;
    private Menu menuLanguage;
    private RadioMenuItem menuGerman;
    private RadioMenuItem menuEnglish;

    //here are the variables for the toolbar and the buttons inside the toolbar
    private ToolBar toolBar;

    private Button btnNewFile;
    private Button btnOpenFile;
    private Button btnSaveFile;
    private Button btnCompileFile;
    private Button btnResizeField;

    private ToggleGroup toggleGroupBtnPlace;
    private ToggleButton btnPlaceMonk;
    private ToggleButton btnPlaceGoodKarma;
    private ToggleButton btnPlaceBadKarma;
    private ToggleButton btnPlaceGround;
    private ToggleButton btnPlaceHole;

    private Button btnTake;
    private Button btnTurnLeft;
    private Button btnForward;
    private Button btnNeutralize;


    private Button btnStart;
    private Button btnPause;
    private Button btnStop;

    // slider for changing the velocity of the simulation
    private Slider velocitySlider;

    private Image imgNewFile;
    private Image imgOpenFile;
    private Image imgSaveFile;
    private Image imgCompileFile;
    private Image imgResizeField;
    private Image imgPlaceMonk;
    private Image imgPlaceGoodKarma;
    private Image imgPlaceBadKarma;
    private Image imgPlaceGround;
    private Image imgPlaceHole;
    private Image imgTake;
    private Image imgNeutralize;
    private Image imgTurnLeft;
    private Image imgForward;
    private Image imgStart;
    private Image imgPause;
    private Image imgStop;

    //here is the real working place
    private SplitPane workingArea;
    private TextArea editor;

    //stackpane for centering the map inside inside the scrollPane
    private StackPane centerStackPane;

    //Label just for notifications
    private ScrollPane scrollPaneErrorMessage;

    public MainView(ModelTerritory model)
    {
        //loading resources
        loadResources();

        borderPane = new BorderPane();
        downBorderPane = new BorderPane();
        borderPane.setCenter(downBorderPane);

        menuBar = new MenuBar();
        borderPane.setTop(menuBar);

        setUpEditorMenu();
        setUpTerritoryMenu();
        setUpPlaceMenu();
        setUpMonkMenu();
        setUpControlMenu();
        setUpExampleMenu();
        setUpNetworkMenu();
        setUpLanguageMenu();

        setUpToolBar();

        // here comes the working area
        workingArea = new SplitPane();
        editor = new TextArea();

        scrollPaneForMap = new ScrollPane();

        // here comes the section for the map
        setUpMap(model);

        // monkPopMenu
        monkPopUpMenu = new MonkPopUpMenu();

        workingArea.getItems().addAll(editor, scrollPaneForMap);
        downBorderPane.setCenter(workingArea);

        scrollPaneErrorMessage = new ScrollPane(new Label("Hier werden deine Kompilierfehler angezeigt"));
        scrollPaneErrorMessage.setFitToWidth(true);
        scrollPaneErrorMessage.setFitToHeight(true);
        downBorderPane.setBottom(scrollPaneErrorMessage);


    }


    public void start(Stage mainStage)
    {
        this.stage = mainStage;
        mainStage.setScene(new Scene(borderPane, 1200, 900));
        mainStage.show();
    }

    // this is where the map for the monk gets generated
    private void setUpMap(ModelTerritory model)
    {
        territoryPanel = new TerritoryPanel(model);
        centerStackPane = new StackPane(territoryPanel);

        // src : https://stackoverflow.com/questions/30687994/how-to-center-the-content-of-a-javafx-8-scrollpane, there i got inspired..
        //this binds the minSize of the stackpane to the viewportsize of the scrollpane, and
        // because a stackpane centers it's content, the scrollpane seems always centered
        centerStackPane.minWidthProperty().bind(Bindings.createDoubleBinding(()->
                scrollPaneForMap.getViewportBounds().getWidth(), scrollPaneForMap.viewportBoundsProperty()));

        centerStackPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                scrollPaneForMap.getViewportBounds().getHeight(), scrollPaneForMap.viewportBoundsProperty()));

        scrollPaneForMap.setContent(centerStackPane);

        territoryPanel.printTerritory();
    }
    private void loadResources()
    {
        //resources for working with Files
        imgNewFile = new Image(getClass().getResource("/resources/guiResources/newLevel.png").toString());
        imgOpenFile = new Image(getClass().getResource("/resources/guiResources/openLevel.png").toString());
        imgSaveFile = new Image(getClass().getResource("/resources/guiResources/saveLevel.png").toString());
        imgCompileFile = new Image(getClass().getResource("/resources/guiResources/compileLevel.png").toString());
        imgResizeField= new Image(getClass().getResource("/resources/guiResources/resizeMap.png").toString());
        //resources for placing things on the map
        imgPlaceMonk = new Image(getClass().getResource("/resources/guiResources/monk.png").toString());
        imgPlaceGoodKarma = new Image(getClass().getResource("/resources/guiResources/goodKarma.png").toString());
        imgPlaceBadKarma = new Image(getClass().getResource("/resources/guiResources/badKarma.png").toString());
        imgPlaceGround = new Image(getClass().getResource("/resources/guiResources/ground.png").toString());
        imgPlaceHole = new Image(getClass().getResource("/resources/guiResources/hole.png").toString());
        //resources for working with the monk
        imgTake = new Image(getClass().getResource("/resources/guiResources/enlightendMonk.png").toString());
        imgNeutralize = new Image(getClass().getResource("/resources/guiResources/neutralizeMonk.png").toString());
        imgTurnLeft = new Image(getClass().getResource("/resources/guiResources/rotateMonk.png").toString());
        imgForward = new Image(getClass().getResource("/resources/guiResources/forwardMonk.png").toString());
        //resources for handling the controls
        imgStart = new Image(getClass().getResource("/resources/guiResources/play.png").toString());
        imgPause = new Image(getClass().getResource("/resources/guiResources/pause.png").toString());
        imgStop = new Image(getClass().getResource("/resources/guiResources/stop.png").toString());
    }
    private void setUpEditorMenu()
    {
        //makes the editor
        menuEditor = new Menu("_Editor");
        menuNewFile = new MenuItem("Neu");
        menuNewFile.setGraphic(new ImageView(imgNewFile));
        menuNewFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+N"));
        menuOpenFile = new MenuItem("Öffnen");
        menuOpenFile.setGraphic(new ImageView(imgOpenFile));
        menuOpenFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+O"));
        menuCompileFile = new MenuItem("Kompilieren");
        menuCompileFile.setGraphic(new ImageView(imgCompileFile));
        menuCompileFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+K"));
        menuPrintFile = new MenuItem("Drucken");
        menuPrintFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+P"));
        menuCloseFile = new MenuItem("Beenden");
        menuCloseFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+Q"));
        menuEditor.getItems().addAll(menuNewFile, menuOpenFile, new SeparatorMenuItem(),menuCompileFile, menuPrintFile,
                new SeparatorMenuItem(), menuCloseFile);
        menuBar.getMenus().add(menuEditor);
    }

    private void setUpTerritoryMenu()
    {
        //makes the territory
        menuTerritory = new Menu("_Territorium");

        subMenuSaveTerritory = new Menu("Speichern");
        menuSaveAsXML = new MenuItem("Map als XML speichern");
        menuSaveAsSerialized = new MenuItem("Map serialisiert speichern");
        subMenuSaveTerritory.getItems().addAll(menuSaveAsXML, menuSaveAsSerialized);

        subMenuLoadTerritory = new Menu("Laden");
        menuLoadXML = new MenuItem("Map aus XML-Datei laden");
        menuDeserialize = new MenuItem("Map aus serialisierter Datei laden");
        subMenuLoadTerritory.getItems().addAll(menuLoadXML, menuDeserialize);
        menuResizeTerritory = new MenuItem("Größe ändern");

        menuTerritory.getItems().addAll(subMenuSaveTerritory, subMenuLoadTerritory, menuResizeTerritory);
    }

    private void setUpPlaceMenu()
    {
        //the actions a user can make
        menuPlaceMonk = new RadioMenuItem("Mönch plazieren");
        menuPlaceGoodKarma = new RadioMenuItem("gutes Karma plazieren");
        menuPlaceBadKarma = new RadioMenuItem("schlechtes Karma plazieren");
        menuPlaceGround = new RadioMenuItem("Boden plazieren");
        menuPlaceHole = new RadioMenuItem("Loch plazieren");

        menuPlaceGround.setSelected(true);

        menuTerritory.getItems().addAll(new SeparatorMenuItem(), menuPlaceMonk, menuPlaceGoodKarma, menuPlaceBadKarma,
                menuPlaceGround, menuPlaceHole);

        menuBar.getMenus().add(menuTerritory);
    }
    private void setUpMonkMenu()
    {
        //the actions the monk can make
        menuMonk = new Menu("_Mönch");
        menuTurnLeft = new MenuItem("drehLinks");
        menuTurnLeft.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+L"));
        menuForward = new MenuItem("vor");
        menuForward.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+V"));
        menuTake = new MenuItem("aufnehmen");
        menuTake.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+A"));
        menuNeutralize  = new MenuItem("neutralisieren");
        menuNeutralize.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+N"));

        menuMonk.getItems().addAll(menuTurnLeft, menuForward, menuTake, menuNeutralize);
        menuBar.getMenus().add(menuMonk);
    }
    private void setUpControlMenu()
    {
        //here are the controls for the menu
        menuControl = new Menu("_Simulation");
        menuStart = new MenuItem("Start/Fortsetzen");
        menuStart.setGraphic(new ImageView(imgStart));
        menuStart.setAccelerator(KeyCombination.keyCombination("SHORTCUT+F11"));
        menuPause = new MenuItem("Pause");
        menuPause.setDisable(true);
        menuPause.setGraphic(new ImageView(imgPause));
        menuStop  = new MenuItem("Stop");
        menuStop.setDisable(true);
        menuStop.setGraphic(new ImageView(imgStop));
        menuStop.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+F12"));

        menuControl.getItems().addAll(menuStart, menuPause, menuStop);
        menuBar.getMenus().add(menuControl);
    }

    private void setUpExampleMenu()
    {
        menuExamples = new Menu("Beispiele");
        menuExamplesLoad = new MenuItem("Laden");
        menuExamplesSave = new MenuItem("Speichern");
        menuExamples.getItems().addAll(menuExamplesLoad, menuExamplesSave);
        menuBar.getMenus().add(menuExamples);
    }

    private void setUpNetworkMenu()
    {
        menuNetWork = new Menu("Tutor");
        if(PropertiesController.getProperties().getProperty("role").equals("tutor"))
        {
            menuGetStudentRequest = new MenuItem("Anfrage laden");
            menuSendAnswerToStudent = new MenuItem("Anfrage beantworten");
            menuSendAnswerToStudent.setDisable(true);
            menuNetWork.getItems().addAll(menuGetStudentRequest, menuSendAnswerToStudent);
        }
        else
        {
            menuSendRequestToTutor = new MenuItem("Tutoranfrage schicken");
            menuGetTutorAnswer = new MenuItem("Tutorantwort holen");
            menuGetTutorAnswer.setDisable(true);
            menuNetWork.getItems().addAll(menuSendRequestToTutor, menuGetTutorAnswer);
        }
        menuBar.getMenus().add(menuNetWork);
    }

    private void setUpLanguageMenu()
    {
        menuLanguage = new Menu("Sprache");
        menuGerman = new RadioMenuItem("Deutsch");
        menuEnglish = new RadioMenuItem("Englisch");

        languageToggleGroup = new ToggleGroup();
        menuEnglish.setToggleGroup(languageToggleGroup);
        menuGerman.setToggleGroup(languageToggleGroup);
        if(LanguageController.getLocale().toString().equals("en"))
        {
            //languageToggleGroup.selectToggle(menuEnglish);
            menuEnglish.setSelected(true);
        }
        else
        {
            menuGerman.setSelected(true);
            //languageToggleGroup.selectToggle(menuGerman);
        }
        menuLanguage.getItems().addAll(menuEnglish, menuGerman);
        menuBar.getMenus().add(menuLanguage);
    }

    private void setUpToolBar()
    {
        //Here gets the toolbar set up
        toolBar = new ToolBar();
        downBorderPane.setTop(toolBar);
        //BeispielMit.getResourceString("BeispielMit.1")
        // Buttons for Filehandling
        btnNewFile = new Button();
        Tooltip ttnewFile = new Tooltip(LanguageController.getResourceString("LanguageController.0"));
        ttnewFile.textProperty().bind(LanguageController.getStringBinding("LanguageController.0"));
        btnNewFile.setTooltip(ttnewFile);
        btnNewFile.setGraphic(new ImageView(imgNewFile));

        btnOpenFile = new Button();
        Tooltip ttOpenFile = new Tooltip(LanguageController.getResourceString("LanguageController.1"));
        ttOpenFile.textProperty().bind(LanguageController.getStringBinding("LanguageController.1"));
        btnOpenFile.setTooltip(ttOpenFile);
        btnOpenFile.setGraphic(new ImageView(imgOpenFile));

        btnSaveFile = new Button();
        Tooltip ttSaveFile = new Tooltip(LanguageController.getResourceString("LanguageController.2"));
        ttSaveFile.textProperty().bind(LanguageController.getStringBinding("LanguageController.2"));
        btnSaveFile.setTooltip(ttSaveFile);
        btnSaveFile.setGraphic(new ImageView(imgSaveFile));

        btnCompileFile = new Button();
        Tooltip ttCompileFile = new Tooltip(LanguageController.getResourceString("LanguageController.3"));
        ttCompileFile.textProperty().bind(LanguageController.getStringBinding("LanguageController.3"));
        btnCompileFile.setTooltip(ttCompileFile);
        btnCompileFile.setGraphic(new ImageView(imgCompileFile));

        //resize Field
        btnResizeField = new Button();
        Tooltip ttResizeField = new Tooltip(LanguageController.getResourceString("LanguageController.4"));
        ttResizeField.textProperty().bind(LanguageController.getStringBinding("LanguageController.4"));
        btnResizeField.setTooltip(ttResizeField);
        btnResizeField.setGraphic(new ImageView(imgResizeField));

        //Buttons for placing things on the Map, they are in a togglegroup because only one can be selected
        toggleGroupBtnPlace = new ToggleGroup();

        btnPlaceMonk = new ToggleButton();
        Tooltip ttPlaceMonk = new Tooltip(LanguageController.getResourceString("LanguageController.5"));
        ttPlaceMonk.textProperty().bind(LanguageController.getStringBinding("LanguageController.5"));
        btnPlaceMonk.setTooltip(ttPlaceMonk);
        btnPlaceMonk.setGraphic(new ImageView(imgPlaceMonk));

        btnPlaceGoodKarma = new ToggleButton();
        Tooltip ttPlaceGoodKarma = new Tooltip(LanguageController.getResourceString("LanguageController.6"));
        ttPlaceGoodKarma.textProperty().bind(LanguageController.getStringBinding("LanguageController.6"));
        btnPlaceGoodKarma.setTooltip(ttPlaceGoodKarma);
        btnPlaceGoodKarma.setGraphic(new ImageView(imgPlaceGoodKarma));

        btnPlaceBadKarma = new ToggleButton();
        Tooltip ttPlaceBadKarma = new Tooltip(LanguageController.getResourceString("LanguageController.7"));
        ttPlaceBadKarma.textProperty().bind(LanguageController.getStringBinding("LanguageController.7"));
        btnPlaceBadKarma.setTooltip(ttPlaceBadKarma);
        btnPlaceBadKarma.setGraphic(new ImageView(imgPlaceBadKarma));

        btnPlaceGround = new ToggleButton();
        Tooltip ttPlaceGround = new Tooltip(LanguageController.getResourceString("LanguageController.8"));
        ttPlaceGround.textProperty().bind(LanguageController.getStringBinding("LanguageController.8"));
        btnPlaceGround.setTooltip(ttPlaceGround);
        btnPlaceGround.setGraphic(new ImageView(imgPlaceGround));
        btnPlaceGround.setSelected(true);

        btnPlaceHole = new ToggleButton();
        Tooltip ttPlaceHole = new Tooltip(LanguageController.getResourceString("LanguageController.9"));
        ttPlaceHole.textProperty().bind(LanguageController.getStringBinding("LanguageController.9"));
        btnPlaceHole.setTooltip(ttPlaceHole);
        btnPlaceHole.setGraphic(new ImageView(imgPlaceHole));

        btnPlaceMonk.setToggleGroup(toggleGroupBtnPlace);
        btnPlaceGoodKarma.setToggleGroup(toggleGroupBtnPlace);
        btnPlaceBadKarma.setToggleGroup(toggleGroupBtnPlace);
        btnPlaceGround.setToggleGroup(toggleGroupBtnPlace);
        btnPlaceHole.setToggleGroup(toggleGroupBtnPlace);

        // Here are the actions for the monk
        btnTake = new Button();
        Tooltip ttTake = new Tooltip(LanguageController.getResourceString("LanguageController.10"));
        ttTake.textProperty().bind(LanguageController.getStringBinding("LanguageController.10"));
        btnTake.setTooltip(ttTake);
        btnTake.setGraphic(new ImageView(imgTake));

        btnNeutralize = new Button();
        Tooltip ttNeutralize = new Tooltip(LanguageController.getResourceString("LanguageController.11"));
        ttNeutralize.textProperty().bind(LanguageController.getStringBinding("LanguageController.11"));
        btnNeutralize.setTooltip(ttNeutralize);
        btnNeutralize.setGraphic(new ImageView(imgNeutralize));

        btnTurnLeft = new Button();
        Tooltip ttTurnLeft = new Tooltip(LanguageController.getResourceString("LanguageController.12"));
        ttTurnLeft.textProperty().bind(LanguageController.getStringBinding("LanguageController.12"));
        btnTurnLeft.setTooltip(ttTurnLeft);
        btnTurnLeft.setGraphic(new ImageView(imgTurnLeft));

        btnForward = new Button();
        Tooltip ttForward = new Tooltip(LanguageController.getResourceString("LanguageController.13"));
        ttForward.textProperty().bind(LanguageController.getStringBinding("LanguageController.13"));
        btnForward.setTooltip(ttForward);
        btnForward.setGraphic(new ImageView(imgForward));

        // Here are the controls elements generated
        btnStart = new Button();
        Tooltip ttStart = new Tooltip(LanguageController.getResourceString("LanguageController.14"));
        ttStart.textProperty().bind(LanguageController.getStringBinding("LanguageController.14"));
        btnStart.setTooltip(ttStart);
        btnStart.setGraphic(new ImageView(imgStart));

        btnPause = new Button();
        Tooltip ttPause = new Tooltip(LanguageController.getResourceString("LanguageController.15"));
        ttPause.textProperty().bind(LanguageController.getStringBinding("LanguageController.15"));
        btnPause.setTooltip(ttPause);
        btnPause.setGraphic(new ImageView(imgPause));

        btnStop = new Button();
        Tooltip ttStop = new Tooltip(LanguageController.getResourceString("LanguageController.16"));
        ttStop.textProperty().bind(LanguageController.getStringBinding("LanguageController.16"));
        btnStop.setTooltip(ttStop);
        btnStop.setGraphic(new ImageView(imgStop));

        //here comes the slider
        velocitySlider = new Slider(0.1, 0.9, .5);
        Tooltip ttVelocitySlider = new Tooltip(LanguageController.getResourceString("LanguageController.17"));
        ttVelocitySlider.textProperty().bind(LanguageController.getStringBinding("LanguageController.17"));
        velocitySlider.setTooltip(ttVelocitySlider);

        toolBar.getItems().addAll(btnNewFile, btnOpenFile, new Separator(), btnSaveFile, btnCompileFile, btnResizeField,
                new Separator(),btnPlaceMonk, btnPlaceGoodKarma, btnPlaceBadKarma, btnPlaceGround, btnPlaceHole, new Separator(),
                btnTake, btnNeutralize, btnTurnLeft, btnForward, new Separator(), btnStart, btnPause, btnStop, velocitySlider);
    }

    // this is not really the view, because it's just a way to get input from the user
    // It's kind of hard to imagine how to really only implement this in MVC, because when this is the view,
    // how only send events to the controller, when I have to send data ?
    public void openChangeSizeDialog(MainController mainController)
    {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Größe ändern");
        dialog.setHeaderText("Geben Sie Länge und Breite des Spielfeldes ein : ");

        //the Button for accepting the resize, it's important thats its here for referencing purpose
        ButtonType btnAcceptResize= new ButtonType("Anwenden", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAcceptResize, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textFieldHeight = new TextField();
        textFieldHeight.setPromptText("12");

        TextField textFieldWidth = new TextField();
        textFieldWidth.setPromptText("13");

        grid.add(new Label("Länge:"), 0, 0);
        grid.add(textFieldHeight, 1, 0);
        grid.add(new Label("Breite:"), 0, 1);
        grid.add(textFieldWidth, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> textFieldHeight.requestFocus());

        // Convert the result to a pair when the accept button
        // is clicked.
        dialog.setResultConverter(dialogButton ->
        {
            if (dialogButton == btnAcceptResize)
            {
                return new Pair<>(textFieldHeight.getText(), textFieldWidth.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(widthHeight ->
        {
            if(widthHeight.getKey() != null && widthHeight.getValue() != null)
            {
                mainController.processResizeOfField(Integer.parseInt(widthHeight.getKey()), Integer.parseInt(widthHeight.getValue()));
            }
        });
    }

    // getters are just used for giving listeners to the buttons

    // the place toggle buttons
    public ToggleButton getBtnPlaceHole(){return btnPlaceHole;}
    public ToggleButton getBtnPlaceMonk(){return btnPlaceMonk;}
    public ToggleButton getBtnPlaceGoodKarma(){return btnPlaceGoodKarma;}
    public ToggleButton getBtnPlaceBadKarma(){return btnPlaceBadKarma;}
    public ToggleButton getBtnPlaceGround(){return btnPlaceGround;}

    public ToggleGroup getToggleGroupBtnPlace(){return toggleGroupBtnPlace;}

    // the related menu for the place toggle buttons
    public RadioMenuItem getMenuPlaceMonk(){return menuPlaceMonk;}
    public RadioMenuItem getMenuPlaceHole(){return menuPlaceHole;}
    public RadioMenuItem getMenuPlaceGround(){return menuPlaceGround;}
    public RadioMenuItem getMenuPlaceGoodKarma(){return menuPlaceGoodKarma;}
    public RadioMenuItem getMenuPlaceBadKarma(){return menuPlaceBadKarma;}

    // menu for handling data
    public MenuItem getMenuOpenFile(){return menuOpenFile;}
    public MenuItem getMenuNewFile(){return menuNewFile;}
    public MenuItem getMenuCompileFile(){return menuCompileFile;}
    public MenuItem getMenuCloseFile(){return menuCloseFile;}
    public MenuItem getMenuResizeTerritory(){return menuResizeTerritory;}

    // Buttons for handling data
    public Button getBtnSaveFile(){return btnSaveFile;}
    public Button getBtnOpenFile(){return btnOpenFile;}
    public Button getBtnNewFile(){return btnNewFile;}
    public Button getBtnCompileFile(){return btnCompileFile;}
    public Button getBtnResizeField(){ return btnResizeField;}
    // buttons for actions of the monk
    public Button getBtnTake(){return btnTake;}
    public Button getBtnTurnLeft(){return btnTurnLeft;}
    public Button getBtnForward(){return btnForward;}
    public Button getBtnNeutralize(){return btnNeutralize;}

    // menu for actions of the monk
    public MenuItem getMenuTake(){return menuTake;}
    public MenuItem getMenuForward(){return menuForward;}
    public MenuItem getMenuNeutralize(){return menuNeutralize;}
    public MenuItem getMenuTurnLeft(){return menuTurnLeft;}

    public TerritoryPanel getTerritoryPanel(){return territoryPanel;}
    public TextArea getEditor(){return editor;}
    public Stage getStage(){return stage;}
    public MonkPopUpMenu getMonkPopUpMenu(){return monkPopUpMenu;}
    public ScrollPane getScrollPaneErrorMessage(){return scrollPaneErrorMessage;}

    public Button getBtnStart() { return btnStart; }
    public Button getBtnPause(){return btnPause;}
    public Button getBtnStop(){return btnStop;}

    public MenuItem getMenuStart(){return menuStart;}
    public MenuItem getMenuPause(){return menuPause;}
    public MenuItem getMenuStop(){return menuStop;}

    public MenuItem getMenuSaveAsSerialized(){return menuSaveAsSerialized;}
    public MenuItem getMenuDeserialize(){return menuDeserialize;}
    public MenuItem getMenuPrintFile(){return menuPrintFile;}
    public MenuItem getMenuSaveAsXML(){return menuSaveAsXML;}
    public MenuItem getMenuLoadXML(){return menuLoadXML;}

    public MenuItem getMenuExamplesSave(){return menuExamplesSave;}
    public MenuItem getMenuExamplesLoad(){return menuExamplesLoad;}

    // student network connection
    public MenuItem getMenuSendRequestToTutor(){return menuSendRequestToTutor;}
    public MenuItem getMenuGetTutorAnswer(){return menuGetTutorAnswer;}

    // tutor network connection
    public MenuItem getMenuGetStudentRequest(){return menuGetStudentRequest;}
    public MenuItem getMenuSendAnswerToStudent(){return menuSendAnswerToStudent;}

    public ToggleGroup getLanguageToggleGroup(){return languageToggleGroup;}
    public RadioMenuItem getMenuGerman(){return menuGerman;}
    public RadioMenuItem getMenuEnglish(){return menuEnglish;}

    public Slider getVelocitySlider(){return velocitySlider;}

    public  void setMonkPopUpMenu(MonkPopUpMenu monkPopUpMenu){this.monkPopUpMenu = monkPopUpMenu;}
}
