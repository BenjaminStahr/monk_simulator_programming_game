package model;

import model.modelExceptions.MonkException;

import java.util.Scanner;

import static model.ModelTerritory.entitys.*;

public class ModelTestGame
{
    static ModelTerritory territory;
    static ModelMonk monk;

    public static void main (String[] args)
    {
        territory = new ModelTerritory(0,0);
        territory.loadTerritory(generateTestMap(), 3, 0);
        monk = new ModelMonk(territory);
        showMap();

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            try
            {
                System.out.println("drehLinks: l, vor: v, aufnehmen: a, neutralisieren: n");
                String eingabe = scanner.next();
                switch (eingabe)
                {
                    case "l":
                        monk.drehLinks();
                        break;
                    case "v":
                        monk.vor();
                        break;
                    case "a":
                        monk.aufnehmen();
                        break;
                    case "n":
                        monk.neutralisieren();
                        break;
                    default:
                        System.out.println("ungültige Eingabe");
                }
                showMap();
            }
            catch (MonkException ex)
            {
                showMap();
                System.out.println(ex.getMessage());
            }
        }
    }

    public static ModelTerritory.entitys[][] generateTestMap()
    {
        ModelTerritory.entitys[][] map = new ModelTerritory.entitys[5][5];
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

    public static void showMap()
    {
        for(int i = 0; i < territory.getTerritorySizeX(); i++)
        {
            for(int j = 0; j < territory.getTerritorySizeY(); j++)
            {
                if(i == territory.getMonkPositionX() && j == territory.getMonkPositionY())
                {
                    switch (territory.getMonkDirection())
                    {
                        case north: System.out.print("<");
                            break;
                        case west: System.out.print("v");
                            break;
                        case south: System.out.print(">");
                            break;
                        case east: System.out.print("^");
                            break;
                    }
                }
                else
                {
                    switch (territory.getTerritory()[i][j])
                    {
                        case badKarma: System.out.print("B");//bad karma
                            break;
                        case hole: System.out.print("O");//hole
                            break;
                        case ground: System.out.print("X");//ground
                            break;
                        case goodKarma: System.out.print("G");//good Karma
                            break;
                    }
                }
            }
            System.out.println();
        }
    }
}
