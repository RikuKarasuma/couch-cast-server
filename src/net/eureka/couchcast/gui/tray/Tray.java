package net.eureka.couchcast.gui.tray;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.gui.AppStage;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;

/**
 * Displays and handles the Desktop Tray Icon. Used to create the settings window ( i.e {@link AppStage} ) whenever a 
 * right-click has been detected over the icon image. The Tray is also used to display informational text to the user.
 * e.g When a new client connection is made or new folder has been imported, this is done through static methods that 
 * can be called from anywhere, once the Tray object has been initialized.
 * 
 * @author Owen McMonagle
 * 
 * @see AppStage
 * 
 * @version 0.3
 */
public final class Tray
{
			
	/**
	 * Combines with Program files string above to create a full path to the installed Tray
	 * icon.
	 */
	private static final String ICON_PATH = ApplicationGlobals.getInstallPath() + "logo.png";
	
	/**
	 * Tray icon object, used as a wrapper to contain the icon image and mouse listener.
	 */
	private static TrayIcon icon = null;
	
	/**
	 * Tray image icon. Imports the icon from the Icon Path to be used by the TrayIcon object.
	 */
	private static ImageIcon image = new ImageIcon(ICON_PATH);
	
	/**
	 * Tray set up flag. Used to indicate whether the Tray has been set up or not.
	 */
	private static boolean initialised = false;
	
	/**
	 * Used to prevent spamming of the GUI set up. Supposed to prevent JavaFX lock up.
	 */
	private static boolean opening = false;
	
	private static AppStage menu = null;
	
	/**
	 * Mouse listener, handles right click events on the TrayIcon. Once the icon has been
	 * right-clicked, a new ConfigurationManager( i.e Settings window ) is created. 
	 */
	private static MouseAdapter inputListener = new MouseAdapter() 
	{
		/**
		 * Listens for right-click events. Creates new settings window once clicked.
		 */
		@Override
		public final void mouseClicked(MouseEvent e) 
		{
			if(e.getButton() == MouseEvent.BUTTON1 && opening == false)		
			{
				opening = true;
				menu.setUp();
				opening = false;
			}
		}
	};
	
	/**
	 * Tray constructor, only needs to be called once. Attempts to set up the TrayIcon object if
	 * the operating system supports a system tray. Once the TrayIcon has been created, it is then
	 * added to the SystemTray.
	 * 
	 * @param AppStage - FX GUI controller, creates menus based on needs.
	 */
	public Tray(AppStage parent)
	{
		// If the tray has not been initialized before and is supported by the OS...
		if(!initialised && SystemTray.isSupported())
		{
			menu = parent;
			// Attempt to set up TrayIcon object.
			setUp();
			// Attempt to add TrayIcon object to the System.
			initialiseTray();
		}
		// If the tray icon object is not supported by the OS...
		else if(!SystemTray.isSupported())
			// Show error message stating such.
			JOptionPane.showMessageDialog(null, "Operating system does not support SystemTray. Shutting down.", "ERROR: System tray not supported.", JOptionPane.ERROR_MESSAGE);
		// if the tray icon has already been unnecessary initialized...
		else if(initialised)
			// Show error message stating such.
			JOptionPane.showMessageDialog(null, "System tray already initialised, unnecessary constructor call.", "ERROR: System tray already created.", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Attempts to set up the TrayIcon object. Uses the installed image and obtains the server name from NetworkGlobals
	 * to initialize the TrayIcon. Once instantiated, a mouse listener is attached which will handle user input.
	 */
	private static void setUp()
	{
		System.out.println(ICON_PATH);
		// Instantiate the TrayIcon object with the retrieved Icon image and server name.
		icon = new TrayIcon(image.getImage(), NetworkGlobals.getServerName());
		// Set TrayIcon to auto resize the attached image.
		icon.setImageAutoSize(true);
		// Add a mouse listener to the Icon for user input.
		icon.addMouseListener(inputListener);        
	}
	
	/**
	 * Attempts to add the TrayIcon object to the System Tray. If successful, the Tray is considered initialised. 
	 */
	private static void initialiseTray()
	{
		// Retrieve the O.S's System tray.
		final SystemTray systemTray = SystemTray.getSystemTray();
		try
		{
			// Attempt to add TrayIcon object to system tray.
			systemTray.add(icon);
			// Tray is now initialized.
			initialised = true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Makes use of the TrayIcons displayMessage method. From here, any part of the server can send a message directly
	 * to the user via Tray captions. The 'caption' parameter is the heading of the message, 'text' being the information
	 * associated with that heading.
	 * @param String caption - Heading of the informational message.
	 * @param String text - Information associated with the caption.
	 */
	public static void displayMessage(String caption, String text)
	{
		// Sends the data to the TrayIcon object for displaying to the user.
		icon.displayMessage(caption.toString(), text.toString(), MessageType.INFO);
	}
	
	/**
	 * Called when the user changes the server name. First it updates the TrayIcons tooltip with the 
	 * new server name, then informs the user by displaying a language specific message.
	 */
	public static void displayNameChanged()
	{
		// Updates tray icon with new server name.
		updateToolTipTitle();
		// Displays server name change to the user.
		displayMessage(LanguageDelegator.getLanguageOfComponent(Languages.TRAY_NAME_CHANGE_SUCCESS), LanguageDelegator.getLanguageOfComponent(Languages.TRAY_NAME_CHANGE_MESSAGE)+NetworkGlobals.getServerName());
	}
	
	/**
	 * Called when the user changes the default download directory. First it updates the TrayIcons tooltip with the 
	 * new server name, then informs the user by displaying a language specific message. 
	 */
	public static void displayDownloadFolderChanged()
	{
		// Updates tray icon with new server name.
		updateToolTipTitle();
		// Displays server directory change to the user.
		displayMessage(LanguageDelegator.getLanguageOfComponent(Languages.TRAY_DOWNLOAD_CHANGE_SUCCESS), LanguageDelegator.getLanguageOfComponent(Languages.TRAY_DOWNLOAD_CHANGE_MESSAGE)+ApplicationGlobals.getLastAddedDirectory());
	}
	
	/**
	 * Called when the user changes the password. Once changed informs the user by displaying a language specific message.
	 */
	public static void displayPasswordChanged()
	{
		// Displays server password change to the user.
		displayMessage(LanguageDelegator.getLanguageOfComponent(Languages.TRAY_PASSWORD_CHANGE_SUCCESS), LanguageDelegator.getLanguageOfComponent(Languages.TRAY_PASSWORD_CHANGE_MESSAGE));
	}
	
	/**
	 * Updates the TrayIcon tooltip with the new server name located at NetworkGlobals.
	 */
	public static void updateToolTipTitle()
	{
		String name_ver = ApplicationGlobals.getNameAndVersion(), net_name = NetworkGlobals.getServerName();
		if(icon != null && net_name != null )
			// Update TrayIcon tooltip with the new server name.
			icon.setToolTip(name_ver + ":" + net_name);
	}
	
	/**
	 * Retrieves the image used as an Icon for the TrayIcon object.
	 * @return ImageIcon - Image icon that is used as the server logo.
	 */
	public static ImageIcon getIconImage()
	{
		return image;
	}
	
	public static void updatePlaylist()
	{
		if(menu != null)
			menu.update();
	}
}
