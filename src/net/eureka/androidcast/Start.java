package net.eureka.androidcast;

import java.sql.Timestamp;

import net.eureka.androidcast.foundation.init.InitialiseFoundation;
import net.eureka.androidcast.foundation.init.NetworkGlobals;
import net.eureka.androidcast.foundation.logging.Logger;
import net.eureka.androidcast.gui.Menu;
import net.eureka.androidcast.gui.tray.Tray;
import net.eureka.androidcast.mediaserver.NetworkHandler;
import net.eureka.androidcast.mediaserver.discovery.PeerReceiver;

/**
 * This is the starting point of the media server. Here each part is initialized and held as global objects so that they won't be Garbage 
 * Collected. Here is a list of each object that is initialized and their functions. </br>
 * 
 * <pre>
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |Class|||||||||||||||||||Reference Name||||||||||||||Function/s||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |PeerReceiver||||||||||||discoveryServer|||||||||||||UPnP network discovery. Handles sending of Server I.P details to client.||||||||||||||||||||
 * |PasswordReceiver||||||||authenticationServer||||||||TCP/IP client authentication. Handles receiving and verification of password.|||||||||||||||
 * |MediaReceiver|||||||||||mediaServer|||||||||||||||||TCP/IP media server delegate. Handles receiving and verification of media server commands.|| 
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||Also handles another TCP/IP server for media info broadcasting.|||||||||||||||||||||||||||||
 * |Tray||||||||||||||||||||taskBarTray|||||||||||||||||Desktop taskbar tray. Sets up taskbar and handles creation of the options menu.|||||||||||||
 * |InitialiseFoundation||||foundationServer||||||||||||Initializes logger, directory and configuration creation, file fetcher and the file server.|
 * |InitialiseNetwork|||||||initialisingObject||||||||||Creates the above objects in a safe specific order and then holds references to them.|||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * </pre>
 * The default server name is also set up here before the configuration file is read just in case it cannot be found.</br>
 * 
 * Here would be the proper place to restart various components of the server as references need to be held to contain the object tree.</br>
 * </br>
 * <h1> VM ARGUEMENTS: -Dfile.encoding=utf-8 -Xmx1000m -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 </br> Make sure these are run through the
 * start script and login script.</h1>
 * </br>
 * @author Owen McMonagle.
 * @see	PeerReceiver
 * @see Tray
 * @see InitialiseFoundation     
 */
 

@SuppressWarnings("unused")
public class Start 
{
	/**
	 * Used for time stamping the log with the new starting time. 
	 */
	private static final StringBuffer INITIALISING_LOG_TEXT = new StringBuffer("Android Cast:"+ new Timestamp(System.currentTimeMillis()).toString());
	
	/**
	 * UPnP network discovery. Handles sending of Server I.P details to client.
	 * * REQUIRES RESTART AFTER NETWORK FAILURE* Handled automatically.
	 * Object restarts itself from the method InitialiseNetwork.restartPeerDiscovery below.
	 */
	private static PeerReceiver discoveryServer = null;
	
	/**
	 * 
	 */
	private static NetworkHandler clientHandler = null;
	
	/**
	 * Desktop task bar tray icon. Sets up task bar icon and handles creation of the options menu.
	 */
	private static Tray taskBarTray = null;
	
	/**
	 * Initialises logger, directory and configuration creation, file fetcher and the file server.
	 */
	private static InitialiseFoundation foundationSetup = null;
	
	/**
	 * Program Starting Constructor. Initialises each object in a specific order because some components interlink at certain times.
	 * For instance, The Logger is created within InitialiseFoundation. Then after it is created the Logger is called to append the
	 * server starting time to file. If the Logger was called before InitialiseFoundation was created then the server would crash 
	 * because the Logger was not set up. Below has each line is explained.    
	 */
	public Start() 
	{
		if(!Static.is64BitArch())
			Menu.initialise(true, false);
		else
		{		
			// Initialise server name with DHCP interface MAC address. 
			setUpServerName();
			// Initialise task bar along with Settings GUI.
			taskBarTray = new Tray();
			// Initialise Logger, directory and configuration creation, file fetcher and server.
			foundationSetup = new InitialiseFoundation();
			// Append to log when the server has started.
			Logger.append(INITIALISING_LOG_TEXT);
			
			if(!foundationSetup.hasNoDHCPNetwork())
				startNetworking();
			// Initialise menu.
			Menu.initialise(false, foundationSetup.hasNoDHCPNetwork());
			//if(foundationSetup.hasNoDHCPNetwork())
		}
	}
	
	/**
	 * Server program start. Initialises starting constructor {@link InitialiseNetwork}. 
	 * @param args UNUSED.
	 */
	public static void main(String[] args)
	{
		new Start();
	}
	
	/**
	 * Initialises server name with DHCP interface MAC address. Then stores it within NetworkGlobals for use throughout the program if necessary.
	 */
	private static void setUpServerName()
	{
		// Stores it within NetworkGlobals.
		NetworkGlobals.setServerName(NetworkGlobals.getDefaultServerName());
	}
	
	/**
	 * Initialises networking components.
	 */
	public static void startNetworking()
	{
		// Initialise UPnP discovery server.
		discoveryServer = new PeerReceiver();
		// Initialise TCP network server.
		clientHandler = new NetworkHandler();
	}
	
	/**
	 * Restarts peer discovery after a network interface failure.
	 */
	public static void restartPeerDiscovery()
	{
		discoveryServer = new PeerReceiver();
	}
}
