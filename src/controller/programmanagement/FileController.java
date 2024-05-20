package controller.programmanagement;

import controller.DatabaseController;
import controller.PropertiesController;
import javafx.application.Platform;
import model.ModelTerritory;
import view.MainView;

import javax.xml.stream.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FileController
{
    private  static String sep = System.getProperty("file.separator");
    private static String programDirectory = System.getProperty("user.dir")+ sep + "src" + sep + "programme";
    private static  String territoryDirectory = System.getProperty("user.dir")+ sep + "src" + sep + "territorys";
    private static String territoryXmlDirectory = System.getProperty("user.dir")+ sep + "src" +
              sep + "xml" + sep + "xmlTerritorys";
    private static String dtdPath = System.getProperty("user.dir")+ sep + "src" +
             sep + "xml" + sep + "territory.dtd";

    // Here are all openend programs stored
    private static ArrayList<Program> openedPrograms = new ArrayList<Program>();

    // creates userdirectory, if it doesn't exist
    public static void makeUserDirectory()
    {
        Path dirPath = Paths.get(programDirectory);
        try
        {
            Files.createDirectory(dirPath);
        }
        catch(IOException ex)
        {
            System.out.println("File already exists : " + ex.getMessage());
        }
        Path dirPathTerritorys = Paths.get(territoryDirectory);
        try
        {
            Files.createDirectory(dirPathTerritorys);
        }
        catch(IOException ex)
        {
            System.out.println("File already exists : " + ex.getMessage());
        }
        Path dirPathXmlTerritorys = Paths.get(territoryXmlDirectory);
        try
        {
            Files.createDirectory(dirPathXmlTerritorys);
        }
        catch (IOException ex)
        {
            System.out.println("File already exists : " + ex.getMessage());
        }
    }
    // creates a new file with the standard text
    public static void createFileInUserDirectory(String fileName)
    {
        Path fileDirectory = Paths.get(programDirectory + sep + fileName + ".java");
        try
        {
                Files.createFile(fileDirectory);
                String initText = "import annotations.Invisible;"+"\n"+
                                  "public class "+fileName+" extends model.ModelMonk { " +
                        "          public"+"\n"+"void main() {"+ "\n"+"}"+"\n"+"}";
                Files.write(fileDirectory, initText.getBytes());
        }
        catch (IOException ex)
        {
            System.out.println("File already exists : " + ex.getMessage());
        }
    }

    // this method opens a new instance of the simulator, with a program attached
    public static void openNewProgram(MainView view, String title)
    {
        openedPrograms.add(new Program(title));
        printProgramToEditor(view, title);
    }

    // closes a program
    public static void closeProgram(MainView mainView, Remote controller)
    {
        for(int i = 0; i < openedPrograms.size(); i++)
        {
            if(openedPrograms.get(i).getName().equals(mainView.getStage().getTitle()))
            {
                if(PropertiesController.getProperties().getProperty("role").equals("tutor"))
                {
                    try
                    {
                        UnicastRemoteObject.unexportObject(controller, true);
                    }
                    catch (NoSuchObjectException e)
                    {
                        System.out.println("Doesn't need to export, because : " + e.getMessage());
                    }
                }
                openedPrograms.remove(i);
                // shutdown if theres no other opened application
                if(openedPrograms.size() == 0)
                {
                    DatabaseController.closeDatabaseConnection();
                    saveDataFromEditor(mainView);
                    Platform.exit();
                }
                saveDataFromEditor(mainView);
                mainView.getStage().close();
                break;
            }
        }

    }

    public static void saveDataFromEditor(MainView mainView)
    {
        Path saveDirectory = Paths.get(programDirectory + sep + mainView.getStage().getTitle() + ".java");
        byte[] saveTextAsBytes = mainView.getEditor().getText().replaceAll("\n", System.getProperty("line.separator")).getBytes();

        try
        {
            // Getting first line from file
            byte[] firstLine = (Files.readAllLines(saveDirectory).get(0)+"\n").getBytes();
            byte[] secLine = (Files.readAllLines(saveDirectory).get(1)+"\n").getBytes();
            // deletes content of file
            new PrintWriter(saveDirectory.toFile()).close();

            Files.write(saveDirectory, firstLine, StandardOpenOption.APPEND);
            Files.write(saveDirectory , secLine, StandardOpenOption.APPEND);
            Files.write(saveDirectory, saveTextAsBytes, StandardOpenOption.APPEND);
            Files.write(saveDirectory, "\n}".getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void printProgramToEditor(MainView mainView, String title)
    {
        Path path = Paths.get(programDirectory + sep + mainView.getStage().getTitle() + ".java");
        try
        {
            List<String> lines = Files.readAllLines(path);

            // first and last element should not be shown on the editor
            for(int i = 2; i < lines.size() - 1; i++)
            {
                mainView.getEditor().appendText(lines.get(i) + "\n");
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void saveTerritoryAsXml(Path path, ModelTerritory model)
    {
        try {
            File xmlFile = path.toFile();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(new FileOutputStream(xmlFile.getAbsolutePath()), "utf-8");
            // Der XML-Header wird erzeugt
            writeXmlIntoSink(writer, model);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
    }

    public static String saveTerritoryAsString(ModelTerritory model)
    {
        Writer writer = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try
        {
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
            writeXmlIntoSink(xmlWriter, model);
            String territory = ((StringWriter) writer).getBuffer().toString();
            return territory;
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeXmlIntoSink(XMLStreamWriter writer, ModelTerritory model)
    {
        try
        {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeDTD("<!DOCTYPE territory SYSTEM \""+ dtdPath +"\">");
            writer.writeCharacters("\n");

            writer.writeStartElement("territory");
            writer.writeCharacters("\n");
            for(int i = 0; i < model.getTerritorySizeY(); i++)
            {
                writer.writeStartElement("row");
                writer.writeCharacters("\n");
                for(int j = 0; j < model.getTerritorySizeX(); j++)
                {
                    writer.writeStartElement("tile");
                    writer.writeCharacters("\n");

                    // xPos
                    writer.writeStartElement("xPos");
                    writer.writeCharacters(String.valueOf(i));
                    writer.writeEndElement();
                    writer.writeCharacters("\n");

                    // yPos
                    writer.writeStartElement("yPos");
                    writer.writeCharacters(String.valueOf(j));
                    writer.writeEndElement();
                    writer.writeCharacters("\n");

                    writer.writeStartElement("content");
                    switch(model.getTerritory()[i][j])
                    {
                        case ground: writer.writeCharacters("ground");
                            break;
                        case goodKarma: writer.writeCharacters("goodKarma");
                            break;
                        case badKarma: writer.writeCharacters("badKarma");
                            break;
                        case hole: writer.writeCharacters("hole");
                            break;
                    }
                    writer.writeEndElement(); // content
                    writer.writeCharacters("\n");
                    writer.writeEndElement(); // tile
                    writer.writeCharacters("\n");
                }
                writer.writeEndElement(); // row
                writer.writeCharacters("\n");
            }

            writer.writeCharacters("\n");
            writer.writeStartElement("territorySizeX");
            writer.writeCharacters(String.valueOf(model.getTerritorySizeX()));
            writer.writeEndElement(); // territorySizeX

            writer.writeCharacters("\n");
            writer.writeStartElement("territorySizeY");
            writer.writeCharacters(String.valueOf(model.getTerritorySizeY()));
            writer.writeEndElement(); // territorySizeY

            writer.writeCharacters("\n");
            writer.writeStartElement("monkPositionX");
            writer.writeCharacters(String.valueOf(model.getMonkPositionX()));
            writer.writeEndElement(); // monkPositionX

            writer.writeCharacters("\n");
            writer.writeStartElement("monkPositionY");
            writer.writeCharacters(String.valueOf(model.getMonkPositionY()));
            writer.writeEndElement(); // monkPositionY

            writer.writeCharacters("\n");
            writer.writeStartElement("monkDirection");
            switch(model.getMonkDirection())
            {
                case north:writer.writeCharacters("north");
                    break;
                case west:writer.writeCharacters("west");
                    break;
                case south:writer.writeCharacters("south");
                    break;
                case east:writer.writeCharacters("east");
                    break;
            }
            writer.writeEndElement(); // monkdirection
            writer.writeCharacters("\n");
            writer.writeEndElement(); // territory
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
    }


    // this method sets the territory from a file
    public static void loadTerritoryAsXml(Path path, ModelTerritory model)
    {
        try
        {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory
                    .createXMLStreamReader(new FileInputStream(path.toFile()));
            // xml doesnt gets validated, but its okay because stax doesnt support validation
            setTerritoryFromXmlFile(model, parser);
        }
        catch (Throwable exc)
        {
            exc.printStackTrace();
        }
    }

    // this method sets the territory to a specied one regarding an xmlString or xmlFile
    public static void setTerritoryFromXmlFile(ModelTerritory model, XMLStreamReader parser) throws XMLStreamException
    {
        ModelTerritory tmpModel = new ModelTerritory(1,1);
        while (parser.hasNext())
        {
            switch (parser.getEventType())
            {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    parser.close();
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    String element = parser.getLocalName();
                    if ("tile".equals(element))
                    {
                        parser.next();
                        parser.next();// now comes x coordinate
                        int x = Integer.parseInt(parser.getElementText());
                        parser.next();
                        parser.next();
                        int y = Integer.parseInt(parser.getElementText());

                        if(x == tmpModel.getTerritorySizeX())
                        {
                            // index x+1, because we begin counting at 0
                            tmpModel.resizeTerritory(x+1, tmpModel.getTerritorySizeY());
                        }
                        if(y == tmpModel.getTerritorySizeY())
                        {
                            tmpModel.resizeTerritory(tmpModel.getTerritorySizeX(), y+1);
                        }
                        parser.next();
                        parser.next();
                        String entity = parser.getElementText();
                        switch (entity)
                        {
                            case "hole": tmpModel.getTerritory()[x][y] = ModelTerritory.entitys.hole;
                                break;
                            case "badKarma": tmpModel.getTerritory()[x][y] = ModelTerritory.entitys.badKarma;
                                break;
                            case "goodKarma": tmpModel.getTerritory()[x][y] = ModelTerritory.entitys.goodKarma;
                                break;
                            case "ground": tmpModel.getTerritory()[x][y] = ModelTerritory.entitys.ground;
                                break;
                        }
                    }
                    else if ("territorySizeX".equals(element))
                    {
                        tmpModel.setTerritorySizeX(Integer.parseInt(parser.getElementText()));
                    }
                    else if("territorySizeY".equals(element))
                    {
                        tmpModel.setTerritorySizeY(Integer.parseInt(parser.getElementText()));
                    }
                    else if("monkPositionX".equals(element))
                    {
                        tmpModel.setMonkPositionX(Integer.parseInt(parser.getElementText()));
                    }
                    else if("monkPositionY".equals(element))
                    {
                        tmpModel.setMonkPositionY(Integer.parseInt(parser.getElementText()));
                    }
                    else if("monkDirection".equals(element))
                    {
                        switch(parser.getElementText())
                        {
                            case "north": tmpModel.setMonkDirection(ModelTerritory.direction.north);
                                break;
                            case "west": tmpModel.setMonkDirection(ModelTerritory.direction.west);
                                break;
                            case "south": tmpModel.setMonkDirection(ModelTerritory.direction.south);
                                break;
                            case "east": tmpModel.setMonkDirection(ModelTerritory.direction.east);
                                break;
                        }
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    break;
                default:
                    break;
            }
            parser.next();
        }
        // copy only if everything was parsed correctly
        copyTerritoryValues(tmpModel, model);
    }

    public static void copyTerritoryValues(ModelTerritory oldTerritory, ModelTerritory newTerritory)
    {
        newTerritory.setTerritory(oldTerritory.getTerritory());
        newTerritory.setTerritorySizeX(oldTerritory.getTerritorySizeX());
        newTerritory.setTerritorySizeY(oldTerritory.getTerritorySizeY());
        newTerritory.setMonkPositionX(oldTerritory.getMonkPositionX());
        newTerritory.setMonkPositionY(oldTerritory.getMonkPositionY());
        newTerritory.setMonkDirection(oldTerritory.getMonkDirection());
    }


    public static void saveTerritory(Path path, ModelTerritory territory)
    {
        FileOutputStream fs;
        ObjectOutputStream os = null;
        try
        {
            fs = new FileOutputStream(path.toFile());
            os = new ObjectOutputStream(fs);
            os.writeObject(territory);
        }
        catch (IOException e)
        {
            System.err.println(e.toString());
        }
        catch (Throwable e)
        {
            System.err.println(e.toString());
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e) { }
            }
        }
    }


    // loads a serialized territory from a file
    public static void loadTerritory(Path path, ModelTerritory model)
    {
        FileInputStream fs;
        ObjectInputStream is = null;

        try
        {
            fs = new FileInputStream(path.toFile());
            is = new ObjectInputStream(fs);

            ModelTerritory loadTerritory = (ModelTerritory) is.readObject();
            copyTerritoryValues(loadTerritory, model);
            model.notifyObserversDirectly();
        }
        catch (ClassNotFoundException e)
        {
            System.err.println(e.toString());
        }
        catch (IOException e)
        {
            System.err.println(e.toString());
        }
        catch (Throwable e)
        {
            System.err.println(e.toString());
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e) { }
            }
        }
    }

    // helper methods

    // looks if a program is already opened
    public static boolean isProgramOpened(String title)
    {
        for(int i = 0; i < openedPrograms.size(); i++)
        {
            if(openedPrograms.get(i).getName().equals(title))
            {
                return true;
            }
        }
        return false;
    }

    public static String getSep(){return sep;}
    public static String getProgramDirectory(){return programDirectory;};
    public static Path getFileDirectory(String name){return Paths.get(programDirectory + sep + name + ".java");}
    public static Path getClassFileDirectory(String name){return Paths.get(programDirectory + sep + name + ".class");}
    public static String getTerritoyDirectory(){return territoryDirectory;}
    public static String getTerritoryXmlDirectory(){return territoryXmlDirectory;}
}
