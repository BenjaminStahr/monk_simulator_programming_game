package model;

public class ModelMonk
{
    private ModelTerritory territory;
    public ModelMonk(ModelTerritory territory)
    {
        this.territory = territory;
    }

    public ModelMonk(){}

    // main method
    public void main(){}

    // the commands
    public void drehLinks()
    {
        territory.turnLeft();
    }
    public void vor()
    {
        territory.forward();
    }
    public void aufnehmen()
    {
        territory.take();
    }
    public void neutralisieren()
    {
        territory.neutralize();
    }

    // the test commands
    public boolean hatGutesKarma(){return territory.hasGoodKarma();}
    public boolean istVornBoden(){return territory.groundInFront();}
    public boolean istVornSchlechtesKarma(){return territory.badKarmaInFront();}
    public boolean istMönchAufGutemKarma(){return territory.goodKarmaThere();}

    public void setTerritory(ModelTerritory territory){this.territory = territory;}
    public ModelTerritory getTerritory(){return territory;}
}
