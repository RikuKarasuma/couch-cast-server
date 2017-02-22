package net.eureka.androidcast.gui;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.eureka.androidcast.foundation.init.ApplicationGlobals;
import net.eureka.androidcast.gui.net.NetSelector;
import net.eureka.androidcast.gui.tray.Tray;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public final class Menu extends Application
{
	private static boolean mainMenu = true;
	
	private static boolean netError = false;
	
	private Stage menu = null;
	
	private MainMenu main;
	
	private SettingsMenu settings;
	
	@Override
	public void start(Stage primaryStage)
	{
		try 
		{
			Platform.setImplicitExit(false);
			menu = primaryStage;
			menu.setTitle(ApplicationGlobals.getNameAndVersion());
			menu.initStyle(StageStyle.UNDECORATED);
			
			//System.out.println(netError);
			// turn off neterror until it's completion
			netError = false;
			if(netError)
				createNetMenu();
			else
				createMainMenu();
			
			//primaryStage.show();
			//openWebpage(new URI("http://www.softwareeureka.net"));
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void createNetMenu()
	{
		menu.setMinHeight(375);
		menu.setMinWidth(700);
		menu.setWidth(700);
		menu.setHeight(375);
		menu.setScene(new NetSelector(this));
		menu.show();	
	}
	
	private void createMainMenu()
	{
		menu.setMinHeight(675);
		menu.setMinWidth(700);
		menu.setWidth(700);
		menu.setHeight(675);
		main = new MainMenu(this);
		menu.setScene(main);
		menu.setOnCloseRequest(new EventHandler<WindowEvent>() 
		{
            @Override
            public void handle(WindowEvent t) {
                destroy();
            }
        });
		Tray.setGUI(this);
	}

	public void setUp()
	{
		Platform.runLater(new Runnable()
		{	
			@Override
			public void run()
			{
				if(menu.isShowing())
					destroy();
				else
					recreate();
			}
		});	
	}
	
	private void destroy()
	{
		menu.hide();
		menu.setScene(null);
		main = null;
		settings = null;
	}
	
	private void recreate()
	{
		if(mainMenu)
		{
			main = new MainMenu(Menu.this);
			menu.setScene(main);
		}
		else
		{
			settings = new SettingsMenu(Menu.this);
			menu.setScene(settings);
		}
		menu.show();
	}
	
	public void switchToSettings()
	{
		if(settings == null)
			settings = new SettingsMenu(this);
		
		menu.setScene(settings);
		
		main = null;
		mainMenu = false;
	}
	
	public void switchToMain()
	{
		if(main == null)
			main = new MainMenu(this);
		
		menu.setScene(main);
		settings = null;
		mainMenu = true;
	}
	
	public void update()
	{
		if(main != null)
			main.updatePlaylist();
	}
	
	public Stage getStage()
	{
		return menu;
	}
	
	public static void initialise(boolean arch_error, boolean net_error)
	{
		if(!arch_error)
		{
			netError = net_error;
			launch(Menu.class);
		}
		else
			launch(ArchError.class);
	}
	
	public static void initialise()
	{
		launch(Menu.class);
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	public static void openWebpage(URI uri) 
	{
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) 
	        try 
	    	{
	            desktop.browse(uri);
	        } 
	    	catch (Exception e) 
	    	{
	            e.printStackTrace();
	        }
	    
	}

	public static void openWebpage(URL url) 
	{
	    try 
	    {
	        openWebpage(url.toURI());
	    }
	    catch (URISyntaxException e) 
	    {
	        e.printStackTrace();
	    }
	}
}
