package model;

import model.modelExceptions.*;
import java.io.Serializable;
import java.util.Observable;

import static model.ModelTerritory.entitys.*;
import static model.ModelTerritory.entitys.badKarma;

public class ModelTerritory extends Observable implements Serializable
{
    //this is the intern representation of the territory
    /**
     *this are the different entitys, which
     * the map can have
     * the monk can walk on ground, can take good karma and
     * neutralize bad karma.
     * Also he's not able to walk on holes
     *
     */
    public enum entitys{badKarma, hole, ground, goodKarma}
    private entitys [][] territory;
    private int territorySizeX;
    private int territorySizeY;
    private int tileSize = 40;

    //directions
    public enum direction{north, west, south, east}

    //the position of the monk
    private int monkPositionX;
    private int monkPositionY;
    private direction monkDirection = direction.north;
    //the monk
    private transient ModelMonk modelMonk;

    //counts how much good Karma a monk has
    private int goodKarmaCount = 0;

    public ModelTerritory(int territorySizeX, int territorySizeY)
    {
        modelMonk = new ModelMonk();
        modelMonk.setTerritory(this);
        this.territorySizeX = territorySizeX;
        this.territorySizeY = territorySizeY;

        this.monkPositionX = 0;
        this.monkPositionY = 0;
        initTerritory(territorySizeX, territorySizeY);
    }

    // here we implement the instructions for testing
    public boolean goodKarmaThere()
    {
        if(territory[monkPositionX][monkPositionY] == entitys.goodKarma)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public boolean badKarmaInFront()
    {
        if(frontInsideGame())
        {
            if (territory[getFrontXPositionOfMonk()][getFrontYPositionOfMonk()] == entitys.badKarma)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    public boolean groundInFront()
    {
        if(frontInsideGame())
        {
            //every field, which is no hole is on ground
            if (territory[getFrontXPositionOfMonk()][getFrontYPositionOfMonk()] != entitys.hole)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    public boolean hasGoodKarma()
    {
        if(goodKarmaCount >= 1 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //instruction to test, though the front of the monk is part of the map
    public boolean frontInsideGame()
    {
        if(getFrontXPositionOfMonk() < 0 || getFrontXPositionOfMonk() > territorySizeX - 1)
        {
            return false;
        }
        if(getFrontYPositionOfMonk() < 0 || getFrontYPositionOfMonk() > territorySizeY - 1)
        {
            return false;
        }
        return true;
    }

    // just notify the Observers
    public void notifyObserversDirectly()
    {
        this.setChanged();
        this.notifyObservers();
    }

    // here are the actions, the monk can perform
    public void turnLeft()
    {
        synchronized (this)
        {
            switch (monkDirection)
            {
                case north:
                    monkDirection = direction.west;
                    break;
                case west:
                    monkDirection = direction.south;
                    break;
                case south:
                    monkDirection = direction.east;
                    break;
                case east:
                    monkDirection = direction.north;
                    break;
            }
        }
        this.setChanged();
        this.notifyObservers();
    }

    public void forward()
    {
        synchronized (this)
        {
            if (!frontInsideGame())
            {
                throw new ActionOutOfMapException("Warum willst du aus der Map laufen?? Das ist verboten");
            }
            if (!groundInFront())
            {
                throw new HoleInFrontException("Du kannst nicht in ein Loch gehen");
            }
            if (badKarmaInFront())
            {
                throw new BadKarmaInFrontException("Du kannst nicht durch schlechtes Karma gehen");
            }
            monkPositionX = getFrontXPositionOfMonk();
            monkPositionY = getFrontYPositionOfMonk();
        }
        this.setChanged();
        this.notifyObservers();
    }
    public void take()
    {
        synchronized(this)
        {
            if (!goodKarmaThere())
            {
                throw new NoGoodKarmaThereException("Man kann kein gutes Karma aufnehmen wo keins ist");
            }
            territory[monkPositionX][monkPositionY] = entitys.ground;
            goodKarmaCount++;
        }
        this.setChanged();
        this.notifyObservers();
    }

    public void neutralize()
    {
        synchronized (this)
        {
            if (!frontInsideGame())
            {
                throw new ActionOutOfMapException("Es gibt kein schlechtes Karma außerhalb der Map...");
            }
            if (!badKarmaInFront())
            {
                throw new NoBadKarmaInFrontException("Man kann kein schlechtes Karma neutralisieren, wo keines ist");
            }
            if (goodKarmaCount < 1)
            {
                throw new NotAbleToNeutralizeException("Ohne gutes Karma kann man kein schlechtes neutralisieren");
            }
            territory[getFrontXPositionOfMonk()][getFrontYPositionOfMonk()] = entitys.ground;
            goodKarmaCount--;
        }
        this.setChanged();
        this.notifyObservers();
    }

    // here just two methods for getting the coordinate of the tile in front of the monk
    public int getFrontXPositionOfMonk()
    {

        if(monkDirection == direction.north)
        {
            return monkPositionX ;
        }
        else if(monkDirection == direction.west)
        {
            return monkPositionX -1 ;
        }
        else if(monkDirection == direction.south)
        {
            return monkPositionX;
        }
        else
        {
            return monkPositionX + 1;
        }
    }

    public int getFrontYPositionOfMonk()
    {
        if(monkDirection == direction.north)
        {
            return monkPositionY - 1;
        }
        else if(monkDirection == direction.west)
        {
            return monkPositionY;
        }
        else if(monkDirection == direction.south)
        {
            return monkPositionY + 1;
        }
        else
        {
            return monkPositionY ;
        }
    }

    // Here are the methods for initalizing and changeing the map
    public void initTerritory(int territorySizeX, int territorySizeY)
    {
        this.territorySizeX = territorySizeX;
        this.territorySizeY = territorySizeY;

        territory = new entitys[territorySizeX][territorySizeY];

        for(int i = 0; i < territorySizeX; i++)
        {
            for(int j = 0; j < territorySizeY; j++)
            {
                territory[i][j] = entitys.ground;
            }
        }
    }

    public synchronized void loadTerritory(entitys [][] map)
    {
        this.territorySizeX = map.length;
        this.territorySizeY = map[0].length;
        territory = new entitys[territorySizeX][territorySizeY];
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                territory[i][j] = map[i][j];
            }
        }
        if(monkOutOfBounds())
        {
            monkPositionX = 0;
            monkPositionY = 0;
            territory[0][0] = entitys.ground;
        }
    }
    public void loadTerritory(entitys[][] map, int monkPositionX, int monkPositionY)
    {
        loadTerritory(map);
        synchronized (this)
        {
            this.monkPositionX = monkPositionX;
            this.monkPositionY = monkPositionY;
        }
    }
    public void resizeTerritory(int territorySizeX, int territorySizeY)
    {
        synchronized (this)
        {
            this.territorySizeX = territorySizeX;
            this.territorySizeY = territorySizeY;
            entitys[][] newTerritory = new entitys[territorySizeX][territorySizeY];

            for (int i = 0; i < territorySizeX; i++)
            {
                for (int j = 0; j < territorySizeY; j++)
                {
                    if (i >= territory.length || j >= territory[0].length)
                    {
                        newTerritory[i][j] = entitys.ground;
                    } else
                    {
                        newTerritory[i][j] = territory[i][j];
                    }

                }
            }
            if (monkOutOfBounds())
            {
                monkPositionX = 0;
                monkPositionY = 0;
                newTerritory[0][0] = entitys.ground;
            }
            territory = newTerritory;
        }
        this.setChanged();
        this.notifyObservers();
    }
    private boolean monkOutOfBounds()
    {
        if(territorySizeX <= monkPositionX || territorySizeY <= monkPositionY)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public entitys[][] generateTestMap()
    {
        entitys[][] map = new entitys[8][8];
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                map[i][j] = hole;
            }
        }
        for(int i = 0; i < map.length; i++)
        {
            map[3][i] = ground;
        }
        for(int i = 0; i < map.length; i++)
        {
            map[i][3] = ground;
        }
        map[3][4] = goodKarma;
        map[4][3] = goodKarma;
        map[0][3] = badKarma;
        map[1][3] = badKarma;
        return map;
    }

    public void setBadKarmaAt(int x, int y)
    {
        synchronized (this)
        {
            territory[x][y] = entitys.badKarma;
        }
        this.setChanged();
        this.notifyObservers();
    }
    public void setHoleAt(int x, int y)
    {
        synchronized (this)
        {
            territory[x][y] = entitys.hole;
        }
        this.setChanged();
        this.notifyObservers();
    }
    public void setGroundAt(int x, int y)
    {
        synchronized (this)
        {
            territory[x][y] = entitys.ground;
        }
        this.setChanged();
        this.notifyObservers();
    }
    public void setGoodKarmaAt(int x, int y)
    {
        synchronized (this)
        {
            territory[x][y] = entitys.goodKarma;
        }
        this.setChanged();
        this.notifyObservers();
    }
    public void setMonkAt(int x, int y)
    {
        synchronized (this)
        {
            monkPositionX = x;
            monkPositionY = y;
        }
        this.setChanged();
        this.notifyObservers();
    }

    // Getters and Setters
    public synchronized void setMonkPositionX(int x){monkPositionX = x;}
    public synchronized void setMonkPositionY(int y){monkPositionY = y;}
    public synchronized void setMonkDirection(direction direction){monkDirection = direction;}
    public synchronized void setMonk(ModelMonk monk){this.modelMonk = monk;}
    public synchronized void setTerritory(entitys[][] entitys){this.territory = entitys;}
    public synchronized void setTerritorySizeX(int territorySizex){this.territorySizeX = territorySizex;}
    public synchronized void setTerritorySizeY(int territorySizeY){this.territorySizeY = territorySizeY;}

    public synchronized entitys[][] getTerritory()
    {
        return territory;
    }
    public synchronized int getMonkPositionX()
    {
        return monkPositionX;
    }
    public synchronized int getMonkPositionY()
    {
        return monkPositionY;
    }
    public synchronized direction getMonkDirection()
    {
        return monkDirection;
    }
    public synchronized int getTerritorySizeX() {return territorySizeX;}
    public synchronized int getTerritorySizeY() {return territorySizeY;}

    public synchronized ModelMonk getModelMonk(){return modelMonk;}

    public synchronized int getTileSize(){return tileSize;}
}
