package view;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.ModelTerritory;

import java.util.Observable;
import java.util.Observer;

public class TerritoryPanel extends Canvas implements Observer
{
    ModelTerritory modelTerritory;
    int tileSize;

    //the images the map is based on
    private Image imgHole;
    private Image imgGround;
    private Image imgBadKarmaOnGround;
    private Image imgGoodKarmaOnGround;
    private Image imgMonkOnGroundNorth;
    private Image imgMonkOnGroundWest;
    private Image imgMonkOnGroundSouth;
    private Image imgMonkOnGroundEast;

    TerritoryPanel(ModelTerritory modelTerritory)
    {
        this.modelTerritory = modelTerritory;
        tileSize = modelTerritory.getTileSize();
        this.setWidth(modelTerritory.getTerritorySizeX() * tileSize);
        this.setHeight(modelTerritory.getTerritorySizeY() * tileSize);

        this.modelTerritory.addObserver(this);

        loadMapResouces();
    }
    public void printTerritory()
    {
        this.setWidth(modelTerritory.getTerritorySizeX() * tileSize);
        this.setHeight(modelTerritory.getTerritorySizeY() * tileSize);

        GraphicsContext gc = this.getGraphicsContext2D();
        for (int i = 0; i < modelTerritory.getTerritorySizeX(); i++)
        {
            for (int j = 0; j < modelTerritory.getTerritorySizeY(); j++)
            {
                if (i == modelTerritory.getMonkPositionX() && j == modelTerritory.getMonkPositionY())
                {
                    switch (modelTerritory.getMonkDirection())
                    {
                        case north:
                            gc.drawImage(imgMonkOnGroundNorth, i * tileSize, j * tileSize);
                            break;
                        case west:
                            gc.drawImage(imgMonkOnGroundWest, i * tileSize, j * tileSize);
                            break;
                        case south:
                            gc.drawImage(imgMonkOnGroundSouth, i * tileSize, j * tileSize);
                            break;
                        case east:
                            gc.drawImage(imgMonkOnGroundEast, i * tileSize, j * tileSize);
                            break;
                    }
                } else
                {
                    switch (modelTerritory.getTerritory()[i][j])
                    {
                        case badKarma:
                            gc.drawImage(imgBadKarmaOnGround, i * tileSize, j * tileSize);
                            break;
                        case hole:
                            gc.drawImage(imgHole, i * tileSize, j * tileSize);
                            break;
                        case ground:
                            gc.drawImage(imgGround, i * tileSize, j * tileSize);
                            break;
                        case goodKarma:
                            gc.drawImage(imgGoodKarmaOnGround, i * tileSize, j * tileSize);
                            break;
                    }
                }
            }
        }
    }

    public void loadMapResouces()
    {
        imgHole = new Image(getClass().getResource("/resources/mapResources/Hole.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgBadKarmaOnGround = new Image(getClass().getResource("/resources/mapResources/BadKarmaOnGround.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgGoodKarmaOnGround = new Image(getClass().getResource("/resources/mapResources/GoodKarmaOnGround.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgGround = new Image(getClass().getResource("/resources/mapResources/Ground.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgMonkOnGroundNorth = new Image(getClass().getResource("/resources/mapResources/MonkOnGroundNorth.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgMonkOnGroundWest = new Image(getClass().getResource("/resources/mapResources/MonkOnGroundWest.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgMonkOnGroundSouth = new Image(getClass().getResource("/resources/mapResources/MonkOnGroundSouth.png").toString(), (double) tileSize, (double) tileSize, true, true);
        imgMonkOnGroundEast = new Image(getClass().getResource("/resources/mapResources/MonkOnGroundEast.png").toString(), (double) tileSize, (double) tileSize, true, true);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if(o == modelTerritory)
        {
            Platform.runLater(() -> printTerritory());
        }
    }

}
