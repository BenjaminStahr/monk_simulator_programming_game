package controller;

import controller.programmanagement.CompileController;
import controller.programmanagement.FileController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ModelMonk;
import model.ModelTerritory;
import model.modelExceptions.MonkException;
import server.NetworkController;
import server.NetworkControllerImplementation;
import server.Request;
import view.MainView;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
// TODO zu große Territorien können nicht in der Datenbank gespeichert werden
public class MainController
{
    enum state{monk, goodKarma, badKarma, ground, hole}
    private state selected = state.ground;

    private MainView mainView;
    private Stage stage;

    private ModelTerritory model;
    private boolean inDragMode = false;

    private Simulation currentSimulation;
    private volatile Integer sem = 0;

    Registry registry = null;
    NetworkController controller;
    private UUID studentIdentifier;
    private UUID actualStudent;
    public boolean networkButtonState[] = {true, false};

    public MainController(Stage stage)
    {
        LanguageController.updateLanguage();
        setUpNetworkConnection();
        studentIdentifier = UUID.randomUUID();
        this.stage = stage;

        // a default territory
        model = new ModelTerritory(5,5);
        model.loadTerritory(model.generateTestMap(), 3, 0);
        model.setMonkDirection(ModelTerritory.direction.south);

        // model gets only passed to the TerritoryPanel, because the territorypanel needs it for drawing the field
        mainView = new MainView(model);

        addListeners(stage);

        // sets up the default program
        FileController.makeUserDirectory();
        FileController.createFileInUserDirectory(stage.getTitle());

        // compiles the defaultprogramm and sets the instance of the monk if the program is compilable
        if(CompileController.isCompilable(FileController.getFileDirectory(stage.getTitle()).toString()))
        {
            CompileController.replaceMonkInstance(FileController.getFileDirectory(stage.getTitle()).toString(), model, mainView);
        }
        else
        {
            ModelMonk modelMonk = new ModelMonk(model);
            model.setMonk(modelMonk);
            // set new context menu
            mainView.setMonkPopUpMenu(mainView.getMonkPopUpMenu().generateBasicContent(modelMonk, mainView));
        }
    }

    // starts the view with the printed default program
    public void start()
    {
        mainView.start(stage);
        updateNetworkButtons();
        FileController.openNewProgram(mainView, stage.getTitle());
    }

    private void addListeners(Stage stage)
    {
        //listeners for buttons, which are important for filemanagement
        mainView.getBtnNewFile().setOnAction(new NewFileListener());
        mainView.getBtnOpenFile().setOnAction(new OpenProgramListener());
        mainView.getBtnSaveFile().setOnAction(new SaveProgramListener());
        mainView.getBtnCompileFile().setOnAction(new CompileListener());
        mainView.getBtnResizeField().setOnAction(new ChangeSizeListener());

        // same listeners for the menu
        mainView.getMenuNewFile().setOnAction(new NewFileListener());
        mainView.getMenuOpenFile().setOnAction(new OpenProgramListener());
        mainView.getMenuCompileFile().setOnAction(new CompileListener());
        mainView.getMenuResizeTerritory().setOnAction(new ChangeSizeListener());
        mainView.getMenuCloseFile().setOnAction(event -> FileController.closeProgram(mainView, controller));

        // binding disabledPropertys, so the user can't use the Menu, if he's not allowed
        mainView.getBtnNewFile().disableProperty().bindBidirectional(mainView.getMenuNewFile().disableProperty());
        mainView.getBtnOpenFile().disableProperty().bindBidirectional(mainView.getMenuOpenFile().disableProperty());
        mainView.getBtnCompileFile().disableProperty().bindBidirectional(mainView.getMenuCompileFile().disableProperty());
        mainView.getBtnResizeField().disableProperty().bindBidirectional(mainView.getMenuResizeTerritory().disableProperty());

        // listener for the actionbuttons of the monk
        mainView.getBtnForward().setOnAction(new MonkForwardListener());
        mainView.getBtnTake().setOnAction(new MonkTakeListener());
        mainView.getBtnTurnLeft().setOnAction(new MonkTurnLeftListener());
        mainView.getBtnNeutralize().setOnAction(new MonkNeutralizeListener());

        // listeners for the actionmenu of the monk
        mainView.getMenuForward().setOnAction(new MonkForwardListener());
        mainView.getMenuTake().setOnAction(new MonkTakeListener());
        mainView.getMenuTurnLeft().setOnAction(new MonkTurnLeftListener());
        mainView.getMenuNeutralize().setOnAction(new MonkNeutralizeListener());

        // binding the disable propertys of the buttons to the menu
        mainView.getBtnForward().disableProperty().bindBidirectional(mainView.getMenuForward().disableProperty());
        mainView.getBtnTake().disableProperty().bindBidirectional(mainView.getMenuTake().disableProperty());
        mainView.getBtnTurnLeft().disableProperty().bindBidirectional(mainView.getMenuTurnLeft().disableProperty());
        mainView.getBtnNeutralize().disableProperty().bindBidirectional(mainView.getMenuNeutralize().disableProperty());

        // listener for the togglegroup, which is responsible for placing things on the map
        mainView.getToggleGroupBtnPlace().selectedToggleProperty().addListener(new PropertySelectedListener());

        // bind placeMenu to buttons
        mainView.getBtnPlaceHole().selectedProperty().bindBidirectional(mainView.getMenuPlaceHole().selectedProperty());
        mainView.getBtnPlaceBadKarma().selectedProperty().bindBidirectional(mainView.getMenuPlaceBadKarma().selectedProperty());
        mainView.getBtnPlaceGoodKarma().selectedProperty().bindBidirectional(mainView.getMenuPlaceGoodKarma().selectedProperty());
        mainView.getBtnPlaceMonk().selectedProperty().bindBidirectional(mainView.getMenuPlaceMonk().selectedProperty());
        mainView.getBtnPlaceGround().selectedProperty().bindBidirectional(mainView.getMenuPlaceGround().selectedProperty());

        mainView.getBtnPlaceMonk().disableProperty().bindBidirectional(mainView.getMenuPlaceMonk().disableProperty());
        mainView.getBtnPlaceBadKarma().disableProperty().bindBidirectional(mainView.getMenuPlaceBadKarma().disableProperty());
        mainView.getBtnPlaceGoodKarma().disableProperty().bindBidirectional(mainView.getMenuPlaceGoodKarma().disableProperty());
        mainView.getBtnPlaceHole().disableProperty().bindBidirectional(mainView.getMenuPlaceHole().disableProperty());
        mainView.getBtnPlaceGround().disableProperty().bindBidirectional(mainView.getMenuPlaceGround().disableProperty());

        //listener for territoryPanel
        mainView.getTerritoryPanel().addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedListener());
        mainView.getTerritoryPanel().addEventHandler(MouseEvent.MOUSE_DRAGGED, new MonkMovedListener());
        mainView.getTerritoryPanel().addEventHandler(MouseEvent.MOUSE_RELEASED, new MonkReleasedListener());

        // listener for simulation
        mainView.getBtnStart().setOnAction(new SimulationStartListener());
        mainView.getBtnPause().setOnAction(new SimulationPauseListener());
        mainView.getBtnStop().setOnAction(new SimulationStopListener());

        mainView.getMenuStart().setOnAction(new SimulationStartListener());
        mainView.getMenuPause().setOnAction(new SimulationPauseListener());
        mainView.getMenuStop().setOnAction(new SimulationStopListener());

        // binding disabled propertys to menu
        mainView.getBtnStart().disableProperty().bindBidirectional(mainView.getMenuStart().disableProperty());
        mainView.getBtnPause().disableProperty().bindBidirectional(mainView.getMenuPause().disableProperty());
        mainView.getBtnStop().disableProperty().bindBidirectional(mainView.getMenuStop().disableProperty());

        // menus for saveing and loading territorys
        mainView.getMenuSaveAsSerialized().setOnAction(new TerritorySerializeSaveListener());
        mainView.getMenuDeserialize().setOnAction(new TerritoryDeserializeListener());
        mainView.getMenuSaveAsXML().setOnAction(new TerritoryToXmlListener());
        mainView.getMenuLoadXML().setOnAction(new TerritoryXmlLoader());
        mainView.getMenuExamplesSave().setOnAction(new SaveExampleListener());
        mainView.getMenuExamplesLoad().setOnAction(new LoadExampleListener());

        // listeners for changing language
        mainView.getLanguageToggleGroup().selectedToggleProperty().addListener(new LanguageChangeListener());

        // menu for printing the territory
        mainView.getMenuPrintFile().setOnAction(new TerritoryPrintListener());

        // the listeners for the network connection
        // gives just to the inialized parts the Listener
        if(mainView.getMenuSendRequestToTutor() != null)
        {
            mainView.getMenuSendRequestToTutor().setOnAction(new SendRequestToTutorListener());
            mainView.getMenuGetTutorAnswer().setOnAction(new GetTutorAnswerListener());
        }
        else
        {
            mainView.getMenuGetStudentRequest().setOnAction(new LoadStudentRequestListener());
            mainView.getMenuSendAnswerToStudent().setOnAction(new SendTutorAnswerToStudentListener());
        }
        // adding slider functionality
        mainView.getVelocitySlider().valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(currentSimulation != null)
            {
                synchronized (currentSimulation.getVelocityFactor())
                {
                    currentSimulation.setVelocityFactor(newValue.doubleValue());
                }

            }
        });

        // listeners for window
        stage.setOnCloseRequest(event ->
        {
            FileController.closeProgram(mainView, controller);
        });

        // listener, who opens the contextmenu for the monk, if user presses him
        mainView.getTerritoryPanel().setOnContextMenuRequested(e -> {
            if(model.getMonkPositionX() == (int)e.getX()/model.getTileSize() && model.getMonkPositionY() == (int)e.getY()/model.getTileSize())
            {
                Platform.runLater(() ->
                {
                    mainView.getMonkPopUpMenu().show(mainView.getTerritoryPanel(), e.getScreenX(), e.getScreenY());
                });
            }
        });
    }

    public void setUpNetworkConnection()
    {
        try
        {
            if(PropertiesController.getProperties().getProperty("role").equals("tutor"))
            {
                NetworkControllerImplementation controllerImplementation = new NetworkControllerImplementation();
                LocateRegistry.createRegistry(Integer.parseInt(PropertiesController.getProperties().getProperty("tutorport")));

                registry = LocateRegistry.getRegistry(Integer.parseInt(PropertiesController.getProperties().getProperty("tutorport")));
                registry.rebind("NetworkController", controllerImplementation);
            }
            else
            {
                registry = LocateRegistry.getRegistry(Integer.parseInt(PropertiesController.getProperties().getProperty("tutorport")));
            }
            controller = (NetworkController) registry.lookup("NetworkController");
        }
        catch (RemoteException e)
        {
            networkButtonState = new boolean[]{false, false};
            e.printStackTrace();
            System.out.println("Probably you already have another tutor opened");
        }
        catch (NotBoundException e)
        {
            e.printStackTrace();
        }
    }

    // answer from the dialog for resizing
    public void processResizeOfField(int width, int height)
    {
        model.resizeTerritory(width, height);
    }

    // manages buttons, for setting the state right after simulation
    public void updateNetworkButtons()
    {
        // means your a tutor
        if(mainView.getMenuGetStudentRequest() != null)
        {
            if (networkButtonState[0])
            {
                mainView.getMenuGetStudentRequest().setDisable(false);
            }
            else
            {
                mainView.getMenuGetStudentRequest().setDisable(true);
            }
            if(networkButtonState[1])
            {
                mainView.getMenuSendAnswerToStudent().setDisable(false);
            }
            else
            {
                mainView.getMenuSendAnswerToStudent().setDisable(true);
            }
        }
        else
        {
            if(networkButtonState[0])
            {
                mainView.getMenuSendRequestToTutor().setDisable(false);
            }
            else
            {
                mainView.getMenuSendRequestToTutor().setDisable(true);
            }
            if(networkButtonState[1])
            {
                mainView.getMenuGetTutorAnswer().setDisable(false);
            }
            else
            {
                mainView.getMenuGetTutorAnswer().setDisable(true);
            }
        }
    }

    // monk is selected and if the player now presses on the monk, he will be in dragmode
    private void enterDragMode(MouseEvent e)
    {
        if(monkSelected(e))
        {
            inDragMode = true;
        }
    }

    // checks if the cursor on the territorypanel is on the monk
    private boolean monkSelected(MouseEvent e)
    {
        return model.getMonkPositionX() == (int)e.getX()/model.getTileSize() && model.getMonkPositionY() == (int)e.getY()/model.getTileSize();
    }


    // https://stackoverflow.com/questions/13557195/how-to-check-if-string-is-a-valid-class-identifier
    // sometimes googling is faster than thinking...
    private boolean isClassIdentifier(String name)
    {
        if(!name.equals(""))
        {
            char[] c = name.toCharArray();

            if (!Character.isJavaIdentifierStart(c[0]))
            {
                return false;
            }
            for (int i = 1; i < c.length; i++)
            {
                if (!Character.isJavaIdentifierPart(c[i]))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    // opens a file
    private void openFile(String name)
    {
        if(!FileController.isProgramOpened(name))
        {
            Stage nextStage = new Stage();
            nextStage.setTitle(name);
            MainController nextMainController = new MainController(nextStage);
            nextMainController.start();
        }
    }

    // the error dialog if a user trys to make a forbidden move with the buttons in the toolbar
    private void buildStandardExceptionDialog(MonkException e)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler beim Ausführen eines Befehls");
        alert.setHeaderText("Dieser Befehl ist im momentanen Zustand nicht möglich");
        alert.setContentText(e.getMessage());
        alert.show();
    }

    // disable to get the simulation running without annoying user interaction
    private void setButtonsForStartSimulation()
    {
        mainView.getBtnNewFile().setDisable(true);
        mainView.getBtnOpenFile().setDisable(true);
        mainView.getBtnSaveFile().setDisable(true);
        mainView.getBtnCompileFile().setDisable(true);
        mainView.getBtnResizeField().setDisable(true);
        mainView.getToggleGroupBtnPlace().getToggles().forEach(toggle ->
        {
            Node node = (Node) toggle ;
            node.setDisable(true);
        });
        mainView.getBtnForward().setDisable(true);
        mainView.getBtnTake().setDisable(true);
        mainView.getBtnNeutralize().setDisable(true);
        mainView.getBtnTurnLeft().setDisable(true);

        mainView.getBtnStart().setDisable(true);
        mainView.getBtnPause().setDisable(false);
        mainView.getBtnStop().setDisable(false);

        mainView.getMenuSaveAsXML().setDisable(true);
        mainView.getMenuSaveAsSerialized().setDisable(true);
        mainView.getMenuLoadXML().setDisable(true);
        mainView.getMenuDeserialize().setDisable(true);
        mainView.getMenuPrintFile().setDisable(true);
        mainView.getMenuExamplesLoad().setDisable(true);
        mainView.getMenuExamplesSave().setDisable(true);
        if(mainView.getMenuGetStudentRequest() != null)
        {
                mainView.getMenuGetStudentRequest().setDisable(true);
                mainView.getMenuSendAnswerToStudent().setDisable(true);
        }
        else
        {
            mainView.getMenuSendRequestToTutor().setDisable(true);
            mainView.getMenuGetTutorAnswer().setDisable(true);
        }
        mainView.getMenuGerman().setDisable(true);
        mainView.getMenuEnglish().setDisable(true);
    }

    // only possible, if user is in simulation mode
    private void setButtonsForPauseSimulation()
    {
        mainView.getBtnStart().setDisable(false);
        mainView.getBtnPause().setDisable(true);
    }

    // setting buttons back to normal
    private void setButtonsForStopSimulation()
    {
        mainView.getBtnNewFile().setDisable(false);
        mainView.getBtnOpenFile().setDisable(false);
        mainView.getBtnSaveFile().setDisable(false);
        mainView.getBtnCompileFile().setDisable(false);
        mainView.getBtnResizeField().setDisable(false);
        mainView.getToggleGroupBtnPlace().getToggles().forEach(toggle ->
        {
            Node node = (Node) toggle ;
            node.setDisable(false);
        });
        mainView.getBtnForward().setDisable(false);
        mainView.getBtnTake().setDisable(false);
        mainView.getBtnNeutralize().setDisable(false);
        mainView.getBtnTurnLeft().setDisable(false);

        mainView.getBtnStart().setDisable(false);
        mainView.getBtnPause().setDisable(true);
        mainView.getBtnStop().setDisable(true);

        mainView.getMenuSaveAsXML().setDisable(false);
        mainView.getMenuSaveAsSerialized().setDisable(false);
        mainView.getMenuLoadXML().setDisable(false);
        mainView.getMenuDeserialize().setDisable(false);
        mainView.getMenuPrintFile().setDisable(false);
        mainView.getMenuExamplesLoad().setDisable(false);
        mainView.getMenuExamplesSave().setDisable(false);
        updateNetworkButtons();
        mainView.getMenuEnglish().setDisable(false);
        mainView.getMenuGerman().setDisable(false);
    }

    // method for stoping the simulation
    public void stopSimulation()
    {
        setButtonsForStopSimulation();
        mainView.getTerritoryPanel().setDisable(false);
        if(currentSimulation != null)
        {
            if (currentSimulation.getState() == Thread.State.RUNNABLE || currentSimulation.getState() == Thread.State.TIMED_WAITING)
            {
                currentSimulation.interrupt();
            }
        }
        model.deleteObserver(currentSimulation);
        currentSimulation = null;
    }

    // Here are different classes, which are only different eventhandlers

    // a class which compiles the Code if the User presses compile button
    private class CompileListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            // save before compile data
            FileController.saveDataFromEditor(mainView);
            if(CompileController.isCompilable(FileController.getFileDirectory(stage.getTitle()).toString()))
            {

                Platform.runLater(() ->
                {
                    CompileController.replaceMonkInstance(FileController.getFileDirectory(stage.getTitle()).toString(), model, mainView);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Kompilieren");
                    alert.setHeaderText("Das Kompilieren war erfolgreich");
                    alert.setContentText("Ihr Programm wurde erfolgreich kompiliert");
                    alert.show();
                });
            }
            else
            {
                ModelMonk modelMonk = new ModelMonk(model);
                model.setMonk(modelMonk);
                // set new context menu
                mainView.setMonkPopUpMenu(mainView.getMonkPopUpMenu().generateBasicContent(modelMonk, mainView));

                Platform.runLater(() ->
                {
                    mainView.getScrollPaneErrorMessage().setContent(new TextArea("Ihr Programm konnte aufgrund folgender Fehler nicht " +
                            "kompiliert werden : \n \n" + CompileController.getErrMessage()));
                });
            }
        }
    }

    // a class which generates an inputfield for the user, makes new file and opens it
    private class NewFileListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Neues Programm anlegen");
            dialog.setHeaderText("Legen Sie ein neues Programm an");
            dialog.setContentText("Geben Sie einen validen Javaklassennamen ein :");

            // Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()){
                if(isClassIdentifier(result.get()))
                {
                    if(!Files.exists(FileController.getFileDirectory(result.get())))
                    {
                        FileController.createFileInUserDirectory(result.get());
                        openFile(result.get());
                    }
                    else
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Fehler");
                        alert.setHeaderText("Diese Klasse existiert schon");
                        alert.setContentText("Du kannst eine Klasse nur einmal anlegen, wenn es sie schon gibt, kannst du sie einfach öffen. " +
                                "Warum probierst du überhaupt sie neu anzulegen ?? Lass das besser");
                        alert.show();
                    }
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Klassenname ist ist nicht valide");
                    alert.setContentText("Der Name den du hier eingibst, muss valide sein. Zum Beispiel darf er nicht mit einer Zahl anfangen");
                    alert.show();
                }
            }
        }
    }

    // Listener for modifing the territoryPanel
    private class MousePressedListener implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent e)
        {
            switch (selected)
            {
                case badKarma: model.setBadKarmaAt((int)e.getX()/model.getTileSize(), (int)e.getY()/model.getTileSize());
                break;
                case goodKarma: model.setGoodKarmaAt((int)e.getX()/model.getTileSize(), (int)e.getY()/model.getTileSize());
                break;
                case ground: model.setGroundAt((int)e.getX()/model.getTileSize(), (int)e.getY()/model.getTileSize());
                break;
                case hole: model.setHoleAt((int)e.getX()/model.getTileSize(), (int)e.getY()/model.getTileSize());
                break;
                case monk: enterDragMode(e);
                break;
            }
        }
    }

    // Here are two Listeners for working with the mouse, while in Dragmode
    private class MonkMovedListener implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent e)
        {
            if(inDragMode)
            {
                // the entity the player has pressed on, looks kinda messy...
                if((int) e.getX() / model.getTileSize() >= 0 && (int) e.getX() / model.getTileSize() < model.getTerritorySizeX() &&
                   (int) e.getY() / model.getTileSize() >= 0 && (int) e.getY() / model.getTileSize() < model.getTerritorySizeY())
                {
                    ModelTerritory.entitys actual = model.getTerritory()[(int) e.getX() / model.getTileSize()][(int) e.getY() / model.getTileSize()];
                    if (actual.equals(ModelTerritory.entitys.goodKarma) || actual.equals(ModelTerritory.entitys.ground))
                    {
                        model.setMonkAt((int) e.getX() / model.getTileSize(), (int) e.getY() / model.getTileSize());
                    }
                }
            }
        }
    }

    private class MonkReleasedListener implements EventHandler<MouseEvent>
    {
        @Override
        public void handle(MouseEvent event)
        {
            inDragMode = false;
        }
    }

    private class SaveProgramListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileController.saveDataFromEditor(mainView);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Speichern");
            alert.setHeaderText("Speichern erfolgreich");
            alert.setContentText("Sie haben ihr Programm soeben erfolgreich gespeichert");
            alert.show();
        }
    }

    // opens a new program from files
    private class OpenProgramListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JAVA files (*.java)", "*.java");
            chooser.getExtensionFilters().add(extFilter);
            chooser.setInitialDirectory(new File(FileController.getProgramDirectory()));
            File file = chooser.showOpenDialog(stage);

            if(file != null)
            {
                String title = file.getName().replace(".java", "");
                // only open program, if it's not already opened
                if (!FileController.isProgramOpened(title))
                {
                    openFile(title);
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warnung");
                    alert.setHeaderText("Programm bereits geöffnet");
                    alert.setContentText("Man kann ein Programm nur einmal öffnen, wenn du es unbedingt zweimal öffnen willst," + " mach das ausgewählte Programm zu und öffne es wieder...");
                    alert.showAndWait();
                }
            }
        }
    }

    // Opening the ResizeDialog for the field
    private class ChangeSizeListener implements  EventHandler
    {
        @Override
        public void handle(Event event)
        {
            mainView.openChangeSizeDialog(MainController.this);
        }
    }

    // Here is the Listener for modifying the Canvas
    private class PropertySelectedListener implements ChangeListener<Toggle>
    {
        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
        {
            if (mainView.getToggleGroupBtnPlace().getSelectedToggle() != null)
            {
                if (mainView.getToggleGroupBtnPlace().getSelectedToggle().equals(mainView.getBtnPlaceBadKarma()))
                {
                    selected = state.badKarma;
                }
                else if (mainView.getToggleGroupBtnPlace().getSelectedToggle().equals(mainView.getBtnPlaceGoodKarma()))
                {
                    selected = state.goodKarma;
                }
                else if (mainView.getToggleGroupBtnPlace().getSelectedToggle().equals(mainView.getBtnPlaceMonk()))
                {
                    selected = state.monk;
                }
                else if (mainView.getToggleGroupBtnPlace().getSelectedToggle().equals(mainView.getBtnPlaceGround()))
                {
                    selected = state.ground;
                }
                else if (mainView.getToggleGroupBtnPlace().getSelectedToggle().equals(mainView.getBtnPlaceHole()))
                {
                    selected = state.hole;
                }
            }
        }
    }

    private class LanguageChangeListener implements ChangeListener<Toggle>
    {
        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
        {
            if(mainView.getLanguageToggleGroup().getSelectedToggle() != null)
            {
                if (mainView.getLanguageToggleGroup().getSelectedToggle().equals(mainView.getMenuGerman()))
                {
                    PropertiesController.setLanguage("de");
                }
                else
                {
                    PropertiesController.setLanguage("en");
                }
                LanguageController.updateLanguage();
            }
        }
    }

    private class MonkForwardListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                model.getModelMonk().vor();
            }
            catch (MonkException e)
            {
                buildStandardExceptionDialog(e);
            }
        }
    }
    private class MonkTurnLeftListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            model.getModelMonk().drehLinks();
        }
    }
    private class MonkNeutralizeListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                model.getModelMonk().neutralisieren();
            }
            catch(MonkException e)
            {
                buildStandardExceptionDialog(e);
            }

        }
    }
    private class MonkTakeListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                model.getModelMonk().aufnehmen();
            }
            catch (MonkException e)
            {
                buildStandardExceptionDialog(e);
            }
        }
    }
    private class SimulationStartListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            setButtonsForStartSimulation();
            mainView.getTerritoryPanel().setDisable(true);
            if(currentSimulation == null)
            {
                sem = new Integer(0);
                currentSimulation = new Simulation(mainView.getTerritoryPanel(), model,
                        MainController.this, mainView.getVelocitySlider().getValue(), sem);
                currentSimulation.start();
            }
            else
            {
                synchronized (sem)
                {
                    sem.notify();
                }
            }
        }
    }


    private class SimulationPauseListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            setButtonsForPauseSimulation();
            currentSimulation.interrupt();

        }
    }


    private class SimulationStopListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            // here we use a method because we want to close the sim from the Simulation Class too
            stopSimulation();
        }
    }

    private class TerritorySerializeSaveListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".ter", "*.ter"));
            chooser.setInitialFileName("example.ter");
            chooser.setInitialDirectory(new File(FileController.getTerritoyDirectory()));
            File file = chooser.showSaveDialog(stage);

            if(file != null)
            {
                FileController.saveTerritory(file.toPath(), model);
            }
        }
    }
    private class TerritoryDeserializeListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".ter", "*.ter"));
            chooser.setInitialDirectory(new File(FileController.getTerritoyDirectory()));
            File file = chooser.showOpenDialog(stage);

            if(file != null)
            {
                FileController.loadTerritory(file.toPath(), model);
            }
        }
    }

    private class TerritoryToXmlListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
            chooser.setInitialFileName("example.xml");
            chooser.setInitialDirectory(new File(FileController.getTerritoryXmlDirectory()));
            File file = chooser.showSaveDialog(stage);
            if(file != null)
            {
                FileController.saveTerritoryAsXml(file.toPath(), model);
            }
        }
    }

    private class TerritoryXmlLoader implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
            chooser.setInitialDirectory(new File(FileController.getTerritoryXmlDirectory()));
            File file = chooser.showOpenDialog(stage);
            if(file != null)
            {
                FileController.loadTerritoryAsXml(file.toPath(), model);
            }
            model.notifyObserversDirectly();
        }
    }

    private class SaveExampleListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            if(!DatabaseController.checkTablesExist())
            {
                DatabaseController.createDatabase();
            }
            Platform.runLater(()->
            {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Tags");
                dialog.setHeaderText("Eingabe der Tags");
                dialog.setContentText("Bitte geben sie alle Tags für das Programm durch ein Leerzeichen getrennt ein :");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent())
                {
                    String xmlCode = FileController.saveTerritoryAsString(model);
                    DatabaseController.insertExample(mainView.getEditor().getText(), xmlCode, result.get());
                }
            });
        }
    }

    private class LoadExampleListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            Platform.runLater(()->
            {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Beispiel laden");
                dialog.setHeaderText("Laden Sie eines Ihrer Beispiele");
                dialog.setContentText("Geben Sie einen Tag eines ihrer Beispiele ein :");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent())
                    {
                        ArrayList<String> names = DatabaseController.getNamesOfProgramsForTag(result.get());
                        if (names != null && names.size() > 0)
                        {
                            Platform.runLater(() ->
                            {
                                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(names.get(0), names);
                                choiceDialog.setTitle("Programmwahl");
                                choiceDialog.setHeaderText("Auswahl eines gespeicherten Beispiels");
                                choiceDialog.setContentText("Wählen Sie das zu ladende Beispiel aus der Liste :");
                                Optional<String> choiceResult = choiceDialog.showAndWait();
                                if (choiceResult.isPresent())
                                {
                                    DatabaseController.setExampleToEnviroment(Integer.parseInt(choiceResult.get()), model, mainView);
                                }
                            });
                        }
                        else
                        {
                            Platform.runLater(() ->
                            {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Fehler aufgetreten");
                                alert.setHeaderText("Kein Beispiel unter diesem Tag");
                                alert.setContentText("Leider konnten wir unter dem von Ihnen eingegebenen Text kein Programm finden." + "Höchstwahrscheinlich haben Sie noch nie unter diesem Tag ein Programm abgespeichert!");
                                alert.showAndWait();
                            });
                        }
                    }
            });
        }
    }

    // just used by the student
    private class SendRequestToTutorListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                controller.sendRequest(mainView.getEditor().getText(), FileController.saveTerritoryAsString(model), studentIdentifier);
                mainView.getMenuSendRequestToTutor().setDisable(true);
                mainView.getMenuGetTutorAnswer().setDisable(false);
                networkButtonState = new boolean[]{false, true};
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class GetTutorAnswerListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                if(controller.isProcessed(studentIdentifier))
                {
                    Request request = controller.getTutorAnswer(studentIdentifier);
                    // load new environment
                    mainView.getEditor().setText(request.getCode());
                    Reader reader = new StringReader(request.getTerritory());
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
                    FileController.setTerritoryFromXmlFile(model, xmlReader);
                    model.notifyObserversDirectly();

                    mainView.getMenuSendRequestToTutor().setDisable(false);
                    mainView.getMenuGetTutorAnswer().setDisable(true);
                    networkButtonState = new boolean[]{true, false};
                }
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
            catch (XMLStreamException e)
            {
                e.printStackTrace();
            }
        }
    }

    // just used by the tutor
    private class LoadStudentRequestListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                Request request = controller.getOldestRequest();
                if(request != null)
                {
                    // load new environment
                    mainView.getEditor().setText(request.getCode());
                    Reader reader = new StringReader(request.getTerritory());
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
                    FileController.setTerritoryFromXmlFile(model, xmlReader);
                    actualStudent = request.getStudentIdentifier();
                    model.notifyObserversDirectly();

                    mainView.getMenuGetStudentRequest().setDisable(true);
                    mainView.getMenuSendAnswerToStudent().setDisable(false);
                    networkButtonState = new boolean[]{false, true};
                }
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
            catch (XMLStreamException e)
            {
                e.printStackTrace();
            }
        }
    }

    // just justed by the tutor
    private class SendTutorAnswerToStudentListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            try
            {
                controller.setTutorAnswer(mainView.getEditor().getText(), FileController.saveTerritoryAsString(model), actualStudent);

                mainView.getMenuGetStudentRequest().setDisable(false);
                mainView.getMenuSendAnswerToStudent().setDisable(true);
                networkButtonState = new boolean[]{true, false};
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class TerritoryPrintListener implements EventHandler
    {
        @Override
        public void handle(Event event)
        {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null)
            {
                if (printerJob.showPrintDialog(stage.getOwner()))
                {
                    if (printerJob.printPage(mainView.getTerritoryPanel()))
                    {
                        printerJob.endJob();
                    }
                }
            }
        }
    }
}
