package view;

import annotations.Invisible;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import model.ModelMonk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MonkPopUpMenu extends ContextMenu
{
    public MonkPopUpMenu generateContent(ModelMonk monk, MainView view)
    {
        MonkPopUpMenu monkPopUpMenu= generateStandardMenu(monk, view);
            for (Method method : monk.getClass().getDeclaredMethods())
            {
                int mod = method.getModifiers();
                if (!Modifier.isPrivate(mod) && !Modifier.isStatic(mod) && !Modifier.isAbstract(mod) && method.getAnnotation(Invisible.class) == null)
                {
                    MenuItem item = buildMenuItemForMethod(method);
                    setEventListenerForItem(item, method, monk, view);
                    monkPopUpMenu.getItems().add(item);
                }
            }
        return monkPopUpMenu;
    }

    public MonkPopUpMenu generateBasicContent(ModelMonk monk, MainView view)
    {
        return generateStandardMenu(monk, view);
    }

    public MonkPopUpMenu generateStandardMenu(ModelMonk monk, MainView view)
    {
        MonkPopUpMenu monkPopUpMenu = new MonkPopUpMenu();
        try
        {
            Method method1 = monk.getClass().getMethod("drehLinks");
            MenuItem item1 = buildMenuItemForMethod(method1);
            setEventListenerForItem(item1,method1, monk, view);
            monkPopUpMenu.getItems().add(item1);

            Method method2 = monk.getClass().getMethod("vor");
            MenuItem item2 = buildMenuItemForMethod(method2);
            setEventListenerForItem(item2,method2, monk, view);
            monkPopUpMenu.getItems().add(item2);

            Method method3 = monk.getClass().getMethod("aufnehmen");
            MenuItem item3 = buildMenuItemForMethod(method3);
            setEventListenerForItem(item3,method3, monk, view);
            monkPopUpMenu.getItems().add(item3);

            Method method4 = monk.getClass().getMethod("neutralisieren");
            MenuItem item4 = buildMenuItemForMethod(method4);
            setEventListenerForItem(item4,method4, monk, view);
            monkPopUpMenu.getItems().add(item4);

            monkPopUpMenu.getItems().add(new SeparatorMenuItem());

            Method method5 = monk.getClass().getMethod("hatGutesKarma");
            MenuItem item5 = buildMenuItemForMethod(method5);
            setEventListenerForItem(item5,method5, monk, view);
            monkPopUpMenu.getItems().add(item5);

            Method method6 = monk.getClass().getMethod("istVornBoden");
            MenuItem item6 = buildMenuItemForMethod(method6);
            setEventListenerForItem(item6,method6, monk, view);
            monkPopUpMenu.getItems().add(item6);

            Method method7 = monk.getClass().getMethod("istVornSchlechtesKarma");
            MenuItem item7 = buildMenuItemForMethod(method7);
            setEventListenerForItem(item7,method7, monk, view);
            monkPopUpMenu.getItems().add(item7);

            Method method8 = monk.getClass().getMethod("istMönchAufGutemKarma");
            MenuItem item8 = buildMenuItemForMethod(method8);
            setEventListenerForItem(item8,method8, monk, view);
            monkPopUpMenu.getItems().add(item8);

            monkPopUpMenu.getItems().add(new SeparatorMenuItem());
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        finally
        {
            return monkPopUpMenu;
        }
    }

    public MenuItem buildMenuItemForMethod(Method method)
    {
        // returntype
        String methodSignature = method.getReturnType().getName() + " ";
        // Method name
        methodSignature += method.getName() + " (";

        // parameters
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0)
        {
            for (int k = 0; k < parameterTypes.length; k++)
            {
                methodSignature += parameterTypes[k].getName();
                if (k < parameterTypes.length - 1)
                    methodSignature += ", ";
            }
            methodSignature += " )";
            MenuItem menuItemWithParameter = new MenuItem(methodSignature);
            menuItemWithParameter.setDisable(true);
            return menuItemWithParameter;
        }
        else
        {
            methodSignature += " )";
            method.setAccessible(true);
            MenuItem item = new MenuItem(methodSignature);
            return item;
        }
    }

    public void setEventListenerForItem(MenuItem item, Method method, ModelMonk monk, MainView view)
    {
        item.setOnAction(event ->
        {
            try
            {
                monk.getTerritory().deleteObserver(view.getTerritoryPanel());
                method.invoke(monk);
                monk.getTerritory().addObserver(view.getTerritoryPanel());
                monk.getTerritory().notifyObserversDirectly();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                monk.getTerritory().addObserver(view.getTerritoryPanel());
                monk.getTerritory().notifyObserversDirectly();

                Platform.runLater(()->
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("fehlerhafte Ausführung einer Methode");
                    alert.setContentText(e.getTargetException().getMessage());
                    alert.show();
                });
            }
        });
    }
}
