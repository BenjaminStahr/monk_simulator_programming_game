package controller;

import controller.programmanagement.FileController;
import model.ModelTerritory;
import view.MainView;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
public class DatabaseController
{
    public static String dbName = "exampleDatabase";

    // generating Tables
    private static final String createExampleTable = "CREATE TABLE Example (e_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                                       " code LONG VARCHAR, map LONG VARCHAR)";
    private static final String createTagTable = "CREATE TABLE Tag (name VARCHAR(64), example_id INT) ";

    // different statements
    private static final String insertExample = "INSERT INTO Example (code, map) VALUES (?, ?) ";
    private static final String insertTag = "INSERT INTO Tag (name, example_id) VALUES (?, ?)";
    private static final String getExamplesForTags = "SELECT Example.e_id FROM Example JOIN Tag ON" +
                                                     " Example.e_id = Tag.example_id WHERE Tag.name = ?";

    private static final String getExample = "SELECT * FROM Example WHERE e_id = ?";

    private static Connection connection;

    public static Connection getConnection()
    {
        try
        {
            if(connection == null || !connection.isValid(0))
            {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                connection = DriverManager.getConnection("jdbc:derby:"
                             + dbName + ";create=true");
                return connection;
            }
            else
            {
                return connection;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void createDatabase()
    {
        Statement stmt = null;
        try
        {
            if(!checkTablesExist())
            {
                stmt = getConnection().createStatement();
                stmt.execute(createExampleTable);
                stmt.execute(createTagTable);
                stmt.close();
            }
        }
        catch (Throwable th)
        {
            th.printStackTrace();
        }
        finally
        {
            try
            {
                if(stmt != null)
                {
                    stmt.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // insert with its tags
    public static void insertExample(String code, String xmlTerritory, String tags)
    {
        PreparedStatement stmt = null;
        ResultSet maxId = null;
        try
        {
            getConnection().setAutoCommit(false);
            stmt = getConnection().prepareStatement(insertExample,PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, code);
            stmt.setString(2, xmlTerritory);
            stmt.executeUpdate();

            // getting highest Id for tags
            maxId = stmt.getGeneratedKeys();
            int highestId = 0;
            if (maxId.next()) {
                highestId = maxId.getInt(1);
            }

            String[] tagsInArray = tags.split(" ");

            for(int i = 0; i < tagsInArray.length; i++)
            {
                PreparedStatement stmtTag = getConnection().prepareStatement(insertTag);
                stmtTag.setString(1, tagsInArray[i]);
                stmtTag.setInt(2, highestId);
                stmtTag.executeUpdate();
                // declaring all statements earlier would be to much overheat
                stmtTag.close();
            }
            getConnection().commit();
        }
        catch (Throwable th)
        {
            th.printStackTrace();
            try {
                if (getConnection() != null)
                {
                    getConnection().rollback();
                }
            } catch (SQLException e) {
            }
        }
        finally
        {
            try
            {
                //reset connection
                if (getConnection() != null)
                {
                    getConnection().setAutoCommit(true);
                }
            }
            catch (SQLException e) { }
            try
            {
                // close statement
                if (stmt != null)
                {
                    stmt.close();
                }
                if(maxId != null)
                {
                    maxId.close();
                }
            }
            catch (SQLException e) { }
        }
    }

    public static boolean checkTablesExist()
    {
        ResultSet tableNames = null;
        try
        {
            DatabaseMetaData metadata = getConnection().getMetaData();
            String[] names = {"TABLE"};
            tableNames = metadata.getTables(null, null, null, names);

            if(tableNames.next())
            {
                tableNames.close();
                return true;
            }
            tableNames.close();
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if(tableNames != null)
            {
                try
                {
                    tableNames.close();
                }
                catch (SQLException e) { }
            }
        }
    }

    public static ArrayList<String> getNamesOfProgramsForTag(String tag)
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try
        {
            stmt = getConnection().prepareStatement(getExamplesForTags);
            stmt.setString(1, tag);
            resultSet = stmt.executeQuery();

            ArrayList<String> names = new ArrayList<>();
            while (resultSet.next())
            {
                names.add(String.valueOf(resultSet.getInt(1)));
            }
            return names;
        }
        catch (Throwable th)
        {
            th.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if(resultSet != null)
                {
                    resultSet.close();
                }
                if(stmt != null)
                {
                    stmt.close();
                }
            }
            catch (SQLException e) { }

        }
    }
    public static void setExampleToEnviroment(int idOfExample, ModelTerritory model, MainView view)
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try
        {
            stmt = getConnection().prepareStatement(getExample);
            stmt.setInt(1, idOfExample);
            resultSet = stmt.executeQuery();

            // now we are at the desired Example
            resultSet.next();

            String program = resultSet.getString(2);
            String territory = resultSet.getString(3);

            // Here we getting the a xmlReader to read the String from the Database
            view.getEditor().setText(program);
            Reader reader = new StringReader(territory);
            XMLInputFactory factory = XMLInputFactory.newInstance(); // Or newFactory()
            XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
            FileController.setTerritoryFromXmlFile(model, xmlReader);
            model.notifyObserversDirectly();
        }
        catch (Throwable th)
        {
            th.printStackTrace();
        }
        finally
        {
            try
            {
                if(stmt != null)
                {
                    stmt.close();
                }
                if(resultSet != null)
                {
                    resultSet.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void closeDatabaseConnection()
    {
        try
        {
            DriverManager.getConnection("jdbc:derby:exampleDatabase;shutdown=true");
        }
        catch (SQLException e) { }
        finally
        {
            try
            {
                getConnection().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}
