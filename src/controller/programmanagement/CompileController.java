package controller.programmanagement;

import model.ModelMonk;
import model.ModelTerritory;
import view.MainView;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CompileController
{
    private static String errMessage;

    public static boolean isCompilable(String path)
    {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        boolean success = javac.run(null, null, errStream, path) == 0;
        if(success)
        {
            return true;
        }
        else
        {
            errMessage = errStream.toString();
            return false;
        }
    }

    public static void replaceMonkInstance(String pathOfFile, ModelTerritory modelTerritory, MainView mainView)
    {
        try
        {
            File file = new File(pathOfFile);

            // getParentFile is important, because the Class loader needs the directory where the class is and not the class
            URLClassLoader classLoader = new URLClassLoader(new URL[] {
                    file.getParentFile().toURI().toURL()
            });

            ModelMonk modelMonk = (ModelMonk) Class.forName(file.getName().replace(".java", ""), true, classLoader).newInstance();

            modelTerritory.setMonk(modelMonk);
            modelMonk.setTerritory(modelTerritory);

            // set new context menu
            mainView.setMonkPopUpMenu(mainView.getMonkPopUpMenu().generateContent(modelMonk, mainView));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
    public static String getErrMessage(){return errMessage;}
}
