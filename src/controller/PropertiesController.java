package controller;

import java.io.*;
import java.util.Properties;

public  class PropertiesController
{


    public static Properties getProperties()
    {
        Properties properties = new Properties();
        InputStream input = null;

        try
        {
            input = new FileInputStream("simulator.properties");
            // load a properties file
            properties.load(input);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return properties;
        }
    }

    public static void setLanguage(String language)
    {
        FileOutputStream out = null;
        FileInputStream in = null;
        try
        {
            in = new FileInputStream("simulator.properties");
            Properties props = new Properties();
            props.load(in);
            String tutorPort = props.getProperty("tutorport");
            String tutorHost = props.getProperty("tutorhost");
            String role = props.getProperty("role");
            out = new FileOutputStream("simulator.properties");
            props.setProperty("tutorport", tutorPort);
            props.setProperty("tutorhost", tutorHost);
            props.setProperty("language", language);
            props.setProperty("role", role);
            props.store(out, null);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e) { }
            }
            if(in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
 }
