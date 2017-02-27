package net.eureka.couchcast.gui;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.InitialiseFoundation;
import net.eureka.couchcast.gui.net.NetSelector;
import net.eureka.couchcast.gui.playlist.PlaylistViewer;
import net.eureka.couchcast.gui.tray.Tray;

/**
 * Used as a starting point for all GUI interactions that doesn't have to do with {@link Tray}.
 * Javafx is used to provide a frontend suite. 
 * @author Owen McMonagle.
 *
 */
public final class Menu extends Application
{
	
	/**
	 * Used to indicate which menu the gui is on.
	 */
	private static boolean mainMenu = true;
	
	
	/**
	 * Used to signal that no DHCP interface has been found in {@link InitialiseFoundation}.
	 */
	private static boolean netError = false;
	
	
	/**
	 * Primary application stage.
	 */
	private Stage menu = null;
	
	
	/**
	 * Main Menu.
	 */
	private MainMenu main;
	
	/**
	 * Setting menu.
	 */
	private SettingsMenu settings;
	
	
	
	/**
	 * Sets up the initial application menus. Starts network interface selection,
	 * if none has been discovered.  
	 * @param primaryStage - Primary application stage for javafx.
	 */
	@Override
	public void start(Stage primaryStage)
	{
		try 
		{
			// Set implicitExit to false so the jvm won't close on window exit.
			Platform.setImplicitExit(false);
			// Set primary stage as global.
			menu = primaryStage;
			// Set title of window.
			menu.setTitle(ApplicationGlobals.getNameAndVersion());
			// Set style of window to undecorated.
			menu.initStyle(StageStyle.UNDECORATED);
			// Set window icon.
			menu.getIcons().add(new Image("file:"+ApplicationGlobals.getInstallPath()+"logo.png"));
			// If network interface error occurs....
			if(netError)
				// Create network selection menu.
				createNetMenu();
			// If no network issues occur at all..
			else
				// Create main menu.
				createMainMenu();
			
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the network interface selection menu. This only occurs when
	 * no interface has been found in the configuration menu. The {@link NetSelector}
	 * is the {@link Scene} set.
	 */
	private void createNetMenu()
	{
		menu.setMinHeight(375);
		menu.setMinWidth(700);
		menu.setWidth(700);
		menu.setHeight(375);
		menu.setScene(new NetSelector(menu, this));
		menu.show();	
	}
	
	/**
	 * Creates the default {@link MainMenu}. This hosts the found file playlist,
	 * along with other buttons such as settings, load and help.
	 */
	public void createMainMenu()
	{
		menu.setMinHeight(675);
		menu.setMinWidth(700);
		menu.setWidth(700);
		menu.setHeight(675);
		main = new MainMenu(menu, this);
		menu.setScene(main);
		menu.setOnCloseRequest(new EventHandler<WindowEvent>() 
		{
			@Override
			public void handle(WindowEvent t) 
			{
				destroy();
			}
		});
		// Gives the Tray object a pointer to the main menu.
		Tray.setGUI(this);
	}

	/**
	 *  Used by the {@link Tray} class to set up either {@link MainMenu} or
	 *  the {@link SettingsMenu} whenever the TrayIcon is clicked upon on
	 *  the Windows desktop.
	 */
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
	
	/**
	 * Destroys the current {@link Scene}.
	 */
	private void destroy()
	{
		menu.hide();
		menu.setScene(null);
		main = null;
		settings = null;
	}
	
	/**
	 * Recreates the current {@link Scene} based on the mainMenu flag. If the 
	 * flag is true, the {@link MainMenu} is set, if false, the {@link SettingsMenu}
	 * is set. Finally the set Scene is shown. 
	 */
	private void recreate()
	{
		if(mainMenu)
		{
			main = new MainMenu(menu, Menu.this);
			menu.setScene(main);
		}
		else
		{
			settings = new SettingsMenu(menu, Menu.this);
			menu.setScene(settings);
		}
		menu.show();
	}
	
	/**
	 * Switches to the {@link SettingsMenu}. Used by the {@link MainMenu} for
	 * easy switching.
	 */
	public void switchToSettings()
	{
		if(settings == null)
			settings = new SettingsMenu(menu, this);
		
		menu.setScene(settings);
		
		main = null;
		mainMenu = false;
	}
	
	/**
	 * Switches to the {@link MainMenu}. Used by the {@link SettingsMenu} for
	 * easy switching.
	 */
	public void switchToMain()
	{
		if(main == null)
			main = new MainMenu(menu, this);
		
		menu.setScene(main);
		settings = null;
		mainMenu = true;
	}
	
	/**
	 * Updates the {@link PlaylistViewer} via {@link Tray}. Tray is signalled
	 * from {@link DirectoryFactory}.
	 */
	public void update()
	{
		if(main != null)
			main.updatePlaylist();
	}
	
	/**
	 * Initialises the GUI. 
	 * <br><br>
	 *	If arch_error is true, launches error message stating that you have the 
	 *	wrong architecture for your system. i.e x64 on x86.
	 * <br><br>
	 * 	If net_error is true, launches the network interface selection menu.
	 * 
	 *  
	 * @param arch_error - Whether or not an arch error has occurred
	 * @param net_error - Whether or not a network error has occurred
	 */
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

	/**
	 * Opens a specific {@link URI} on the system default browser.
	 * @param uri - URI to website.
	 */
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

	/**
	 * Opens a specific {@link URL} on the system default browser.
	 * @param uri - URL to website.
	 */
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
