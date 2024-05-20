package controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import model.ModelTerritory;
import model.modelExceptions.MonkException;
import view.TerritoryPanel;

import java.util.Observable;
import java.util.Observer;

public class Simulation extends Thread implements Observer
{
    Integer sem;
    ModelTerritory territory;
    TerritoryPanel panel;
    MainController mainController;

    private double velocity = 1500;
    private Double velocityFactor;

    public Simulation(TerritoryPanel panel, ModelTerritory territory, MainController mainController, Double velocityFactor, Integer sem)
    {
        this.territory = territory;
        this.panel = panel;
        this.mainController = mainController;
        this.velocityFactor = velocityFactor;
        this.sem = sem;
    }

    public void run()
    {
        this.territory.addObserver(this);
        try
        {
            territory.getModelMonk().main();
            mainController.stopSimulation();
        } catch (MonkException ex)
        {
            mainController.stopSimulation();
            Platform.runLater(() ->
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler beim Ausführen der Simulation");
                alert.setHeaderText("Ihre Mainmethode hat zu einem Laufzeitfehler geführt, überprüfen Sie diese");
                alert.setContentText(ex.getMessage());
                alert.show();
            });
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        try
        {
            Thread.sleep((long) (velocity * (1 - velocityFactor)));
        }
        catch (InterruptedException e)
        {
            interrupt();
        }
        if (Thread.interrupted())
        {
            try
            {
                synchronized (sem)
                {
                    sem.wait();
                }
            }
            catch (InterruptedException e)
            {
                System.out.println("Nachricht aus einer Exception, die nie passieren sollte");
                e.printStackTrace();
            }
        }
    }

    public void setVelocityFactor(double velocityFactor)
    {
        this.velocityFactor = velocityFactor;
    }

    public Double getVelocityFactor() {
        return velocityFactor;
    }
}
