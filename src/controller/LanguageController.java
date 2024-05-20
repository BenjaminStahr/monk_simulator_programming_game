package controller;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class LanguageController
{
    private static Locale locale;
    private static ResourceBundle bundle;
    public static ObjectProperty<ResourceBundle> resourcesProperty = new SimpleObjectProperty<>();

    public static void updateLanguage()
    {
         Properties prop = PropertiesController.getProperties();
        try
        {
            String language = prop.getProperty("language");
            if (language == null)
            {
                LanguageController.locale = Locale.getDefault();
            }
            else
            {
                LanguageController.locale = new Locale(language);
            }
        }
        catch (Throwable e)
        {
            LanguageController.locale = Locale.getDefault();
        }
        Locale.setDefault(LanguageController.locale);
        LanguageController.bundle = ResourceBundle.getBundle("resources.toolbar", LanguageController.locale);
        resourcesProperty.set(bundle);
    }


    // binds the string in the resource bundle to the resource property
    // every time the resource property gets updated, the strings get changed
    public static StringBinding getStringBinding(String key)
    {
        return new StringBinding()
        {
            {
                bind(resourcesProperty);
            }
            @Override
            public String computeValue()
            {
                return resourcesProperty.get().getString(key);
            }
        };
    }

    public static String getResourceString(String key)
    {
        try
        {
            return LanguageController.bundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }

    public static Locale getLocale(){return locale;}
}
