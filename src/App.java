import controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class App extends Application {

    public static void main(String[] args) {

        FileOutputStream out = null;
        FileInputStream in = null;
        try
        {
            out = new FileOutputStream("simulator.properties");
            in = new FileInputStream("simulator.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();
            props.setProperty("role", args[0]);
            props.setProperty("tutorhost", args[1]);
            props.setProperty("tutorport", args[2]);
            props.setProperty("language", args[3]);
            props.store(out, null);
            out.close();
        }
        catch (Exception e)
        {
            FileOutputStream out2 = null;
            try
            {
                out2 = new FileOutputStream("simulator.properties");
                Properties props = new Properties();
                props.setProperty("role", "tutor");
                props.setProperty("tutorhost", "localhost");
                props.setProperty("tutorport", "2345");
                props.setProperty("language", "en");
                props.store(out2, null);
                out.close();
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            finally
            {
                if(out2 != null)
                {
                    try
                    {
                        out2.close();
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        }
        finally
        {
            try
            {
                if(in != null)
                {
                    in.close();
                }
                if(out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        launch(args);
    }

    // this class only sets up the Controller
    public void start(Stage stage) throws InterruptedException
    {
        stage.setTitle("defaultSimulator");
        MainController controller = new MainController(stage);
        controller.start();
    }
}